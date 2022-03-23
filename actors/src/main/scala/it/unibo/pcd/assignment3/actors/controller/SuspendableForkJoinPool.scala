package it.unibo.pcd.assignment3.actors.controller

import java.util.concurrent.ForkJoinPool

/** A [[ForkJoinPool]] which can also be suspended. It can be done by passing a [[SuspendedFlag]] which will be checked before
  * executing any new task. If the flag is set to "suspend", the execution will be suspended until the flag is reset.
  * @param threadNumber
  *   the number of threads to dedicate to this [[ForkJoinPool]]
  * @param suspendedFlag
  *   the flag to be checked for suspension
  */
class SuspendableForkJoinPool(threadNumber: Int, suspendedFlag: SuspendedFlag) extends ForkJoinPool(threadNumber) {

  override def execute(runnable: Runnable): Unit = {
    suspendedFlag.check()
    super.execute(runnable)
  }
}
