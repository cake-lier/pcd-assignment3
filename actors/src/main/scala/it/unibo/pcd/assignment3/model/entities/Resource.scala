package it.unibo.pcd.assignment3.model.entities

sealed trait Resource {

  val page: Page

  val stopwordsSet: StopwordsSet
}

object Resource {

  private final case class ResourceImpl(page: Page, stopwordsSet: StopwordsSet) extends Resource

  def apply(page: Page, stopwordsSet: StopwordsSet): Resource = ResourceImpl(page, stopwordsSet)
}
