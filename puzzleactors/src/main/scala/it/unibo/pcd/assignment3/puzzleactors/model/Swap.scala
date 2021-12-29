package it.unibo.pcd.assignment3.puzzleactors.model

trait Swap {
  val firstPosition: Position

  val secondPosition: Position
}

object Swap {

  private case class SwapImpl(firstPosition: Position, secondPosition: Position) extends Swap

  def apply(firstPosition: Position, secondPosition: Position): Swap = SwapImpl(firstPosition, secondPosition)

  def unapply(swap: Swap): Option[(Position, Position)] = Some((swap.firstPosition, swap.secondPosition))
}
