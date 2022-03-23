package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.Tile.TileImpl

/** A piece of a puzzle that can be swapped with another to reorder and solve it. It can be kept in order along with others using
  * their current [[Position]]s to display the current tiles' arrangement.
  *
  * It must be constructed through its companion object.
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[TileImpl], name = "impl")
  )
)
trait Tile extends Ordered[Tile] {

  /** Returns the original [[Position]] of this tile, the one in the reordered puzzle. */
  val originalPosition: Position

  /** Returns the current [[Position]] of this tile, the one in the current tiles' arrangement. */
  val currentPosition: Position

  /** Returns whether this tile is currently in the same [[Position]] as its one in the reordered puzzle. */
  def isInRightPlace: Boolean
}

/** Companion object to the [[Tile]] trait, containing its factory method. */
object Tile {

  /* An implementation of the Tile trait. */
  private[model] final case class TileImpl(originalPosition: Position, currentPosition: Position) extends Tile {

    override def isInRightPlace: Boolean = originalPosition === currentPosition

    override def compare(that: Tile): Int = currentPosition.compareTo(that.currentPosition)
  }

  /** The factory method for creating a new instance of the [[Tile]] trait, given its original [[Position]] and its current
    * [[Position]].
    * @param originalPosition
    *   the [[Position]] of the created [[Tile]] in the reordered puzzle
    * @param currentPosition
    *   the [[Position]] of the created [[Tile]] in the current tiles' arrangement
    * @return
    *   a new instance of the [[Tile]] trait
    */
  def apply(originalPosition: Position, currentPosition: Position): Tile = TileImpl(originalPosition, currentPosition)
}
