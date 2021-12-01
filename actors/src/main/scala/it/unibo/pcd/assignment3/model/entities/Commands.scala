package it.unibo.pcd.assignment3.model.entities

import akka.actor.typed.ActorRef

import java.nio.file.Path

sealed trait Command

sealed trait FilePath extends Command {

  val path: Path
}

object FilePath {

  private final case class FilePathImpl(path: Path) extends FilePath

  def apply(path: Path): FilePath = FilePathImpl(path)
}

sealed trait StopwordsSet extends Command {

  val stopwords: Set[String]
}

object StopwordsSet {

  private final case class StopwordsSetImpl(stopwords: Set[String]) extends StopwordsSet

  def apply(stopwords: Set[String]): StopwordsSet = StopwordsSetImpl(stopwords)
}

/** A page entity as conceived into the problem space. */
sealed trait Page extends Command {

  /** Returns the textual content of this page. */
  val text: String
}

object Page {

  private final case class PageImpl(text: String) extends Page

  def apply(text: String): Page = PageImpl(text)
}

sealed trait Resource extends Command {

  val page: Page

  val stopwordsSet: StopwordsSet
}

object Resource {

  private final case class ResourceImpl(page: Page, stopwordsSet: StopwordsSet) extends Resource

  def apply(page: Page, stopwordsSet: StopwordsSet): Resource = ResourceImpl(page, stopwordsSet)
}

/** An update entity which contains all necessary information to be displayed to the user.
  */
sealed trait Update extends Command {

  /** Returns the most frequent words associated with their frequencies at a specific point in time. */
  val frequencies: Map[String, Long]

  /** Returns the processed words in total at a specific point in time. */
  val processedWords: Long
}

object Update {

  private final case class UpdateImpl(frequencies: Map[String, Long], processedWords: Long) extends Update

  def apply(frequencies: Map[String, Long], processedWords: Long): Update = UpdateImpl(frequencies, processedWords)
}

sealed trait PoisonPill extends Command {

  val forward: Boolean
}

object PoisonPill {

  private final case class PoisonPillImpl(forward: Boolean) extends PoisonPill

  def apply(forward: Boolean): PoisonPill = PoisonPillImpl(forward)

  def unapply(poisonPill: PoisonPill): Option[Boolean] = Some(poisonPill.forward)
}

sealed trait Availability extends Command{

  val actor: ActorRef[Command]
}

object Availability{

  private final case class AvailabilityImpl(actor: ActorRef[Command]) extends Availability

  def apply(actor: ActorRef[Command]): Availability= AvailabilityImpl(actor)

  def unapply(availability: Availability): Option[ActorRef[Command]] = Some(availability.actor)

}
