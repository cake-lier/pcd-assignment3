package it.unibo.pcd.assignment3.actors.controller

/** A settable flag that allows tasks to check whether their execution should continue or should be suspended.
  *
  * It must be constructed through its companion object.
  */
trait SuspendedFlag {

  /** It sets the state of this flag to "suspended". */
  def suspend(): Unit

  /** It sets the state of this flag to "running". */
  def resume(): Unit

  /** It checks if the execution should continue or should be suspended. This method is blocking if the state of the flag is set
    * to "suspended".
    */
  def check(): Unit
}

import java.util.concurrent.locks.ReentrantLock

/** Companion object to the [[SuspendedFlag]] trait, containing its factory method. */
object SuspendedFlag {

  /* An implementation of the SuspendedFlag trait. */
  private class SuspendedFlagImpl extends SuspendedFlag {
    private val lock = new ReentrantLock()
    private val suspendedCondition = lock.newCondition()
    private var isRunning = true

    def check(): Unit = {
      lock.lock()
      try {
        while (!isRunning) {
          suspendedCondition.await()
        }
      } finally {
        lock.unlock()
      }
    }

    def suspend(): Unit = {
      lock.lock()
      try {
        isRunning = false
      } finally {
        lock.unlock()
      }
    }

    def resume(): Unit = {
      lock.lock()
      try {
        isRunning = true
        suspendedCondition.signalAll()
      } finally {
        lock.unlock()
      }
    }
  }

  /** Returns a new instance of the [[SuspendedFlag]] trait. */
  def apply(): SuspendedFlag = new SuspendedFlagImpl()
}
