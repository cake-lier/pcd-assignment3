package it.unibo.pcd.assignment3.actors.controller

import akka.actor.typed.ActorSystem
import it.unibo.pcd.assignment3.actors.controller.actors._
import it.unibo.pcd.assignment3.actors.model.entities.FilePath
import it.unibo.pcd.assignment3.actors.view.View

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

  private class ControllerImpl(view: View) extends Controller {
    private var actorSystem: Option[ActorSystem[Command]] = None
    private val suspendedFlag: SuspendedFlag = SuspendedFlag()
    private val totalActors: Int = (Runtime.getRuntime.availableProcessors * 1.0f * (1 + 1.093f)).round
    private val executor: ExecutionContext =
      ExecutionContext.fromExecutor(new SuspendableForkJoinPool(totalActors, suspendedFlag))

    override def launch(filesDirectory: Path, stopwordsFile: Path, wordsNumber: Int): Unit = {
      suspendedFlag.resume()
      actorSystem.foreach(_.terminate())
      actorSystem = Some(
        ActorSystem(
          RootActor(FilePath(filesDirectory), FilePath(stopwordsFile), wordsNumber, view, executor, totalActors),
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
