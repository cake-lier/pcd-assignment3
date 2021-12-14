package it.unibo.pcd.assignment3

object Operations {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class AnyOps[A](self: A) {
    def ===(other: A): Boolean = self == other
  }

  def discard(evaluateForSideEffectOnly: Any): Unit = {
    val _ = evaluateForSideEffectOnly
    ()
  }
}
