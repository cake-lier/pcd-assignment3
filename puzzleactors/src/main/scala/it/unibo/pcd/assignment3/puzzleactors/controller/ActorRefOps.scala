package it.unibo.pcd.assignment3.puzzleactors.controller

import akka.actor.typed.ActorRef

/** Collection of extension utilities for the [[ActorRef]] trait. */
object ActorRefOps {

  /** Defines extension methods for [[Iterable]]s of [[ActorRef]]s.
    * @param self
    *   the [[Iterable]] of [[ActorRef]]s instance to which extending its methods
    */
  implicit class RichActorRef(self: Iterable[ActorRef[Command]]) {

    /** Extends the [[ActorRef.tell]] method functionality with the capability of telling the same message to more actors at the
      * same time.
      * @param messageFactory
      *   the factory for creating the message to send to each actor given the [[VectorClock]] to embed in the message
      * @param currentTimestamp
      *   the initial [[VectorClock]] to use for embedding in messages
      * @return
      *   the last [[VectorClock]] used
      */
    def tellAll(
      messageFactory: VectorClock[String] => Command,
      currentTimestamp: VectorClock[String]
    ): VectorClock[String] =
      self.foldLeft(currentTimestamp) { (t, a) =>
        val nextTimestamp: VectorClock[String] = t.tick
        a ! messageFactory(nextTimestamp)
        nextTimestamp
      }

    /** Alias for [[RichActorRef.tellAll]]. */
    def !!(
      messageFactory: VectorClock[String] => Command,
      currentTimestamp: VectorClock[String]
    ): VectorClock[String] =
      tellAll(messageFactory, currentTimestamp)
  }
}
