package it.unibo.pcd.assignment3.controller

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.controller.actors.TaskExecutorActor
import it.unibo.pcd.assignment3.model.entities.{Command, FilePath, Page, Update}
import it.unibo.pcd.assignment3.model.tasks.{FilterTaskType, PageFilterTask, TaskContext}

import java.nio.file.Path

/** The Controller component of this application, it should represent the application itself. That being so, it receives user
  * input from the View component and notifies it of changes in the Model component state. It should also be capable of notifying
  * the Model of requests made by the user and receive the adequate response. At last, it should manage the application state.
  */
trait Controller {

  /** It launches a new computation with the inputs given.
    * @param filesDirectory
    *   the path of the directory containing the PDF files to process
    * @param stopwordsFile
    *   the path of the file containing the stopwords
    * @param wordsNumber
    *   the number of most frequent words to display
    */
  def launch(filesDirectory: Path, stopwordsFile: Path, wordsNumber: Int): Unit

  /** It notifies the Model to suspend the currently running computation. */
  def suspend(): Unit

  /** It notifies the Model to resume the currently suspended computation. */
  def resume(): Unit

  /** It exits the application. */
  def exit(): Unit
}

object Controller {
  private val totalThreads: Int = (Runtime.getRuntime.availableProcessors * 1.0f * (1 + 1.093f)).round

  private class ControllerImpl extends Controller {
    private var actorSystem: Option[ActorSystem[Command]] = None

    override def launch(filesDirectory: Path, stopwordsFile: Path, wordsNumber: Int): Unit =
      actorSystem = Some(
        ActorSystem(
          Behaviors.setup[Command] { c =>
            val pathCoordinator = c.spawn[Command](???, "path_coordinator")
            val pageCoordinator = c.spawn[Command](???, "page_coordinator")
            val updateCoordinator = c.spawn[Command](???, "update_coordinator")
            LazyList
              .continually(FilterTaskType.values.toSeq)
              .flatten
              .take(totalThreads - 2)
              .map {
                case FilterTaskType.Path => TaskExecutorActor[FilePath, Page](Seq.empty[TaskContext[FilePath, Page]])
                case FilterTaskType.Page =>
                  TaskExecutorActor(Seq(TaskContext(updateCoordinator, PageFilterTask(), DispatcherSelector.blocking())))
                case FilterTaskType.Update => TaskExecutorActor[Page, Update](Seq.empty[TaskContext[Page, Update]])
              }
              .foreach(c.spawn[Command](_, ""))
            Behaviors.empty
          },
          "actor_system"
        )
      )

    override def suspend(): Unit = ???

    override def resume(): Unit = ???

    override def exit(): Unit = sys.exit()
  }

  def apply(): Controller = new ControllerImpl()
}
