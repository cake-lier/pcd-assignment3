package it.unibo.pcd.assignment3.game.controller

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.typed.{Cluster, Subscribe, Unsubscribe}
import it.unibo.pcd.assignment3.Operations.AnyOps
import it.unibo.pcd.assignment3.game.model.{Actor, Board}

import java.time.Instant

object ActorsUtils {
  val serviceKey: ServiceKey[Message] = ServiceKey[Message]("playerService")
  def listingResponseAdapter(ctx: ActorContext[Message]): ActorRef[Receptionist.Listing] =
    ctx.messageAdapter[Receptionist.Listing] { case ActorsUtils.serviceKey.Listing(players) =>
      OnlinePlayers(players)
    }
  def memberEventAdapter(ctx: ActorContext[Message]): ActorRef[MemberEvent] = ctx.messageAdapter(ParticipantsChange)
}

object Player {
  def apply(controller: Actor, board: Board): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.system.receptionist ! Receptionist.Register(ActorsUtils.serviceKey, ctx.self)
    Idle(controller, GameUpdate(board, 0))
  }
  def apply(controller: Actor): Behavior[Message] =
    Behaviors.setup { ctx =>
      ctx.system.receptionist ! Receptionist.Register(ActorsUtils.serviceKey, ctx.self)
      WaitingForGameStatus(controller)
    }
}

private object WaitingForGameStatus {
  def apply(controller: Actor): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.system.receptionist ! Receptionist.Find(ActorsUtils.serviceKey, ActorsUtils.listingResponseAdapter(ctx))
    Cluster(ctx.system).subscriptions ! Subscribe(ActorsUtils.memberEventAdapter(ctx), classOf[MemberEvent])

    def behavior(
      lockRequests: Set[Actor],
      gameRequests: Set[Actor],
      games: Map[Actor, GameUpdate],
      participantsChange: Boolean = false
    ): Behavior[Message] = Behaviors.setup { ctx =>
      if (games.keySet === gameRequests && !participantsChange && games.nonEmpty) {
        WaitingForGameStatus
          .lastBoard(games)
          .map { b =>
            controller ! NewBoard(b.board)
            Idle(controller, b)
          }
          .getOrElse {
            controller ! Stop()
            Idle(controller, GameUpdate(Board.empty, 0))
          }
      } else
        Behaviors.receiveMessagePartial {
          case _: ParticipantsChange =>
            ctx.system.receptionist ! Receptionist.Find(ActorsUtils.serviceKey, ActorsUtils.listingResponseAdapter(ctx))
            Behaviors.same
          case OnlinePlayers(players) =>
            val otherPlayers = players.filter(p => !(p === ctx.self))
            (otherPlayers -- gameRequests).foreach(_ ! DiscoverGameStatus(ctx.self))
            behavior(lockRequests, otherPlayers, games.filter(p => players.contains(p._1)))
          case DontHaveGameToo(player) => behavior(lockRequests, gameRequests - player, games, participantsChange)
          case GameStatus(lastUpdate, player) =>
            behavior(lockRequests, gameRequests, games + (player -> lastUpdate), participantsChange)
          case DiscoverGameStatus(player) =>
            player ! DontHaveGameToo(ctx.self)
            Behaviors.same
          case LockRequest(player, _) => behavior(lockRequests + player, gameRequests, games, participantsChange)
          case u: GameUpdate =>
            lockRequests.foreach(_ ! LockPermitted(ctx.self))
            Idle(controller, u)
        }
    }
    behavior(Set.empty[Actor], Set.empty[Actor], Map.empty[Actor, GameUpdate], participantsChange = true)
  }

  private def lastBoard(games: Map[Actor, GameUpdate]): Option[GameUpdate] =
    games.values.maxByOption(_.timestamp)
}

private object Idle {
  def apply(controller: Actor, lastBoard: GameUpdate): Behavior[Message] = Behaviors.receive { (ctx, message) =>
    message match {
      case LockRequest(player, _) =>
        player ! LockPermitted(ctx.self)
        Behaviors.same
      case m: Move => WaitingForLock(controller, m, lastBoard)
      case b: GameUpdate =>
        controller ! NewBoard(b.board)
        Idle(controller, b)
      case DiscoverGameStatus(player) =>
        player ! GameStatus(lastBoard, ctx.self)
        Behaviors.same
      case _ => Behaviors.same
    }
  }
}

private object WaitingForLock {
  def apply(controller: Actor, move: Move, lastBoard: GameUpdate): Behavior[Message] = Behaviors.setup { ctx =>
    val personalTimestamp = Instant.now.toEpochMilli
    Cluster(ctx.system).subscriptions ! Subscribe(ActorsUtils.memberEventAdapter(ctx), classOf[MemberEvent])
    ctx.system.receptionist ! Receptionist.Find(ActorsUtils.serviceKey, ActorsUtils.listingResponseAdapter(ctx))

    def behavior(
      personalLock: Map[Actor, Boolean],
      lockRequests: Set[Actor],
      participantsChange: Boolean = false
    ): Behavior[Message] = {
      if (personalLock.values.toList.forall(b => b === true) && !participantsChange) {
        Cluster(ctx.system).subscriptions ! Unsubscribe(ctx.self)
        InCriticalSection(lockRequests, personalLock.keySet, controller, move, lastBoard)
      } else {
        Behaviors.receiveMessagePartial {
          // case change of participants number
          case _: ParticipantsChange =>
            ctx.system.receptionist ! Receptionist.Find(ActorsUtils.serviceKey, ActorsUtils.listingResponseAdapter(ctx))
            behavior(personalLock, lockRequests, participantsChange = true)
          // case message from receptionist with online players
          case OnlinePlayers(players) =>
            val otherPlayers = players.filter(p => !(p === ctx.self))
            (otherPlayers -- personalLock.keySet).foreach(_ ! LockRequest(ctx.self, personalTimestamp))
            behavior(otherPlayers.map(p => p -> personalLock.getOrElse(p, false)).toMap, lockRequests)
          // case lock permitted
          case LockPermitted(player) =>
            behavior(personalLock.filter(p => !(p._1 === player)) + (player -> true), lockRequests, participantsChange)
          // case lock request
          case LockRequest(player, timestamp) if timestamp < personalTimestamp =>
            player ! LockPermitted(ctx.self)
            Behaviors.same
          // case same timestamp undo the move
          case LockRequest(_, timestamp) if timestamp === personalTimestamp =>
            controller ! NewBoard(lastBoard.board)
            lockRequests.foreach(_ ! LockPermitted(ctx.self))
            Idle(controller, lastBoard)
          case LockRequest(player, _) => behavior(personalLock, lockRequests + player, participantsChange)
          // case update
          case b: GameUpdate =>
            controller ! NewBoard(b.board)
            Behaviors.same
          case DiscoverGameStatus(player) =>
            player ! GameStatus(lastBoard, ctx.self)
            Behaviors.same
        }
      }
    }
    behavior(Map.empty[Actor, Boolean], Set.empty[Actor], participantsChange = true)
  }
}

private object InCriticalSection {
  def apply(
    lockRequest: Set[Actor],
    players: Set[Actor],
    controller: Actor,
    move: Move,
    lastBoard: GameUpdate
  ): Behavior[Message] =
    Behaviors.setup { ctx =>
      val gameUpdate = GameUpdate(Board.swap(lastBoard.board, move.card1, move.card2), lastBoard.timestamp + 1)
      controller ! NewBoard(gameUpdate.board)
      players.foreach(_ ! gameUpdate)
      lockRequest.foreach(_ ! LockPermitted(ctx.self))
      Idle(controller, gameUpdate)
    }
}
