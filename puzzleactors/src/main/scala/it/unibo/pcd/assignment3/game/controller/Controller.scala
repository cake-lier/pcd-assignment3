package it.unibo.pcd.assignment3.game.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import it.unibo.pcd.assignment3.Operations.AnyOps
import it.unibo.pcd.assignment3.game.model.{Actor, Board, Card}
import it.unibo.pcd.assignment3.game.view.{PuzzleBoard, View}

object Controller {
  def apply(rows: Int, columns: Int, create: Boolean = false): Behavior[Message] = Behaviors.setup { ctx =>
    if (create) {
      val board = Board.randomNonEndedBoard(rows, columns)
      val player = ctx.spawnAnonymous[Message](Player(ctx.self, board))
      ControllerWithBoard(board, player)
    } else {
      val player = ctx.spawnAnonymous[Message](Player(ctx.self))
      EmptyController(player)
    }
  }
}

object EmptyController {
  def apply(player: Actor): Behavior[Message] = Behaviors.receiveMessagePartial {
    case NewBoard(board) => ControllerWithBoard(board, player)
    case _               => Behaviors.same
  }
}

object ControllerWithBoard {
  def apply(entryBoard: Board, player: Actor): Behavior[Message] = Behaviors.setup { ctx =>
    val view = ctx.spawnAnonymous[Message](View(entryBoard.rows, entryBoard.columns, ctx.self))

    view ! NewBoard(entryBoard)
    if (Board.completed(entryBoard)) {
      view ! GameEnded()
    }

    def behavior(gameStatus: Board, selectedCard: Option[Card], ignore: Boolean = false): Behavior[Message] =
      Behaviors.receiveMessagePartial {
        case SelectCard(c) =>
          selectedCard match {
            case Some(s) if c === s =>
              behavior(gameStatus, None)
            case Some(s) =>
              player ! Move(s, c)
              behavior(gameStatus, None, ignore = true)
            case _ => behavior(gameStatus, Some(c))
          }
        case g: NewBoard =>
          view ! g
          if (Board.completed(g.board)) {
            view ! GameEnded()
            Behaviors.same
          } else {
            behavior(g.board, None)
          }
      }
    behavior(entryBoard, None)
  }
}
