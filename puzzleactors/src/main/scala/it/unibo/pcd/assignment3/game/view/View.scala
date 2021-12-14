package it.unibo.pcd.assignment3.game.view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.assignment3.game.controller.{GameEnded, Message, NewBoard, Stop}
import it.unibo.pcd.assignment3.game.model.Actor

import java.awt.Image
import javax.imageio.ImageIO
import javax.swing._
import scala.util.{Failure, Success, Try}

object View {
  def apply(rows: Int, columns: Int, controller: Actor): Behavior[Message] = {
    Try(ImageIO.read(ClassLoader.getSystemResource("bletchley-park-mansion.jpg"))) match {
      case Success(image) => successPuzzleBoard(rows, columns, image, controller)
      case Failure(_)     => failurePuzzleBoard(controller)
    }
  }

  private def successPuzzleBoard(rows: Int, columns: Int, image: Image, controller: Actor): Behavior[Message] =
    Behaviors.setup { _ =>
      val view = PuzzleBoard(rows, columns, image, controller)
      Behaviors.receiveMessagePartial {
        case NewBoard(board) =>
          view.printBoard(board)
          Behaviors.same
        case GameEnded() =>
          view.showGameCompletion()
          Behaviors.stopped
      }
    }
  private def failurePuzzleBoard(controller: Actor): Behavior[Message] = Behaviors.setup { _ =>
    new JFrame() {
      JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE)
      controller ! Stop()
    }
    Behaviors.stopped
  }
}
