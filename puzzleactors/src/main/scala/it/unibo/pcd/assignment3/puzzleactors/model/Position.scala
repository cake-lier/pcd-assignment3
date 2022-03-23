package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.Position.PositionImpl

/** A position in a grid structure. It is defined through an x and a y coordinate, which values can only be non-negative integers.
  *
  * It must be constructed through its companion object.
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PositionImpl], name = "impl")
  )
)
trait Position extends Ordered[Position] {

  /** Returns the x coordinate of this position. */
  val x: Int

  /** Returns the y coordinate of this position. */
  val y: Int
}

/** Companion object of the [[Position]] trait, containing its factory method. */
object Position {

  /* An implementation of the Position trait. */
  private[model] final case class PositionImpl(x: Int, y: Int) extends Position {

    override def compare(that: Position): Int = if (x =/= that.x) x - that.x else y - that.y
  }

  /** The factory method for creating new instances of the [[Position]] trait given its coordinates.
    * @param x
    *   the x coordinate of the created [[Position]]
    * @param y
    *   the y coordinate of the created [[Position]]
    * @return
    *   a new instance of the [[Position]] trait
    */
  def apply(x: Int, y: Int): Position = PositionImpl(x, y)
}
