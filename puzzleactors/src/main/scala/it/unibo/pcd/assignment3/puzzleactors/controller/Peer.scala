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
    ctx.messageAdapter[Receptionist.Listing] { case addressBookKey.Listing(peers) => ChangedPeers(peers) }

  private def registrationResponseAdapter(ctx: ActorContext[Command]): ActorRef[Receptionist.Registered] =
    ctx.messageAdapter[Receptionist.Registered](_ => RegistrationSuccessful)

  private object AwaitingAllStatuses {

    private def awaitRegistration(
      root: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      gameState: GameState,
      timestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] =
      Behaviors.receiveMessage {
        case RegistrationSuccessful => Idle(root, peers, gameState, timestamp)
        case ChangedPeers(n)        => awaitRegistration(root, n, gameState, timestamp)
        case _                      => Behaviors.unhandled
      }

    private def chooseBoard(
      root: ActorRef[Command],
      ctx: ActorContext[Command],
      statuses: Map[ActorRef[Command], GameState],
      timestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] =
      statuses
        .values
        .maxByOption(_.progressiveId)
        .map { g =>
          root ! NewBoardReceived(g.board)
          ctx.system.receptionist ! Receptionist.Register(addressBookKey, ctx.self)
          awaitRegistration(root, statuses.keySet, g, timestamp)
        }
        .getOrElse(Behaviors.empty)

    private def mainBehavior(
      root: ActorRef[Command],
      statuses: Map[ActorRef[Command], GameState],
      timestamp: VectorClock[ActorRef[Command]],
      remainingPeers: Set[ActorRef[Command]]
    ): Behavior[Command] =
      Behaviors.receive { (c, m) =>
        m match {
          case GameUpdate(g, t, s) if remainingPeers.size > 1 =>
            mainBehavior(root, statuses + (s -> g), timestamp.tick.update(t), remainingPeers - s)
          case GameUpdate(g, t, s) => chooseBoard(root, c, statuses + (s -> g), timestamp.tick.update(t))
          case ChangedPeers(n) =>
            val allPeers = statuses.keySet ++ remainingPeers
            if (n.size > allPeers.size) {
              val newPeers = n -- allPeers
              mainBehavior(root, statuses, newPeers !! (RequestGameUpdate(c.self, _), timestamp), remainingPeers ++ newPeers)
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
      peers: Set[ActorRef[Command]],
      initialTimestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] = mainBehavior(root, Map.empty[ActorRef[Command], GameState], initialTimestamp, peers)
  }

  private object Idle {

    def apply(
      root: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      currentGameState: GameState,
      currentTimestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] =
      Behaviors.receive { (c, m) =>
        m match {
          case LockRequest(p, t) =>
            val timestamp = currentTimestamp.tick.update(t).tick
            p ! LockPermitted(c.self, timestamp)
            apply(root, peers, currentGameState, timestamp)
          case RequestSwap(s) => AwaitLock(root, c.self, peers, s, currentGameState, currentTimestamp)
          case GameUpdate(g, t, _) =>
            root ! NewBoardReceived(g.board)
            apply(root, peers, g, currentTimestamp.tick.update(t))
          case RequestGameUpdate(p, t) =>
            val timestamp = currentTimestamp.tick.update(t).tick
            p ! GameUpdate(currentGameState, timestamp, c.self)
            apply(root, peers, currentGameState, timestamp)
          case _ => Behaviors.unhandled
        }
      }
  }

  private object AwaitLock {

    private def inCriticalSection(
      root: ActorRef[Command],
      self: ActorRef[Command],
      lockRequest: Set[ActorRef[Command]],
      peers: Set[ActorRef[Command]],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] = {
      val puzzleBoard: PuzzleBoard = gameState.board.swap(swap.firstPosition, swap.secondPosition)
      val nextGameState: GameState = GameState(puzzleBoard, gameState.progressiveId + 1)
      root ! NewBoardReceived(puzzleBoard)
      val nextTimestamp: VectorClock[ActorRef[Command]] = peers !! (GameUpdate(gameState, _, self), timestamp)
      Idle(root, peers, nextGameState, lockRequest !! (LockPermitted(self, _), nextTimestamp))
    }

    private def mainBehavior(
      root: ActorRef[Command],
      personalLock: Map[ActorRef[Command], Boolean],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[ActorRef[Command]],
      lockRequests: Set[ActorRef[Command]]
    ): Behavior[Command] = {
      Behaviors.receive { (c, m) =>
        m match {
          case ChangedPeers(p) =>
            val allPeers = personalLock.keySet + c.self
            if (p.size > allPeers.size) {
              val newPeers = p -- allPeers
              mainBehavior(
                root,
                newPeers.foldLeft(personalLock)((l, a) => l + (a -> false)),
                swap,
                gameState,
                newPeers !! (LockRequest(c.self, _), timestamp),
                lockRequests
              )
            } else {
              val gonePeers = allPeers -- p
              if (personalLock.filter(!_._2).keySet == gonePeers)
                inCriticalSection(
                  root,
                  c.self,
                  lockRequests -- gonePeers,
                  allPeers -- gonePeers,
                  swap,
                  gameState,
                  timestamp
                )
              else
                mainBehavior(
                  root,
                  personalLock -- gonePeers,
                  swap,
                  gameState,
                  timestamp,
                  lockRequests -- gonePeers
                )
            }
          case LockPermitted(p, t) if personalLock.count(!_._2) > 1 =>
            mainBehavior(root, personalLock + (p -> true), swap, gameState, timestamp.tick.update(t), lockRequests)
          case LockPermitted(_, t) =>
            inCriticalSection(root, c.self, lockRequests, personalLock.keySet + c.self, swap, gameState, timestamp.tick.update(t))
          case LockRequest(p, t) if t.isBefore(timestamp) =>
            val nextTimestamp: VectorClock[ActorRef[Command]] = timestamp.tick.update(t).tick
            p ! LockPermitted(c.self, nextTimestamp)
            mainBehavior(root, personalLock, swap, gameState, nextTimestamp, lockRequests)
          case LockRequest(p, t) =>
            mainBehavior(
              root,
              personalLock,
              swap,
              gameState,
              timestamp.tick.update(t),
              lockRequests + p
            )
          case GameUpdate(g, t, _) =>
            root ! NewBoardReceived(g.board)
            mainBehavior(
              root,
              personalLock,
              swap,
              g,
              timestamp.tick.update(t),
              lockRequests
            )
          case RequestGameUpdate(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameUpdate(gameState, nextTimestamp, c.self)
            mainBehavior(root, personalLock, swap, gameState, nextTimestamp, lockRequests)
        }
      }
    }

    def apply(
      root: ActorRef[Command],
      self: ActorRef[Command],
      peers: Set[ActorRef[Command]],
      swap: Swap,
      currentGameState: GameState,
      currentTimestamp: VectorClock[ActorRef[Command]]
    ): Behavior[Command] =
      mainBehavior(
        root,
        peers.filter(_ !== self).map(_ -> false).toMap,
        swap,
        currentGameState,
        currentTimestamp,
        Set.empty[ActorRef[Command]]
      )
  }

  def apply(root: ActorRef[Command], board: PuzzleBoard): Behavior[Command] = Behaviors.setup { c =>
    c.system.receptionist ! Receptionist.Register(addressBookKey, c.self, registrationResponseAdapter(c))
    Behaviors.receiveMessage {
      case RegistrationSuccessful =>
        c.system.receptionist ! Receptionist.Subscribe(addressBookKey, listingResponseAdapter(c))
        Behaviors.receiveMessage {
          case ChangedPeers(p) => Idle(root, p, GameState(board, 0), VectorClock(c.self))
          case _               => Behaviors.unhandled
        }
      case _ => Behaviors.unhandled
    }
  }

  def apply(root: ActorRef[Command]): Behavior[Command] = Behaviors.setup { c =>
    c.system.receptionist ! Receptionist.Subscribe(addressBookKey, listingResponseAdapter(c))
    Behaviors.receiveMessage {
      case ChangedPeers(p) => AwaitingAllStatuses(root, p, p !! (RequestGameUpdate(c.self, _), VectorClock(c.self)))
      case _               => Behaviors.unhandled
    }
  }
}
