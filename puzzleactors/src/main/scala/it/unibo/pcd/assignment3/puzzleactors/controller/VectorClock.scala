package it.unibo.pcd.assignment3.puzzleactors.controller

trait VectorClock[A] {

  val ticks: Map[A, Long]

  def tick: VectorClock[A]

  def update(other: VectorClock[A]): VectorClock[A]

  def isBefore(other: VectorClock[A]): Boolean
}

object VectorClock {

  private class VectorClockImpl[A](self: A, ticks: Map[A, Long]) extends VectorClock[A] {

    override def tick: VectorClock[A] = new VectorClockImpl(self, ticks + (self -> (ticks(self) + 1)))

    override def update(other: VectorClock[A]): VectorClock[A] =
      new VectorClockImpl(
        self,
        other
          .ticks
          .keySet
          .union(ticks.keySet)
          .map(k => k -> other.ticks.getOrElse(k, 0).max(ticks.getOrElse(k, 0)))
          .toMap
      )

    override def isBefore(other: VectorClock[A]): Boolean =
      ticks.forall(e => e._2 <= other.ticks.getOrElse(e._1, 0)) && ticks.exists(e => e._2 < other.ticks.getOrElse(e._1, 0))
  }

  def apply[A](self: A): VectorClock[A] = new VectorClockImpl(self, Map(self -> 0))
}
