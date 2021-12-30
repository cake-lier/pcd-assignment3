package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.model.GameState.GameStateImpl

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[GameStateImpl], name = "impl")
  )
)
trait GameState {

  val board: PuzzleBoard

  val progressiveId: Long
}

object GameState {

  private[model] final case class GameStateImpl(board: PuzzleBoard, progressiveId: Long) extends GameState

  def apply(board: PuzzleBoard, progressiveId: Long): GameState = GameStateImpl(board, progressiveId)

  def unapply(gameState: GameState): Option[(PuzzleBoard, Long)] = Some((gameState.board, gameState.progressiveId))
}
