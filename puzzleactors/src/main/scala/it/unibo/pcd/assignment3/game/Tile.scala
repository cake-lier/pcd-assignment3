package it.unibo.pcd.assignment3.game


import java.awt.Image

sealed trait Tile{
  val originalPosition: Int
  val currentPosition: Int
}
object Tile {
  final private case class TileImpl(originalPosition: Int, currentPosition: Int) extends Tile

  def apply(originalPosition: Int, currentPosition: Int): Tile = TileImpl(originalPosition,currentPosition)

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def rightPlace(tile: Tile): Boolean = tile.currentPosition == tile.originalPosition

  implicit val comparator: Ordering[Tile] = (a,b) => a.currentPosition - b.currentPosition
}
