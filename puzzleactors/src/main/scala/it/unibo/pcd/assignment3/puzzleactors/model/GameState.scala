package it.unibo.pcd.assignment3.puzzleactors.model

trait GameState {

  val board: PuzzleBoard

  val progressiveId: Long
}

object GameState {

  private case class GameStateImpl(board: PuzzleBoard, progressiveId: Long) extends GameState

  def apply(board: PuzzleBoard, progressiveId: Long): GameState = GameStateImpl(board, progressiveId)

  def unapply(gameState: GameState): Option[(PuzzleBoard, Long)] = Some((gameState.board, gameState.progressiveId))
}
