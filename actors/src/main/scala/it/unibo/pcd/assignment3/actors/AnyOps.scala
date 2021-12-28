package it.unibo.pcd.assignment3.actors

object AnyOps {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class AnyOps[A](self: A) {
    def ===(other: A): Boolean = self == other
  }

  @specialized
  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _ = evaluateForSideEffectOnly
    ()
  }
}
