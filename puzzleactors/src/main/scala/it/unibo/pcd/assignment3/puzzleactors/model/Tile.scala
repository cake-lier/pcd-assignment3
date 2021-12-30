package it.unibo.pcd.assignment3.puzzleactors.model

import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps

trait Tile extends Ordered[Tile] {

  val originalPosition: Position

  val currentPosition: Position

  def isInRightPlace: Boolean
}

object Tile {

  private final case class TileImpl(originalPosition: Position, currentPosition: Position) extends Tile {

    override def isInRightPlace: Boolean = originalPosition === currentPosition

    override def compare(that: Tile): Int = currentPosition.compareTo(that.currentPosition)
  }

  def apply(originalPosition: Position, currentPosition: Position): Tile = TileImpl(originalPosition, currentPosition)
}
