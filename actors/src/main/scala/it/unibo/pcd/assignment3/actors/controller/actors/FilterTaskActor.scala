package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.controller.actors.ConvertibleToCommand._
import it.unibo.pcd.assignment3.actors.model.entities.{Resource, Update}
import it.unibo.pcd.assignment3.actors.model.tasks.{IterableTask, SingletonTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/** A worker actor, an actor whose purpose is to execute [[it.unibo.pcd.assignment3.actors.model.tasks.Task]]s with the input
  * coming from its coordinator. It has the ability to change its behavior into the one of the worker which is next into the data
  * transformation chain. This is made for keeping all actors always busy with work.
  */
object FilterTaskActor {

  /** Returns the behavior of the last FilterTask actor in the data transformation chain, the one without a factory for mutating
    * its behavior into the one of the next kind of worker actors. This is because there is none, being the last.
    * @param root
    *   the root actor of the system
    * @param prevCoordinator
    *   the previous coordinator actor in the data transformation chain, the PageCoordinator actor
    * @param nextCoordinator
    *   the next coordinator actor in the data transformation chain, the UpdateSink actor
    * @param task
    *   the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] this actor has to execute, the one which transforms a
    *   [[Resource]] into an [[Update]]
    * @param executor
    *   the executor on which the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]]s will be executed
    * @param firstBuilt
    *   whether or not the actor was built with this behavior as its first one
    * @return
    *   the behavior of the last FilterTask actor in the data transformation chain
    */
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

  /** Returns the behavior of a generic FilterTask actor which takes a [[SingletonTask]] as the
    * [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to execute.
    * @param root
    *   the root actor of the system
    * @param prevCoordinator
    *   the previous coordinator actor in the data transformation chain
    * @param nextCoordinator
    *   the next coordinator actor in the data transformation chain
    * @param task
    *   the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] this actor has to execute
    * @param executor
    *   the executor on which the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]]s will be executed
    * @param nextActorFactory
    *   the factory instance which allows this actor to mutate its behavior into the one of the next FilterTask actor in the data
    *   transformation chain
    * @param firstBuilt
    *   whether or not the actor was built with this behavior as its first one
    * @param firstConverter
    *   the [[ConvertibleToCommand]] instance capable of converting the type of the [[Command]] received from the previous
    *   coordinator actor into the type of the input of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to be executed
    * @param secondConverter
    *   the [[ConvertibleToCommand]] instance capable of converting the type of the output of the
    *   [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to be executed into the type of the [[Command]] to be sent to the
    *   next coordinator actor
    * @tparam A
    *   the type of [[Command]] received from the previous coordinator actor
    * @tparam B
    *   the type of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] input
    * @tparam C
    *   the type of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] output
    * @tparam D
    *   the type of [[Command]] to be sent to the next coordinator actor
    * @return
    *   the behavior of a generic FilterTask actor
    */
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

  /** Returns the behavior of a generic FilterTask actor which takes a [[IterableTask]] as the
    * [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to execute.
    * @param root
    *   the root actor of the system
    * @param prevCoordinator
    *   the previous coordinator actor in the data transformation chain
    * @param nextCoordinator
    *   the next coordinator actor in the data transformation chain
    * @param task
    *   the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] this actor has to execute
    * @param executor
    *   the executor on which the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]]s will be executed
    * @param nextActorFactory
    *   the factory instance which allows this actor to mutate its behavior into the one of the next FilterTask actor in the data
    *   transformation chain
    * @param firstBuilt
    *   whether or not the actor was built with this behavior as its first one
    * @param firstConverter
    *   the [[ConvertibleToCommand]] instance capable of converting the type of the [[Command]] received from the previous
    *   coordinator actor into the type of the input of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to be executed
    * @param secondConverter
    *   the [[ConvertibleToCommand]] instance capable of converting the type of the output of the
    *   [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] to be executed into the type of the [[Command]] to be sent to the
    *   next coordinator actor
    * @tparam A
    *   the type of [[Command]] received from the previous coordinator actor
    * @tparam B
    *   the type of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] input
    * @tparam C
    *   the type of the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]] output
    * @tparam D
    *   the type of [[Command]] to be sent to the next coordinator actor
    * @return
    *   the behavior of a generic FilterTask actor
    */
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
