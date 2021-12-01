package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.model.entities.{Availability, Command, PoisonPill}
import it.unibo.pcd.assignment3.model.tasks.{IterableTask, SingletonTask, Task, TaskContext}
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
              Future({h.task match {
                case s: SingletonTask[_, _] => singletonTaskHandler(s, a, h.coordinator)
                case i: IterableTask[_, _]  => iterableTaskHandler(i, a, h.coordinator)
              }; h.coordinator ! Availability(c.self)})(c.system.dispatchers.lookup(h.dispatcherSelector))
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

object CoordinatorActor{
  def apply[I <: Command](actors: Set[ActorRef[Command]],forwardPill: Boolean = true): Behavior[Command] =
    state[I](actors.map(_ -> 0).toMap,forwardPill)
  private def state[I <: Command](actors: Map[ActorRef[Command],Int],forwardPill: Boolean): Behavior[Command] = Behaviors.receive { (_, msg) =>
    msg match {
      case Availability(actor: ActorRef[Command]) => state(actors updated(actor, actors(actor)-1),forwardPill)
      case d : I => actors.minByOption(_._2)
        .map(_._1)
        .map(a => {
          a ! d
          state(actors updated (a,actors(a)+1),forwardPill)
        })
        .getOrElse(Behaviors.stopped)
      case _:PoisonPill =>
        actors
          .minByOption(_._2)
          .map(_._1)
          .map(a => {
            a ! PoisonPill(forwardPill)
            actors - a
          }).flatMap(_.keys)
          .foreach(_ ! PoisonPill(false))
        Behaviors.stopped
    }
  }
}
