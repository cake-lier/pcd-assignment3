package it.unibo.pcd.assignment3.puzzleactors.model

trait Position extends Ordered[Position] {

  val x: Int

  val y: Int
}

object Position {

  private case class PositionImpl(x: Int, y: Int) extends Position {

    override def compare(that: Position): Int = if (x != that.x) x - that.x else y - that.y
  }

  def apply(x: Int, y: Int): Position = PositionImpl(x, y)
}
