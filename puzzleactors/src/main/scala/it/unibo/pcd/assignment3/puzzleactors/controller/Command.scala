package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

sealed trait Command
sealed trait SerializableCommand extends Command

object Command {
  // receptionist to peer as registration ack
  final case object RegistrationSuccessful extends Command
  // receptionist to peer as subscribe ack and on every peer change
  final case class ChangedPeers(peers: Set[ActorRef[Command]]) extends Command

  // new peer to already joined peers for getting current game status
  final case class RequestGameUpdate(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand
  // peer to peer after explicit request or after swap completed
  final case class GameUpdate(gameState: GameState, timestamp: VectorClock[String], sentFrom: ActorRef[Command])
    extends SerializableCommand
  // peer to peer when the critical section can be accessed according to self
  final case class LockPermitted(player: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand
  // player -> player
  final case class LockRequest(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand

  // controller to peer representative for requesting a swap
  final case class RequestSwap(swap: Swap) extends Command
  // peer representative to controller when a change in the state of the puzzle board occurs
  final case class NewBoardReceived(puzzleBoard: PuzzleBoard) extends Command
}
