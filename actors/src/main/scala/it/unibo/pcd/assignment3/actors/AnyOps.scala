package it.unibo.pcd.assignment3.actors

/** Collection of extension utilities for the Any trait. */
object AnyOps {

  /** Defines the "equals" and "not equals" operations without using cooperative equality. */
  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class AnyOps[A](self: A) {

    /** Checks if two objects are equal without using cooperative equality. */
    def ===(other: A): Boolean = self == other
  }

  /** Allows to explicitly discard a result of a method making it a Unit returning statement. */
  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _ = evaluateForSideEffectOnly
    ()
  }
}
