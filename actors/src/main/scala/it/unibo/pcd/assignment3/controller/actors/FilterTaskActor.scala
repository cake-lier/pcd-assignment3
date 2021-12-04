package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.pcd.assignment3.controller.actors.Command.{Available, PoisonPill, Ready}
import it.unibo.pcd.assignment3.controller.actors.ConvertibleToCommand._
import it.unibo.pcd.assignment3.model.tasks.{IterableTask, SingletonTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object FilterTaskActor {

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    main(root, prevCoordinator, nextCoordinator, task, executor, None)

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: () => Behavior[Command]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    main(root, prevCoordinator, nextCoordinator, task, executor, Some(nextActorFactory))

  private def main[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Option[() => Behavior[Command]]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] = Behaviors.setup { c =>
    prevCoordinator ! Available(c.self)
    Behaviors.receiveMessage {
      case Ready =>
        root ! Ready
        Behaviors.same
      case PoisonPill =>
        nextActorFactory match {
          case Some(f) => f()
          case _       => Behaviors.stopped
        }
      case a: A =>
        implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
        Future(nextCoordinator ! task(a.fromCommand).toCommand)(executor)
          .onComplete(_ => prevCoordinator ! Available(c.self))
        Behaviors.same
      case _ => Behaviors.unhandled
    }
  }

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    main(root, prevCoordinator, nextCoordinator, task, executor, None)

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: () => Behavior[Command]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] =
    main(root, prevCoordinator, nextCoordinator, task, executor, Some(nextActorFactory))

  private def main[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Option[() => Behavior[Command]]
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] = Behaviors.setup { c =>
    prevCoordinator ! Available(c.self)
    Behaviors.receiveMessage {
      case Ready =>
        root ! Ready
        Behaviors.same
      case PoisonPill =>
        nextActorFactory match {
          case Some(f) => f()
          case _       => Behaviors.stopped
        }
      case a: A =>
        implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
        Future(task(a.fromCommand).foreach(nextCoordinator ! _.toCommand))(executor)
          .onComplete(_ => prevCoordinator ! Available(c.self))
        Behaviors.same
      case _ => Behaviors.unhandled
    }
  }
}
