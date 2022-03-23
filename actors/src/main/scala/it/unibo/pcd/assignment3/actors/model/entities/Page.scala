package it.unibo.pcd.assignment3.actors.model.entities

/** A page entity as conceived into the problem space.
  *
  * It must be constructed through its companion object.
  */
sealed trait Page {

  /** Returns the textual content of this page. */
  val text: String
}

/** Companion object of the [[Page]] trait, containing its factory method. */
object Page {

  /* An implementation of the Page trait. */
  private final case class PageImpl(text: String) extends Page

  /** The factory method for creating new instances of the [[Page]] trait, given its textual content of the page itself.
    * @param text
    *   the textual content of this page
    * @return
    *   a new instance of the [[Page]] trait
    */
  def apply(text: String): Page = PageImpl(text)
}
