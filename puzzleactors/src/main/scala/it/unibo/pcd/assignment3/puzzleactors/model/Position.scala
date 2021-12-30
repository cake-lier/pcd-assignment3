package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.Position.PositionImpl

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PositionImpl], name = "impl")
  )
)
trait Position extends Ordered[Position] {

  val x: Int

  val y: Int
}

object Position {

  private[model] final case class PositionImpl(x: Int, y: Int) extends Position {

    override def compare(that: Position): Int = if (x =/= that.x) x - that.x else y - that.y
  }

  def apply(x: Int, y: Int): Position = PositionImpl(x, y)
}
