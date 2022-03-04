package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

sealed trait Command
sealed trait SerializableCommand extends Command

object Command {
  // receptionist to peer as registration ack
  final case object RegistrationSuccessful extends Command
  // receptionist to peer as subscribe ack and on every peer change
  final case class PeersChanged(peers: Set[ActorRef[Command]]) extends Command
  // new peer to already joined peers for getting current game state
  final case class GameUpdateRequest(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand
  // peer to peer after explicit request or after swap completed
  final case class GameUpdate(gameState: GameState, timestamp: VectorClock[String], sentFrom: ActorRef[Command])
    extends SerializableCommand
  // peer to another peer when the critical section can be accessed according to sender
  final case class LockPermitted(player: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand
  // peer to another peer when sender wants to enter critical section
  final case class LockRequest(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand
  // controller to peer representative for requesting a swap
  final case class SwapRequest(swap: Swap) extends Command
  // peer representative to controller when a change in the state of the puzzle board occurs
  final case class NewBoardReceived(puzzleBoard: PuzzleBoard) extends Command
  // peer representative to controller when the board cannot be choose
  final case object SetupError extends Command
}
