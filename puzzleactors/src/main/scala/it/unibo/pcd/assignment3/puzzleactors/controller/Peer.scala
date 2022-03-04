package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.controller.ActorRefOps.RichActorRef
import it.unibo.pcd.assignment3.puzzleactors.controller.Command._
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

object Peer {
  private val addressBookKey: ServiceKey[Command] = ServiceKey[Command]("address_book")

  private def listingResponseAdapter(ctx: ActorContext[Command]): ActorRef[Receptionist.Listing] =
    ctx.messageAdapter[Receptionist.Listing] { case addressBookKey.Listing(p) =>
      val peers: Set[ActorRef[Command]] = p - ctx.self
      PeersChanged(peers)
    }

  private def registrationResponseAdapter(ctx: ActorContext[Command]): ActorRef[Receptionist.Registered] =
    ctx.messageAdapter[Receptionist.Registered](_ => RegistrationSuccessful)

  private object AwaitingAllStatuses {

    private def awaitRegistration(
      root: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      gameState: GameState,
      timestamp: VectorClock[String]
    ): Behavior[Command] =
      Behaviors.receiveMessage {
        case RegistrationSuccessful =>
          Behaviors.receiveMessage {
            case PeersChanged(_) => Idle(root, peers, gameState, timestamp)
            case _               => Behaviors.unhandled
          }
        case PeersChanged(n) => awaitRegistration(root, n, gameState, timestamp)
        case _               => Behaviors.unhandled
      }

    private def chooseBoard(
      root: ActorRef[Command],
      ctx: ActorContext[Command],
      statuses: Map[ActorRef[Command], GameState],
      timestamp: VectorClock[String]
    ): Behavior[Command] =
      statuses
        .values
        .maxByOption(_.progressiveId)
        .map { g =>
          root ! NewBoardReceived(g.board)
          ctx.system.receptionist ! Receptionist.Register(addressBookKey, ctx.self, registrationResponseAdapter(ctx))
          awaitRegistration(root, statuses.keySet, g, timestamp)
        }
        .getOrElse(SetupFailed(root))

    private def mainBehavior(
      root: ActorRef[Command],
      statuses: Map[ActorRef[Command], GameState],
      timestamp: VectorClock[String],
      remainingPeers: Set[ActorRef[Command]]
    ): Behavior[Command] =
      Behaviors.receive { (c, m) =>
        m match {
          case GameUpdate(g, t, s) if remainingPeers.size > 1 =>
            mainBehavior(root, statuses + (s -> g), timestamp.tick.update(t), remainingPeers - s)
          case GameUpdate(g, t, s) => chooseBoard(root, c, statuses + (s -> g), timestamp.tick.update(t))
          case PeersChanged(n) =>
            val allPeers = statuses.keySet ++ remainingPeers
            if (n.size > allPeers.size) {
              val newPeers = n -- allPeers
              mainBehavior(root, statuses, newPeers !! (GameUpdateRequest(c.self, _), timestamp), remainingPeers ++ newPeers)
            } else {
              val gonePeers = allPeers -- n
              if (gonePeers === remainingPeers)
                chooseBoard(root, c, statuses, timestamp)
              else
                mainBehavior(root, statuses -- gonePeers, timestamp, remainingPeers -- gonePeers)
            }
          case _ => Behaviors.unhandled
        }
      }

    def apply(
      root: ActorRef[Command],
      ctx: ActorContext[Command],
      peers: Set[ActorRef[Command]],
      initialTimestamp: VectorClock[String]
    ): Behavior[Command] =
      mainBehavior(
        root,
        Map.empty[ActorRef[Command], GameState],
        peers !! (GameUpdateRequest(ctx.self, _), initialTimestamp),
        peers
      )
  }

  private object SetupFailed {
    def apply(root: ActorRef[Command]): Behavior[Command] = Behaviors.setup { _ =>
      root ! SetupError
      Behaviors.stopped
    }
  }

  private object Idle {

    def apply(
      root: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      gameState: GameState,
      timestamp: VectorClock[String]
    ): Behavior[Command] =
      Behaviors.receive { (c, m) =>
        m match {
          case LockRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! LockPermitted(c.self, nextTimestamp)
            apply(root, peers, gameState, nextTimestamp)
          case SwapRequest(s) => AwaitLock(root, c.self, peers, s, gameState, timestamp)
          case GameUpdate(g, t, _) =>
            root ! NewBoardReceived(g.board)
            apply(root, peers, g, timestamp.tick.update(t))
          case GameUpdateRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameUpdate(gameState, nextTimestamp, c.self)
            apply(root, peers, gameState, nextTimestamp)
          case PeersChanged(p) => apply(root, p, gameState, timestamp)
          case _               => Behaviors.unhandled
        }
      }
  }

  private object AwaitLock {

    private def inCriticalSection(
      root: ActorRef[Command],
      self: ActorRef[Command],
      lockRequests: Set[ActorRef[Command]],
      peers: Set[ActorRef[Command]],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[String]
    ): Behavior[Command] = {
      val puzzleBoard: PuzzleBoard = gameState.board.swap(swap.firstPosition, swap.secondPosition)
      val nextGameState: GameState = GameState(puzzleBoard, gameState.progressiveId + 1)
      root ! NewBoardReceived(puzzleBoard)
      val nextTimestamp: VectorClock[String] = peers !! (GameUpdate(nextGameState, _, self), timestamp)
      Idle(root, peers, nextGameState, lockRequests !! (LockPermitted(self, _), nextTimestamp))
    }

