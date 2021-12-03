package it.unibo.pcd.assignment3.controller.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.Receptionist
import it.unibo.pcd.assignment3.model.entities._
import org.apache.pdfbox.pdmodel.PDDocument

import java.nio.file.Path
import scala.language.implicitConversions

sealed trait Command

object Command {

  final case class FilePathCommand(path: Path) extends Command

  final case class StopwordsSetCommand(stopwordsSet: StopwordsSet, replyTo: ActorRef[Command]) extends Command

  final case class DocumentCommand(document: PDDocument) extends Command

  final case class PageCommand(text: String) extends Command

  final case class ResourceCommand(page: Page, stopwordsSet: StopwordsSet) extends Command

  final case class UpdateCommand(frequencies: Map[String, Long], processedWords: Long) extends Command

  case object StopwordsAck extends Command

  case object PoisonPill extends Command

  final case class Available(actor: ActorRef[Command]) extends Command

  case object TimerExpired extends Command
}
