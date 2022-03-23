package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.controller.ActorRefOps.RichActorRef
import it.unibo.pcd.assignment3.puzzleactors.controller.Command._
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

/** The peer actor of the system, the one that has to interact with the others for the correct progress of the game. */
object Peer {
  private val addressBookKey: ServiceKey[Command] = ServiceKey[Command]("address_book")

  /* Returns an adapter for transforming a Receptionist.Listing command into a PeersChanged one. */
  private def listingResponseAdapter(ctx: ActorContext[Command]): ActorRef[Receptionist.Listing] =
    ctx.messageAdapter[Receptionist.Listing] { case addressBookKey.Listing(p) =>
      val peers: Set[ActorRef[Command]] = p - ctx.self
      PeersChanged(peers)
    }

  /* Returns an adapter for transforming a Receptionist.Registered message into a RegistrationSuccessful one. */
  private def registrationResponseAdapter(ctx: ActorContext[Command]): ActorRef[Receptionist.Registered] =
    ctx.messageAdapter[Receptionist.Registered](_ => RegistrationSuccessful)

  /* State in which a peer stays while waiting for all GameStateResponses coming from all the other peers in the cluster. */
  private object AwaitingAllStatuses {

    /* Chooses the PuzzleBoard from the current state of the game according to all boards that it has received from the other
     * peers.
     */
    private def chooseBoard(
      root: ActorRef[Command],
      statuses: Map[ActorRef[Command], Option[GameState]],
      timestamp: VectorClock[String]
    ): Behavior[Command] =
      statuses
        .values
        .flatten
        .maxByOption(_.progressiveId)
        .map { g =>
          root ! NewBoardReceived(g.board)
          Idle(root, statuses.keySet, g, timestamp)
        }
        .getOrElse {
          root ! SetupError
          Behaviors.stopped
        }

    /* The main sub-state of the AwaitAllStatuses state, which is a recursive one. */
    private def mainBehavior(
      root: ActorRef[Command],
      statuses: Map[ActorRef[Command], Option[GameState]],
      timestamp: VectorClock[String],
      remainingPeers: Set[ActorRef[Command]]
    ): Behavior[Command] =
      Behaviors.receive { (c, m) =>
        m match {
          case GameStateResponse(g, t, s) if remainingPeers.size > 1 =>
            mainBehavior(root, statuses + (s -> g), timestamp.tick.update(t), remainingPeers - s)
          case GameStateResponse(g, t, s) => chooseBoard(root, statuses + (s -> g), timestamp.tick.update(t))
          case LockRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! LockPermitted(c.self, nextTimestamp)
            mainBehavior(root, statuses, nextTimestamp, remainingPeers)
          case GameUpdate(g, t, _) =>
            root ! NewBoardReceived(g.board)
            Idle(root, statuses.keySet ++ remainingPeers, g, timestamp.tick.update(t))
          case GameStateRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameStateResponse(None, nextTimestamp, c.self)
            mainBehavior(root, statuses, nextTimestamp, remainingPeers)
          case PeersChanged(n) =>
            val allPeers = statuses.keySet ++ remainingPeers
            if (n.size > allPeers.size) {
              val newPeers = n -- allPeers
              mainBehavior(root, statuses, newPeers !! (GameStateRequest(c.self, _), timestamp), remainingPeers ++ newPeers)
            } else {
              val gonePeers = allPeers -- n
              if (gonePeers === remainingPeers)
                chooseBoard(root, statuses, timestamp)
              else
                mainBehavior(root, statuses -- gonePeers, timestamp, remainingPeers -- gonePeers)
            }
          case _ => Behaviors.unhandled
        }
      }

    /* Creates a new AwaitingAllStatuses behavior state. */
    def apply(
      root: ActorRef[Command],
      ctx: ActorContext[Command],
      peers: Set[ActorRef[Command]],
      initialTimestamp: VectorClock[String]
    ): Behavior[Command] =
      mainBehavior(
        root,
        Map.empty[ActorRef[Command], Option[GameState]],
        peers !! (GameStateRequest(ctx.self, _), initialTimestamp),
        peers
      )
  }

  /* State in which a peer stays idling, waiting for the next thing to do.  */
  private object Idle {

    /* Creates a new Idle behavior state. */
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
          case GameStateRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameStateResponse(Some(gameState), nextTimestamp, c.self)
            apply(root, peers, gameState, nextTimestamp)
          case PeersChanged(p)            => apply(root, p, gameState, timestamp)
          case GameStateResponse(_, t, _) => apply(root, peers, gameState, timestamp.tick.update(t))
          case _                          => Behaviors.unhandled
        }
      }
  }

  /* State in which a peer stays while performing the operations for accessing critical section where a Swap of the Tiles of the
   * puzzle can be made.
   */
  private object AwaitLock {

    /* The sub-state of the AwaitLock state that represents the critical section. */
    private def inCriticalSection(
      root: ActorRef[Command],
      self: ActorRef[Command],
      lockRequests: Set[ActorRef[Command]],
      peers: Set[ActorRef[Command]],
      swap: Swap,
      gameState: GameState,
      timestamp: VectorClock[String]
    ): Behavior[Command] = {
      val puzzleBoard: PuzzleBoard = gameState.board.swap(swap)
      val nextGameState: GameState = GameState(puzzleBoard, gameState.progressiveId + 1)
      root ! NewBoardReceived(puzzleBoard)
      val nextTimestamp: VectorClock[String] = peers !! (GameUpdate(nextGameState, _, self), timestamp)
      Idle(root, peers, nextGameState, lockRequests !! (LockPermitted(self, _), nextTimestamp))
    }

    /* The main sub-state of the AwaitLock state, which is a recursive one. */
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
          case GameStateRequest(p, t) =>
            val nextTimestamp = timestamp.tick.update(t).tick
            p ! GameStateResponse(Some(gameState), nextTimestamp, c.self)
            mainBehavior(root, personalLock, swap, gameState, nextTimestamp, lockRequests, failed)
          case GameStateResponse(_, t, _) =>
            mainBehavior(root, personalLock, swap, gameState, timestamp.tick.update(t), lockRequests, failed)
          case _ => Behaviors.unhandled
        }
      }
    }

    /* Creates a new AwaitLock behavior state. */
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

  /** Returns the behavior of a peer actor when the peer is the first one in joining a game session.
    * @param root
    *   the root actor of the local actor system in which the peer actor runs
    * @param board
    *   the initial [[PuzzleBoard]] of the game
    * @return
    *   the behavior of a peer actor when the peer is the first one in joining a game session
    */
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

  /** Returns the behavior of a peer actor when it is an "extra peer", so a peer which is not the first in joining a desired game
    * session.
    * @param root
    *   the root actor of the local actor system in which the peer actor runs
    * @return
    *   the behavior of a peer actor when it is an "extra peer", so a peer which is not the first in joining a desired game
    *   session
    */
  def apply(root: ActorRef[Command]): Behavior[Command] = Behaviors.setup { c =>
    c.system.receptionist ! Receptionist.Register(addressBookKey, c.self, registrationResponseAdapter(c))
    Behaviors.receiveMessage {
      case RegistrationSuccessful =>
        c.system.receptionist ! Receptionist.Subscribe(addressBookKey, listingResponseAdapter(c))
        Behaviors.receiveMessage {
          case PeersChanged(_) =>
            Behaviors.receiveMessage {
              case PeersChanged(p) => AwaitingAllStatuses(root, c, p, VectorClock(c.self.toString))
              case _               => Behaviors.unhandled
            }
          case _ => Behaviors.unhandled
        }
      case _ => Behaviors.unhandled
    }
  }
}
