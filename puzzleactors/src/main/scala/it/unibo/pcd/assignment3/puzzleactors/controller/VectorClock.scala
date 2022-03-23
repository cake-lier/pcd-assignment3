package it.unibo.pcd.assignment3.puzzleactors.controller

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.controller.VectorClock.VectorClockImpl

/** A vector clock, a data structure to be used for determining the partial ordering of events in a distributed system using a
  * "happened-before" relation.
  *
  * It must be constructed through its companion object.
  * @tparam A
  *   the type of the process identifier in the distributed system
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[VectorClockImpl[String]], name = "impl")
  )
)
trait VectorClock[A] {

  /** Returns the [[Map]] used for associating each process with its own value for the clock. */
  val ticks: Map[A, Int]

  /** Increments the value in this vector clock for the process that owns it by one.
    * @return
    *   this [[VectorClock]] with the value for the process that owns it incremented by one
    */
  def tick: VectorClock[A]

  /** Updates the values contained in this vector clock using the other given [[VectorClock]]. The update operation keeps the
    * highest value for each process between the ones given by the two clocks. If the value for a process in a clock is absent, it
    * is treated as being zero.
    * @param other
    *   the other [[VectorClock]] used for updating this [[VectorClock]]
    * @return
    *   this [[VectorClock]] with the values inside it updated using the other given [[VectorClock]]
    */
  def update(other: VectorClock[A]): VectorClock[A]

  /** Returns if this clock is associated to an event which happened before the one associated with the given other
    * [[VectorClock]].
    * @param other
    *   the other [[VectorClock]] to be used for doing this comparison
    * @return
    *   if this clock is associated to an event which happened before the one associated with the given other [[VectorClock]]
    */
  def isBefore(other: VectorClock[A]): Boolean

  /** Alias for [[VectorClock.isBefore]]. */
  def <(other: VectorClock[A]): Boolean = isBefore(other)
}

/** Companion object to the [[VectorClock]] trait, containing its factory method. */
object VectorClock {

  /* An implementation of the VectorClock trait. */
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

  /** The factory method for creating new instances of the [[VectorClock]] trait given the identifier of the owner process of this
    * clock.
    * @param self
    *   the identifier of the owner process of this [[VectorClock]]
    * @tparam A
    *   the type of the process identifier in the distributed system
    * @return
    *   a new instance of the [[VectorClock]] trait
    */
  def apply[A](self: A): VectorClock[A] = new VectorClockImpl(self, Map(self -> 0))
}
