package it.unibo.pcd.assignment3.model.entities

/** An update entity which contains all necessary information to be displayed to the user.
  */
trait Update {

  /** Returns the most frequent words associated with their frequencies at a specific point in time. */
  val frequencies: Map[String, Long]

  /** Returns the processed words in total at a specific point in time. */
  val processedWords: Long
}

object Update {

  private final case class UpdateImpl(frequencies: Map[String, Long], processedWords: Long) extends Update

  def apply(frequencies: Map[String, Long], processedWords: Long): Update = UpdateImpl(frequencies, processedWords)
}
