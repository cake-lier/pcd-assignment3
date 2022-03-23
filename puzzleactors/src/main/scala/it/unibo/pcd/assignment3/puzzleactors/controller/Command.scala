package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.puzzleactors.model.{GameState, PuzzleBoard, Swap}

/** A command that an actor can send to another into the system.
  *
  * The only possible instances are defined through its companion object.
  */
sealed trait Command

/** Companion object of the [[Command]] trait, containing its instances. */
object Command {

  /** A subtype of [[Command]] to be extended by all commands which need to be serializable because they are sent between the
    * nodes of the cluster.
    */
  sealed trait SerializableCommand extends Command

  /** Command sent from the cluster receptionist to a peer after the registration operation has completed successfully. */
  final case object RegistrationSuccessful extends Command

  /** Command sent from the cluster receptionist to a peer after the subscription operation has completed successfully and
    * whenever the members of the cluster change, either because a new peer joined the cluster or because it left it.
    * @param peers
    *   the current [[Set]] of peers which are member of the cluster
    */
  final case class PeersChanged(peers: Set[ActorRef[Command]]) extends Command

  /** Command sent from new peers to all the other peers in the cluster for receiving their [[GameState]] in order to determine
    * the last one and set it as theirs.
    * @param replyTo
    *   the [[ActorRef]] of the actor making this request and to which all other peers must answer
    * @param timestamp
    *   the [[VectorClock]] embedded in the command as a timestamp
    */
  final case class GameStateRequest(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand

  /** Command sent as a response to a [[GameStateRequest]] command containing the current game state, if the sender has determined
    * it.
    * @param gameState
    *   an [[Option]] containing the current game state of the sender, if it has it
    * @param timestamp
    *   the [[VectorClock]] embedded in the command as a timestamp
    * @param sentFrom
    *   the [[ActorRef]] of the actor sending this command
    */
  final case class GameStateResponse(gameState: Option[GameState], timestamp: VectorClock[String], sentFrom: ActorRef[Command])
    extends SerializableCommand

  /** Command sent by a peer leaving critical section which contains the new current state of the game, after the swap it wanted
    * to apply on the [[PuzzleBoard]] of the previous [[GameState]].
    * @param gameState
    *   the new [[GameState]]
    * @param timestamp
    *   the [[VectorClock]] embedded in the command as a timestamp
    * @param sentFrom
    *   the [[ActorRef]] of the actor sending this command
    */
  final case class GameUpdate(gameState: GameState, timestamp: VectorClock[String], sentFrom: ActorRef[Command])
    extends SerializableCommand

  /** Command sent by a peer after receiving a [[LockRequest]] command indicating to the original sender that it has the peer
    * approval to enter the critical section.
    * @param sentFrom
    *   the [[ActorRef]] of the peer sending this approval
    * @param timestamp
    *   the [[VectorClock]] embedded in the message as a timestamp
    */
  final case class LockPermitted(sentFrom: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand

  /** Command sent from a peer to all other members of the cluster for requesting their approval for entering critical section.
    * @param replyTo
    *   the [[ActorRef]] of the actor wishing to enter critical section
    * @param timestamp
    *   the [[VectorClock]] embedded in the message as a timestamp
    */
  final case class LockRequest(replyTo: ActorRef[Command], timestamp: VectorClock[String]) extends SerializableCommand

  /** Command sent from the root actor of the local actor system to the peer actor when the player requested to do a [[Swap]] move
    * in the puzzle.
    * @param swap
    *   the [[Swap]] move the player intend to perform
    */
  final case class SwapRequest(swap: Swap) extends Command

  /** Command sent from the peer actor to the root actor of the local actor system when a new [[PuzzleBoard]] has been received
    * from the cluster or when the current state of the [[PuzzleBoard]] is to be reset.
    * @param puzzleBoard
    *   the new [[PuzzleBoard]] to be set as the [[PuzzleBoard]] the player is currently seeing
    */
  final case class NewBoardReceived(puzzleBoard: PuzzleBoard) extends Command

  /** Command sent from the peer actor to the root actor of the local actor system to notify that an error in the setup phase has
    * occurred and no current [[GameState]] could be determined.
    */
  final case object SetupError extends Command
}
