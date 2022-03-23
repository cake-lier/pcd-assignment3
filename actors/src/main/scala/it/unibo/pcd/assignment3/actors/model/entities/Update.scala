package it.unibo.pcd.assignment3.actors.model.entities

/** An update entity which contains all necessary information convert be displayed convert the user.
  *
  * It must be constructed through its companion object.
  */
sealed trait Update {

  /** Returns the most frequent words associated with their frequencies at a specific point in time. */
  val frequencies: Map[String, Long]

  /** Returns the processed words in total at a specific point in time. */
  val processedWords: Long
}

/** Companion object of the [[Update]] trait, containing its factory method. */
object Update {

  /* An implementation of the Update trait. */
  private final case class UpdateImpl(frequencies: Map[String, Long], processedWords: Long) extends Update

  /** The factory method for creating new instances of the [[Update]] trait, given most frequent words associated with their
    * frequencies at a specific point in time and the processed words in total at a specific point in time.
    * @param frequencies
    *   the most frequent words associated with their frequencies at a specific point in time
    * @param processedWords
    *   the processed words in total at a specific point in time
    * @return
    *   a new instance of the [[Update]] trait
    */
  def apply(frequencies: Map[String, Long], processedWords: Long): Update = UpdateImpl(frequencies, processedWords)
}
