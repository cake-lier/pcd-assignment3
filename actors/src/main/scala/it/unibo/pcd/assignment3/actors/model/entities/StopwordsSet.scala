package it.unibo.pcd.assignment3.actors.model.entities

/** A set of stopwords, the words to be ignored during the analysis of the documents for finding the frequency of the most
  * recurrent words and the total number of processed words.
  *
  * It must be constructed through its companion object.
  */
sealed trait StopwordsSet {

  /** Returns the wrapped [[Set]] of [[String]]s which represent the stopwords. */
  val stopwords: Set[String]
}

/** Companion object to the [[StopwordsSet]] trait, containing its factory method. */
object StopwordsSet {

  /* An implementation of the StopwordsSet trait. */
  private final case class StopwordsSetImpl(stopwords: Set[String]) extends StopwordsSet

  /** The factory method for creating a new instance of the [[StopwordsSet]] trait given the [[Set]] of [[String]]s which
    * represent the stopwords themselves.
    * @param stopwords
    *   the [[Set]] of [[String]]s which represent the stopwords themselves
    * @return
    *   a new instance of the [[StopwordsSet]] trait
    */
  def apply(stopwords: Set[String]): StopwordsSet = StopwordsSetImpl(stopwords)
}
