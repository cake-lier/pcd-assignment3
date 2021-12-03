package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors.Command._
import it.unibo.pcd.assignment3.controller.actors.ConvertibleToCommand.RichConvertibleToCommand
import it.unibo.pcd.assignment3.model.entities.FilePath
import it.unibo.pcd.assignment3.model.tasks.{DocumentPathsGeneratorTask, StopwordsGeneratorTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PathGeneratorActor {

  def apply(
    filesDirectory: FilePath,
    stopwordsFile: FilePath,
    exceptionHandler: Throwable => Unit,
    pathCoordinator: ActorRef[Command],
    pageCoordinator: ActorRef[Command],
    nextActorFactory: () => Behavior[Command],
    executor: ExecutionContext
  ): Behavior[Command] =
    Behaviors.setup { c =>
      Future(StopwordsGeneratorTask(stopwordsFile))(executor).onComplete {
        case Failure(e) => exceptionHandler(e)
        case Success(v) => pageCoordinator ! StopwordsSetCommand(v, c.self)
      }(c.system.dispatchers.lookup(DispatcherSelector.default()))
      Behaviors.receiveMessage {
        case StopwordsAck =>
          Future(DocumentPathsGeneratorTask(filesDirectory))(executor).onComplete {
            case Failure(e) => exceptionHandler(e)
            case Success(v) =>
              (v.map[Command](_.toCommand).toSeq :+ PoisonPill).foreach(pathCoordinator ! _)
          }(c.system.dispatchers.lookup(DispatcherSelector.default()))
          nextActorFactory()
        case _ => Behaviors.unhandled
      }
    }
}
