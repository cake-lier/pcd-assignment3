package it.unibo.pcd.assignment3.controller

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

  def apply(): Controller = ???
}
