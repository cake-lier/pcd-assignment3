package it.unibo.pcd.assignment3.actors.controller

trait SuspendedFlag {

  def suspend(): Unit

  def resume(): Unit

  def check(): Unit
}

import java.util.concurrent.locks.ReentrantLock

object SuspendedFlag {

  private class SuspendedFlagImpl() extends SuspendedFlag {
    private val lock = new ReentrantLock()
    private val suspendedCondition = lock.newCondition()
    private var isRunning = true

    def check(): Unit = {
      lock.lock()
      try {
        while (!this.isRunning) {
          this.suspendedCondition.await()
        }
      } finally {
        this.lock.unlock()
      }
    }

    def suspend(): Unit = {
      this.lock.lock()
      try {
        this.isRunning = false
      } finally {
        this.lock.unlock()
      }
    }

    def resume(): Unit = {
      this.lock.lock()
      try {
        this.isRunning = true
        this.suspendedCondition.signalAll()
      } finally {
        this.lock.unlock()
      }
    }
  }

  def apply(): SuspendedFlag = new SuspendedFlagImpl()
}
