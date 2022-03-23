package it.unibo.pcd.assignment3.actors.model.entities

/** A resource to be processed by a [[it.unibo.pcd.assignment3.actors.model.tasks.PageFilterTask]], consisting of a [[Page]] and
  * the [[StopwordsSet]] to be used for processing that page.
  *
  * It must be constructed through its companion object.
  */
sealed trait Resource {

  /** Returns the [[Page]] associated with this resource. */
  val page: Page

  /** Returns the [[StopwordsSet]] associated with this resource. */
  val stopwordsSet: StopwordsSet
}

/** Companion object to the [[Resource]] trait, containing its factory method. */
object Resource {

  /* An implementation of the Resource trait. */
  private final case class ResourceImpl(page: Page, stopwordsSet: StopwordsSet) extends Resource

  /** The factory method for creating new instances of the [[Resource]] trait given the [[Page]] and the [[StopwordsSet]]
    * associated with the one to be created.
    * @param page
    *   the [[Page]] associated with the [[Resource]] to be created
    * @param stopwordsSet
    *   the [[StopwordsSet]] associated with the [[Resource]] to be created
    * @return
    *   a new instance of the [[Resource]] trait
    */
  def apply(page: Page, stopwordsSet: StopwordsSet): Resource = ResourceImpl(page, stopwordsSet)
}
