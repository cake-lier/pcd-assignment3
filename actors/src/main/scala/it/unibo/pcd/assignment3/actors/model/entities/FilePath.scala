package it.unibo.pcd.assignment3.actors.model.entities

import java.nio.file.Path

/** A path to a file into the filesystem of the system in which this application runs.
  *
  * It must be constructed through its companion object.
  */
sealed trait FilePath {

  /** Returns the [[Path]] to the file wrapped by this instance. */
  val path: Path
}

/** Companion object to the [[FilePath]] trait, containing its factory method. */
object FilePath {

  /* Implementation to the FilePath trait. */
  private final case class FilePathImpl(path: Path) extends FilePath

  /** The factory method for creating new instances of the [[FilePath]] trait given the [[Path]] to the file that the created
    * instance has to wrap.
    * @param path
    *   the [[Path]] to the file that the created instance has to wrap
    * @return
    *   a new instance of the [[FilePath]] trait
    */
  def apply(path: Path): FilePath = FilePathImpl(path)
}
