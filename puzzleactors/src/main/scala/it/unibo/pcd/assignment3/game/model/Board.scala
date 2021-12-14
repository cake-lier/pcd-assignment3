package it.unibo.pcd.assignment3.game.model

import it.unibo.pcd.assignment3.Operations.AnyOps

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.Random

trait Board {
  val cards: Seq[Card]
  val rows: Int
  val columns: Int
}
object Board {
  private final case class BoardImpl(cards: Seq[Card], rows: Int, columns: Int) extends Board

  def apply(cards: Seq[Card], rows: Int, columns: Int): Board = BoardImpl(cards, rows, columns)

  implicit def toSeq(board: Board): Seq[Card] = board.cards

  val empty: Board = Board(Seq.empty[Card], 0, 0)

  def swap(board: Board, index1: Card, index2: Card): Board = {
    val temp = board(index1)
    copy(board, board.updated(index1, board(index2)).updated(index2, temp))
  }

  def copy(board: Board, cards: Seq[Card]): Board = Board(cards, board.rows, board.columns)

  def completed(board: Board): Boolean = board.cards === board.cards.sorted

  @tailrec
  def randomNonEndedBoard(rows: Int, columns: Int): Board = {
    val board = Board(Random.shuffle((0 until rows * columns).toList), rows, columns)
    if ((rows === 1 && columns === 1) || !completed(board)) board else randomNonEndedBoard(rows, columns)
  }
}
