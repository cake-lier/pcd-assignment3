package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef

object ActorRefOps {

  implicit class RichActorRef(self: Iterable[ActorRef[Command]]) {

    def tellAll(
      messageFactory: VectorClock[ActorRef[Command]] => Command,
      currentTimestamp: VectorClock[ActorRef[Command]]
    ): VectorClock[ActorRef[Command]] =
      self.foldLeft(currentTimestamp) { (t, a) =>
        val nextTimestamp: VectorClock[ActorRef[Command]] = t.tick
        a ! messageFactory(nextTimestamp)
        nextTimestamp
      }

    def !!(
      messageFactory: VectorClock[ActorRef[Command]] => Command,
      currentTimestamp: VectorClock[ActorRef[Command]]
    ): VectorClock[ActorRef[Command]] =
      tellAll(messageFactory, currentTimestamp)
  }
}
