package it.unibo.pcd.assignment3.actors.controller

import java.util.concurrent.ForkJoinPool

class SuspendableForkJoinPool(threadNumber: Int, suspendedFlag: SuspendedFlag) extends ForkJoinPool(threadNumber) {

  override def execute(runnable: Runnable): Unit = {
    suspendedFlag.check()
    super.execute(runnable)
  }
}
