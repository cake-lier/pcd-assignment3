package it.unibo.pcd.assignment3.actors.model.entities

/** A page entity as conceived into the problem space. */
sealed trait Page {

  /** Returns the textual content of this page. */
  val text: String
}

object Page {

  private final case class PageImpl(text: String) extends Page

  def apply(text: String): Page = PageImpl(text)
}
