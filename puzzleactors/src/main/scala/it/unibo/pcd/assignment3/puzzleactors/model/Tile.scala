package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.Tile.TileImpl

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[TileImpl], name = "impl")
  )
)
trait Tile extends Ordered[Tile] {

  val originalPosition: Position

  val currentPosition: Position

  def isInRightPlace: Boolean
}

object Tile {

  private[model] final case class TileImpl(originalPosition: Position, currentPosition: Position) extends Tile {

    override def isInRightPlace: Boolean = originalPosition === currentPosition

    override def compare(that: Tile): Int = currentPosition.compareTo(that.currentPosition)
  }

  def apply(originalPosition: Position, currentPosition: Position): Tile = TileImpl(originalPosition, currentPosition)
}
