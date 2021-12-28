package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.controller.actors.ConvertibleToCommand.RichConvertibleToCommand
import it.unibo.pcd.assignment3.actors.model.entities.FilePath
import it.unibo.pcd.assignment3.actors.model.tasks.{DocumentPathsGeneratorTask, StopwordsGeneratorTask}

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
      implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
      Future(StopwordsGeneratorTask(stopwordsFile))(executor).onComplete {
        case Failure(e) => exceptionHandler(e)
        case Success(v) => pageCoordinator ! StopwordsSetCommand(v, c.self)
      }
      Behaviors.receiveMessage {
        case StopwordsAck =>
          Future(DocumentPathsGeneratorTask(filesDirectory))(executor).onComplete {
            case Failure(e) => exceptionHandler(e)
            case Success(v) =>
              (v.map[Command](_.toCommand).toSeq :+ PoisonPill).foreach(pathCoordinator ! _)
          }
          nextActorFactory()
        case _ => Behaviors.unhandled
      }
    }
}
