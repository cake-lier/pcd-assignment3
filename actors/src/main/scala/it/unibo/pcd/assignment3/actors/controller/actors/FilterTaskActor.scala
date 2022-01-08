package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command.{
  Available,
  PageCommand,
  PoisonPill,
  Ready,
  StopwordsAck,
  StopwordsSetCommand
}
import it.unibo.pcd.assignment3.actors.controller.actors.ConvertibleToCommand._
import it.unibo.pcd.assignment3.actors.model.entities.{Resource, Update}
import it.unibo.pcd.assignment3.actors.model.tasks.{IterableTask, SingletonTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object FilterTaskActor {

  def apply(
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[Resource, Update],
    executor: ExecutionContext,
    firstBuilt: Boolean
  ): Behavior[Command] = Behaviors.setup { c =>
    prevCoordinator ! Available(c.self)
    Behaviors.receiveMessage {
      case Ready =>
        if (firstBuilt) {
          root ! Ready
        }
        Behaviors.receiveMessage {
          case StopwordsSetCommand(s, r) =>
            r ! StopwordsAck(c.self)
            Behaviors.receiveMessage {
              case PoisonPill => Behaviors.stopped
              case a: PageCommand =>
                implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
                Future(nextCoordinator ! task(Resource(a.fromCommand, s)).toCommand)(executor)
                  .onComplete(_ => prevCoordinator ! Available(c.self))
                Behaviors.same
              case _ => Behaviors.unhandled
            }
          case _ => Behaviors.unhandled
        }
      case PoisonPill => Behaviors.stopped
      case _          => Behaviors.unhandled
    }
  }

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: SingletonTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Boolean => Behavior[Command],
    firstBuilt: Boolean
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] = Behaviors.setup { c =>
    prevCoordinator ! Available(c.self)
    Behaviors.receiveMessage {
      case Ready =>
        if (firstBuilt) {
          root ! Ready
        }
        Behaviors.receiveMessage {
          case PoisonPill => nextActorFactory(false)
          case a: A =>
            implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
            Future(nextCoordinator ! task(a.fromCommand).toCommand)(executor)
              .onComplete(_ => prevCoordinator ! Available(c.self))
            Behaviors.same
          case _ => Behaviors.unhandled
        }
      case PoisonPill => nextActorFactory(false)
      case _          => Behaviors.unhandled
    }
  }

  def apply[A <: Command: ClassTag, B, C, D <: Command](
    root: ActorRef[Command],
    prevCoordinator: ActorRef[Command],
    nextCoordinator: ActorRef[Command],
    task: IterableTask[B, C],
    executor: ExecutionContext,
    nextActorFactory: Boolean => Behavior[Command],
    firstBuilt: Boolean
  )(implicit
    firstConverter: ConvertibleToCommand[B, A],
    secondConverter: ConvertibleToCommand[C, D]
  ): Behavior[Command] = Behaviors.setup { c =>
    prevCoordinator ! Available(c.self)
    Behaviors.receiveMessage {
      case Ready =>
        if (firstBuilt) {
          root ! Ready
        }
        Behaviors.receiveMessage {
          case PoisonPill => nextActorFactory(false)
          case a: A =>
            implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
            Future(task(a.fromCommand).foreach(nextCoordinator ! _.toCommand))(executor)
              .onComplete(_ => prevCoordinator ! Available(c.self))
            Behaviors.same
          case _ => Behaviors.unhandled
        }
      case PoisonPill => nextActorFactory(false)
      case _          => Behaviors.unhandled
    }
  }
}
