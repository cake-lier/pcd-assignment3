package it.unibo.pcd.assignment3.model.tasks

import akka.actor.typed.{ActorRef, DispatcherSelector}
import it.unibo.pcd.assignment3.model.entities.Command

trait TaskContext[A <: Command, B] {

  val coordinator: ActorRef[Command]

  val task: Task[A, B]

  val dispatcherSelector: DispatcherSelector
}

object TaskContext {

  private final case class TaskContextImpl[A <: Command, B](
    coordinator: ActorRef[Command],
    task: Task[A, B],
    dispatcherSelector: DispatcherSelector
  ) extends TaskContext[A, B]

  def apply[A <: Command, B](
    coordinator: ActorRef[Command],
    task: Task[A, B],
    dispatcherSelector: DispatcherSelector
  ): TaskContext[A, B] = TaskContextImpl(coordinator, task, dispatcherSelector)
}
