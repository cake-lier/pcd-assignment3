package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors.Command.{Available, PoisonPill}
import it.unibo.pcd.assignment3.controller.actors.ConvertibleToCommand._
import it.unibo.pcd.assignment3.model.tasks.{IterableTask, SingletonTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object FilterTaskActor {

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    apply(prevCoordinator, nextCoordinator, task, executor, None)

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: () => Behavior[Command]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    apply(prevCoordinator, nextCoordinator, task, executor, Some(nextActorFactory))

  private def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Option[() => Behavior[Command]]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    Behaviors.setup { c =>
      prevCoordinator ! Available(c.self)
      Behaviors.receiveMessage {
        case PoisonPill =>
          nextActorFactory match {
            case Some(f) => f()
            case _       => Behaviors.stopped
          }
        case a: A =>
          Future(nextCoordinator ! task(a.fromCommand).toCommand)(executor)
            .onComplete(_ => prevCoordinator ! Available(c.self))(c.system.dispatchers.lookup(DispatcherSelector.default()))
          Behaviors.same
        case _ => Behaviors.unhandled
      }
    }

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    apply(prevCoordinator, nextCoordinator, task, executor, None)

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: () => Behavior[Command]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    apply(prevCoordinator, nextCoordinator, task, executor, Some(nextActorFactory))

  private def apply[A <: Command: ClassTag, B, C, D <: Command](
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Option[() => Behavior[Command]]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    Behaviors.setup { c =>
      prevCoordinator ! Available(c.self)
      Behaviors.receiveMessage {
        case PoisonPill =>
          nextActorFactory match {
            case Some(f) => f()
            case _       => Behaviors.stopped
          }
        case a: A =>
          Future(task(a.fromCommand).foreach(nextCoordinator ! _.toCommand))(executor)
            .onComplete(_ => prevCoordinator ! Available(c.self))(c.system.dispatchers.lookup(DispatcherSelector.default()))
          Behaviors.same
        case _ => Behaviors.unhandled
      }
    }
}
