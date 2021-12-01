package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.model.entities.{Command, PoisonPill}
import it.unibo.pcd.assignment3.model.tasks.{IterableTask, SingletonTask, TaskContext}
import it.unibo.pcd.assignment3.AnyOps.discard

import scala.concurrent.Future

object TaskExecutorActor {

  private def singletonTaskHandler[A <: Command, B <: Command](task: SingletonTask[A, B], a: A, coordinator: ActorRef[B]): Unit =
    coordinator ! task(a)

  private def iterableTaskHandler[A <: Command, B <: Command](task: IterableTask[A, B], a: A, coordinator: ActorRef[B]): Unit =
    task(a).foreach(coordinator ! _)

  def apply[A <: Command, B](taskContexts: Seq[TaskContext[A, B]]): Behavior[Command] = taskContexts match {
    case h :: t =>
      Behaviors.receive { (c, m) =>
        m match {
          case a: A =>
            discard {
              Future(h.task match {
                case s: SingletonTask[_, _] => singletonTaskHandler(s, a, h.coordinator)
                case i: IterableTask[_, _]  => iterableTaskHandler(i, a, h.coordinator)
              })(c.system.dispatchers.lookup(h.dispatcherSelector))
            }
            Behaviors.same
          case p @ PoisonPill(f) =>
            if (f) {
              h.coordinator ! p
            }
            TaskExecutorActor(t)
          case _ => Behaviors.unhandled
        }
      }
    case _ => Behaviors.stopped
  }
}
