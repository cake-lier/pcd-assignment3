package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef

object ActorRefOps {

  implicit class RichActorRef(self: Iterable[ActorRef[Command]]) {

    def tellAll(
      messageFactory: VectorClock[String] => Command,
      currentTimestamp: VectorClock[String]
    ): VectorClock[String] =
      self.foldLeft(currentTimestamp) { (t, a) =>
        val nextTimestamp: VectorClock[String] = t.tick
        a ! messageFactory(nextTimestamp)
        nextTimestamp
      }

    def !!(
      messageFactory: VectorClock[String] => Command,
      currentTimestamp: VectorClock[String]
    ): VectorClock[String] =
      tellAll(messageFactory, currentTimestamp)
  }
}
