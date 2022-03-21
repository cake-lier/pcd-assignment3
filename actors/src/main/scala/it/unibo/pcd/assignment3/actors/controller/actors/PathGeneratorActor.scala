package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.actors.controller.actors.Command._
import it.unibo.pcd.assignment3.actors.controller.actors.ConvertibleToCommand.RichConvertibleToCommand
import it.unibo.pcd.assignment3.actors.model.entities.FilePath
import it.unibo.pcd.assignment3.actors.model.tasks.{DocumentPathsGeneratorTask, StopwordsGeneratorTask}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** The actor which starts the computation by reading the [[it.unibo.pcd.assignment3.actors.model.entities.StopwordsSet]] using
  * the corresponding [[FilePath]] and the [[FilePath]] of the PDF documents using the [[FilePath]] of their directory and then
  * send them to the correct coordinator actors.
  */
object PathGeneratorActor {

  /** Returns the behavior of a PathGenerator actor.
    * @param filesDirectory
    *   the [[FilePath]] of the directory in which the PDF files to be analyzed are located
    * @param stopwordsFile
    *   the [[FilePath]] of the file containing the [[it.unibo.pcd.assignment3.actors.model.entities.StopwordsSet]]
    * @param exceptionHandler
    *   a handler for managing exceptions while reading the content of the various [[FilePath]]s
    * @param pathCoordinator
    *   the first coordinator in the data transformation chain, the PathCoordinator
    * @param pageCoordinator
    *   the coordinator whose job is managing the worker actors for transforming
    *   [[it.unibo.pcd.assignment3.actors.model.entities.Page]]s
    * @param nextActorFactory
    *   the factory instance which allows this actor to mutate its behavior into the one of the next FilterTask actor in the data
    *   transformation chain
    * @param executor
    *   the executor on which the [[it.unibo.pcd.assignment3.actors.model.tasks.Task]]s will be executed
    * @return
    *   the behavior of a PathGenerator actor
    */
  def apply(
    filesDirectory: FilePath,
    stopwordsFile: FilePath,
    exceptionHandler: Throwable => Unit,
    pathCoordinator: ActorRef[Command],
    pageCoordinator: ActorRef[Command],
    nextActorFactory: Boolean => Behavior[Command],
    executor: ExecutionContext
  ): Behavior[Command] =
    Behaviors.setup { c =>
      implicit val dispatcher: ExecutionContext = c.system.dispatchers.lookup(DispatcherSelector.default())
      Future(StopwordsGeneratorTask(stopwordsFile))(executor).onComplete {
        case Failure(e) => exceptionHandler(e)
        case Success(v) => pageCoordinator ! StopwordsSetCommand(v, c.self)
      }
      Behaviors.receiveMessage {
        case StopwordsAck(_) =>
          Future(DocumentPathsGeneratorTask(filesDirectory))(executor).onComplete {
            case Failure(e) => exceptionHandler(e)
            case Success(v) =>
              (v.map[Command](_.toCommand).toSeq :+ PoisonPill).foreach(pathCoordinator ! _)
          }
          nextActorFactory(false)
        case _ => Behaviors.unhandled
      }
    }
}
