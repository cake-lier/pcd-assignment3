package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.actors.model.entities._
import org.apache.pdfbox.pdmodel.PDDocument

import java.nio.file.Path
import scala.language.implicitConversions

/** A command that an actor can send to another into the system.
  *
  * The only possible instances are defined through its companion object.
  */
sealed trait Command

/** Companion object to the [[Command]] trait, containing all of its possible instances. */
object Command {

  /** A [[Command]] for wrapping the content of a [[FilePath]], a [[Path]] to a file.
    *
    * @param path
    *   the [[Path]] to a file this [[Command]] has to wrap
    */
  final case class FilePathCommand(path: Path) extends Command

  /** A [[Command]] for receiving a [[StopwordsSet]] along with the sender of the [[Command]] itself.
    *
    * @param stopwordsSet
    *   the [[StopwordsSet]] to be sent with this [[Command]]
    * @param replyTo
    *   the sender of this [[Command]]
    */
  final case class StopwordsSetCommand(stopwordsSet: StopwordsSet, replyTo: ActorRef[Command]) extends Command

  /** A [[Command]] for wrapping the content of a [[Document]], a document as specified by the Apache PDFBox library.
    *
    * @param document
    *   the Apache PDFBox document this [[Command]] has to wrap
    */
  final case class DocumentCommand(document: PDDocument) extends Command

  /** A [[Command]] for wrapping the content of a [[Page]], a [[String]] representing its textual content.
    *
    * @param text
    *   the [[String]] this command has to wrap
    */
  final case class PageCommand(text: String) extends Command

  /** A [[Command]] for wrapping the content of an [[Update]], a [[Map]] associating each word found to its frequency and the
    * number of words processed for each page.
    *
    * @param frequencies
    *   the [[Map]] associating each word found to its frequency this [[Command]] has to wrap
    * @param processedWords
    *   the number of words processed this [[Command]] has to wrap
    */
  final case class UpdateCommand(frequencies: Map[String, Long], processedWords: Long) extends Command

  /** A command to be sent as a response to a [[StopwordsSetCommand]] containing the reference to the actor sending it.
    *
    * @param sentFrom
    *   the actor sending this message
    */
  final case class StopwordsAck(sentFrom: ActorRef[Command]) extends Command

  /** A command for telling an actor that no more input resources will be sent from now on and, when it has completed its
    * operations, it should cease its execution.
    */
  case object PoisonPill extends Command

  /** A command to be used by a worker actor to tell its coordinator that is now available for executing a new job, being this the
    * first time or because it has completed its current task.
    *
    * @param actor
    *   the actor now available sending this command
    */
  final case class Available(actor: ActorRef[Command]) extends Command

  /** A command to be self-sent to an actor when the associated timer has expired. */
  case object TimerExpired extends Command

  /** A command to be sent when an actor is now ready to do something, for whatever reason. */
  case object Ready extends Command
}
