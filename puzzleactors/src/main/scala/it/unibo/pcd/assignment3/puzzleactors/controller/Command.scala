package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

sealed trait Command

object Command {
  // receptionist to peer as registration ack
  final case object RegistrationSuccessful extends Command
  // receptionist to peer after find request
  final case class ChangedPeers(peers: Set[ActorRef[Command]]) extends Command

  // new peer to already joined peers for getting current game status
  final case class RequestGameUpdate(replyTo: ActorRef[Command], timestamp: VectorClock[ActorRef[Command]]) extends Command
  // peer to peer after explicit request or after swap completed
  final case class GameUpdate(gameState: GameState, timestamp: VectorClock[ActorRef[Command]], sentFrom: ActorRef[Command])
    extends Command
  // peer to peer when the critical section can be accessed according to self
  final case class LockPermitted(player: ActorRef[Command], timestamp: VectorClock[ActorRef[Command]]) extends Command
  // player -> player
  final case class LockRequest(replyTo: ActorRef[Command], timestamp: VectorClock[ActorRef[Command]]) extends Command
  // controller -> local player
  final case class RequestSwap(swap: Swap) extends Command
  // local player -> controller
  final case class NewBoardReceived(puzzleBoard: PuzzleBoard) extends Command
}
