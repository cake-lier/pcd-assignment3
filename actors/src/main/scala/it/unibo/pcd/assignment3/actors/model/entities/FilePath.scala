package it.unibo.pcd.assignment3.actors.model.entities

import java.nio.file.Path

sealed trait FilePath {

  val path: Path
}

object FilePath {

  private final case class FilePathImpl(path: Path) extends FilePath

  def apply(path: Path): FilePath = FilePathImpl(path)
}
