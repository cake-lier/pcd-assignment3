package it.unibo.pcd.assignment3.model.entities

sealed trait StopwordsSet {

  val stopwords: Set[String]
}

object StopwordsSet {

  private final case class StopwordsSetImpl(stopwords: Set[String]) extends StopwordsSet

  def apply(stopwords: Set[String]): StopwordsSet = StopwordsSetImpl(stopwords)
}