    private def mainBehavior(
      root: ActorRef[Command],
      personalLock: Map[ActorRef[Command], Boolean],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[String],
      lockRequests: Set[ActorRef[Command]],
      failed: Boolean
    ): Behavior[Command] = {
      Behaviors.receive { (c, m) =>
        m match {
          case PeersChanged(p) =>
            val allPeers = personalLock.keySet
            if (p.size > allPeers.size) {
              val newPeers = p -- allPeers
              mainBehavior(
                root,
                personalLock ++ newPeers.map(_ -> false).toMap,
                swap,
                gameState,
                newPeers !! (LockRequest(c.self, _), timestamp),
                lockRequests,
                failed
              )
            } else {
              val gonePeers = allPeers -- p
              if (personalLock.filter(!_._2).keySet === gonePeers) {
                if (failed)
                  Idle(root, personalLock.keySet, gameState, lockRequests !! (LockPermitted(c.self, _), timestamp))
                else
                  inCriticalSection(root, c.self, lockRequests -- gonePeers, allPeers -- gonePeers, swap, gameState, timestamp)
              } else
                mainBehavior(root, personalLock -- gonePeers, swap, gameState, timestamp, lockRequests -- gonePeers, failed)
            }
          case LockPermitted(p, t) if personalLock.count(!_._2) > 1 =>
            mainBehavior(root, personalLock + (p -> true), swap, gameState, timestamp.tick.update(t), lockRequests, failed)
          case LockPermitted(_, t) =>
            if (failed)
              Idle(root, personalLock.keySet, gameState, lockRequests !! (LockPermitted(c.self, _), timestamp))
            else
              inCriticalSection(root, c.self, lockRequests, personalLock.keySet, swap, gameState, timestamp.tick.update(t))
          case LockRequest(p, t) if timestamp < t =>
            mainBehavior(root, personalLock, swap, gameState, timestamp.tick.update(t), lockRequests + p, failed)
          case LockRequest(p, t) =>
            val nextTimestamp: VectorClock[String] = timestamp.tick.update(t)
            if (t < timestamp) {
              val sentTimestamp: VectorClock[String] = nextTimestamp.tick
              p ! LockPermitted(c.self, sentTimestamp)
              mainBehavior(root, personalLock, swap, gameState, sentTimestamp, lockRequests, failed)
            } else {
              root ! NewBoardReceived(gameState.board)
              if (personalLock.count(!_._2) > 0)
                mainBehavior(root, personalLock + (p -> true), swap, gameState, nextTimestamp, lockRequests, failed = true)
              else
                Idle(root, personalLock.keySet, gameState, lockRequests !! (LockPermitted(c.self, _), nextTimestamp))
            }
          case GameUpdate(g, t, _) =>
            root ! NewBoardReceived(g.board)
            mainBehavior(
              root,
              personalLock,
              swap,
              g,
              timestamp.tick.update(t),
              lockRequests,
              failed
            )
          case GameUpdateRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameUpdate(gameState, nextTimestamp, c.self)
            mainBehavior(root, personalLock, swap, gameState, nextTimestamp, lockRequests, failed)
          case _ => Behaviors.unhandled
        }
      }
    }

    def apply(
      root: ActorRef[Command],
      self: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[String]
    ): Behavior[Command] = {
      if (peers.nonEmpty)
        mainBehavior(
          root,
          peers.map(_ -> false).toMap,
          swap,
          gameState,
          peers !! (LockRequest(self, _), timestamp),
          Set.empty[ActorRef[Command]],
          failed = false
        )
      else
        inCriticalSection(root, self, Set.empty[ActorRef[Command]], peers, swap, gameState, timestamp)
    }
  }

  def apply(root: ActorRef[Command], board: PuzzleBoard): Behavior[Command] = Behaviors.setup { c =>
    c.system.receptionist ! Receptionist.Register(addressBookKey, c.self, registrationResponseAdapter(c))
    Behaviors.receiveMessage {
      case RegistrationSuccessful =>
        c.system.receptionist ! Receptionist.Subscribe(addressBookKey, listingResponseAdapter(c))
        Behaviors.receiveMessage {
          case PeersChanged(_) => Idle(root, Set.empty[ActorRef[Command]], GameState(board, 0), VectorClock(c.self.toString))
          case _               => Behaviors.unhandled
        }
      case _ => Behaviors.unhandled
    }
  }

  def apply(root: ActorRef[Command]): Behavior[Command] = Behaviors.setup { c =>
    c.system.receptionist ! Receptionist.Subscribe(addressBookKey, listingResponseAdapter(c))
    Behaviors.receiveMessage {
      case PeersChanged(_) =>
        Behaviors.receiveMessage {
          case PeersChanged(p) => AwaitingAllStatuses(root, c, p, VectorClock(c.self.toString))
          case _               => Behaviors.unhandled
        }
      case _ => Behaviors.unhandled
    }
  }
}
