package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.model.GameState.GameStateImpl

/** The state of the game at a given instant in time.
  *
  * It must be constructed through its companion object.
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[GameStateImpl], name = "impl")
  )
)
trait GameState {

  /** Returns the board of the puzzle containing the arrangement of the [[Tile]]s of the puzzle in this state of the game. */
  val board: PuzzleBoard

  /** Returns the unique, progressive id that identifies this state of the game. */
  val progressiveId: Long
}

/** Companion object to the [[GameState]] trait, containing its factory and extractor methods. */
object GameState {

  /* An implementation of the GameState trait. */
  private[model] final case class GameStateImpl(board: PuzzleBoard, progressiveId: Long) extends GameState

  /** The factory method for creating a new instance of the [[GameState]] trait given the [[PuzzleBoard]] that it contains and the
    * progressive id that identifies it.
    * @param board
    *   the board of the puzzle containing the arrangement of the [[Tile]]s of the puzzle in this state of the game
    * @param progressiveId
    *   the unique, progressive id that identifies this state of the game
    * @return
    *   a new instance of the [[GameState]] trait
    */
  def apply(board: PuzzleBoard, progressiveId: Long): GameState = GameStateImpl(board, progressiveId)

  /** The extractor method for destructuring a [[GameState]] instance into its components: its [[PuzzleBoard]] and its progressive
    * id.
    * @param gameState
    *   the instance of [[GameState]] to destructure
    * @return
    *   an [[Option]] containing a [[Tuple2]] with the components of the given [[GameState]] instance
    */
  def unapply(gameState: GameState): Option[(PuzzleBoard, Long)] = Some((gameState.board, gameState.progressiveId))
}
