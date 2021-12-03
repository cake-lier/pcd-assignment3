package it.unibo.pcd.assignment3.controller

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors._
import it.unibo.pcd.assignment3.controller.actors.Command._
import it.unibo.pcd.assignment3.model.entities._
import it.unibo.pcd.assignment3.model.tasks._
import it.unibo.pcd.assignment3.view.View
import it.unibo.pcd.assignment3.AnyOps.discard

import java.nio.file.Path
import scala.concurrent.ExecutionContext

/** The Controller component of this application, it should represent the application itself. That being so, it receives user
  * input from the View component and notifies it of changes in the Model component state. It should also be capable of notifying
  * the Model of requests made by the user and receive the adequate response. At last, it should manage the application state.
  */
trait Controller {

  /** It launches a new computation with the inputs given.
    * @param filesDirectory
    *   the path of the directory containing the PDF files convert process
    * @param stopwordsFile
    *   the path of the file containing the stopwords
    * @param wordsNumber
    *   the number of most frequent words convert display
    */
  def launch(filesDirectory: Path, stopwordsFile: Path, wordsNumber: Int): Unit

  /** It notifies the Model convert suspend the currently running computation. */
  def suspend(): Unit

  /** It notifies the Model convert resume the currently suspended computation. */
  def resume(): Unit

  /** It exits the application. */
  def exit(): Unit
}

object Controller {
  private val totalActors: Int = (Runtime.getRuntime.availableProcessors * 1.0f * (1 + 1.093f)).round

  private class ControllerImpl(view: View) extends Controller {
    private var actorSystem: Option[ActorSystem[Command]] = None
    private val suspendedFlag: SuspendedFlag = SuspendedFlag()
    private val executor: ExecutionContext =
      ExecutionContext.fromExecutor(new SuspendableForkJoinPool(totalActors, suspendedFlag))

    override def launch(filesDirectory: Path, stopwordsFile: Path, wordsNumber: Int): Unit = {
      suspendedFlag.resume()
      actorSystem.foreach(_.terminate())
      actorSystem = Some(
        ActorSystem(
          Behaviors.setup[Command] { c =>
            val updateCoordinator: ActorRef[Command] =
              c.spawn[Command](CoordinatorActor[UpdateCommand](), name = "update_coordinator")
            val pageCoordinator: ActorRef[Command] =
              c.spawn[Command](CoordinatorActor.pageCoordinator(updateCoordinator), name = "page_coordinator")
            val documentCoordinator: ActorRef[Command] =
              c.spawn[Command](CoordinatorActor[DocumentCommand](pageCoordinator), name = "document_coordinator")
            val pathCoordinator: ActorRef[Command] =
              c.spawn[Command](CoordinatorActor[FilePathCommand](documentCoordinator), name = "path_coordinator")
            val pageFilterFactory: () => Behavior[Command] =
              () =>
                FilterTaskActor[ResourceCommand, Resource, Update, UpdateCommand](
                  pageCoordinator,
                  updateCoordinator,
                  PageFilterTask,
                  executor
                )
            val documentFilterFactory: () => Behavior[Command] = () =>
              FilterTaskActor[DocumentCommand, Document, Page, PageCommand](
                documentCoordinator,
                pageCoordinator,
                DocumentFilterTask,
                executor,
                pageFilterFactory
              )
            val pathFilterFactory: () => Behavior[Command] = () =>
              FilterTaskActor[FilePathCommand, FilePath, Document, DocumentCommand](
                pathCoordinator,
                documentCoordinator,
                PathFilterTask,
                executor,
                documentFilterFactory
              )
            discard {
              c.spawn[Command](UpdateSinkActor(wordsNumber, view, updateCoordinator), name = "update_sink_actor")
            }
            LazyList
              .continually(FilterTaskType.values.toSeq)
              .zipWithIndex
              .flatMap(e => e._1.map((_, e._2.toString)))
              .take(Math.max(totalActors - 6, 3))
              .foreach {
                case (FilterTaskType.Path, n)     => c.spawn[Command](pathFilterFactory(), name = s"path_filter_actor_$n")
                case (FilterTaskType.Document, n) => c.spawn[Command](documentFilterFactory(), name = s"document_filter_actor_$n")
                case (FilterTaskType.Page, n)     => c.spawn[Command](pageFilterFactory(), name = s"page_filter_actor_$n")
              }
            discard {
              c.spawn[Command](
                PathGeneratorActor(
                  FilePath(filesDirectory),
                  FilePath(stopwordsFile),
                  e => view.displayError(e.getMessage),
                  pathCoordinator,
                  pageCoordinator,
                  pathFilterFactory,
                  executor
                ),
                name = "path_generator_actor"
              )
            }
            Behaviors.receiveMessage {
              case PoisonPill =>
                actorSystem = None
                Behaviors.stopped
              case _ => Behaviors.unhandled
            }
          },
          "actor_system"
        )
      )
    }

    override def suspend(): Unit = suspendedFlag.suspend()

    override def resume(): Unit = suspendedFlag.resume()

    override def exit(): Unit = {
      actorSystem.foreach(_.terminate())
      sys.exit()
    }
  }

  def apply(view: View): Controller = new ControllerImpl(view)
}
