package it.unibo.pcd.assignment3.puzzleactors.controller

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.controller.VectorClock.VectorClockImpl

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[VectorClockImpl[String]], name = "impl")
  )
)
trait VectorClock[A] {

  val ticks: Map[A, Int]

  def tick: VectorClock[A]

  def update(other: VectorClock[A]): VectorClock[A]

  def isBefore(other: VectorClock[A]): Boolean
}

object VectorClock {

  private[controller] class VectorClockImpl[A](self: A, val ticks: Map[A, Int]) extends VectorClock[A] {

    override def tick: VectorClock[A] = new VectorClockImpl(self, ticks + (self -> (ticks(self) + 1)))

    override def update(other: VectorClock[A]): VectorClock[A] =
      new VectorClockImpl(
        self,
        other
          .ticks
          .keySet
          .union(ticks.keySet)
          .map(k => k -> (other.ticks.getOrElse(k, 0) max ticks.getOrElse(k, 0)))
          .toMap
      )

    override def isBefore(other: VectorClock[A]): Boolean =
      ticks.forall(e => e._2 <= other.ticks.getOrElse(e._1, 0)) && ticks.exists(e => e._2 < other.ticks.getOrElse(e._1, 0))
  }

  def apply[A](self: A): VectorClock[A] = new VectorClockImpl(self, Map(self -> 0))
}
