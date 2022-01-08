package it.unibo.pcd.assignment3.actors.controller.actors

import akka.actor.typed.ActorRef
import it.unibo.pcd.assignment3.actors.model.entities._
import org.apache.pdfbox.pdmodel.PDDocument

import java.nio.file.Path
import scala.language.implicitConversions

sealed trait Command

object Command {

  final case class FilePathCommand(path: Path) extends Command

  final case class StopwordsSetCommand(stopwordsSet: StopwordsSet, replyTo: ActorRef[Command]) extends Command

  final case class DocumentCommand(document: PDDocument) extends Command

  final case class PageCommand(text: String) extends Command

  final case class UpdateCommand(frequencies: Map[String, Long], processedWords: Long) extends Command

  final case class StopwordsAck(sentFrom: ActorRef[Command]) extends Command

  case object PoisonPill extends Command

  final case class Available(actor: ActorRef[Command]) extends Command

  case object TimerExpired extends Command

  case object Ready extends Command
}
