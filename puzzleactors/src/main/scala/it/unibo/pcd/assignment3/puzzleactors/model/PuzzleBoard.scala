package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.PuzzleBoard.PuzzleBoardImpl

import scala.util.Random

/** The board of a puzzle, containing all of its [[Tile]]s in the current arrangement.
  *
  * It must be constructed through its companion object.
  */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PuzzleBoardImpl], name = "impl")
  )
)
trait PuzzleBoard {

  /** Returns all the [[Tile]]s that makes up the puzzle, ordered from the top left to the bottom right one in the current
    * arrangement.
    */
  val tiles: Seq[Tile]

  /** Applies the given swap to this board.
    * @param swap
    *   the [[Swap]] to be applied
    * @return
    *   this [[PuzzleBoard]] with the given [[Swap]] applied
    */
  def swap(swap: Swap): PuzzleBoard

  /** Returns whether the current arrangement of [[Tile]]s constitutes a solution to the puzzle. */
  def isSolution: Boolean
}

/** Companion object to the [[PuzzleBoard]] trait, containing its factory methods. */
object PuzzleBoard {

  /* An implementation of the PuzzleBoard trait. */
  private[model] final case class PuzzleBoardImpl(tiles: Seq[Tile]) extends PuzzleBoard {

    override def swap(swap: Swap): PuzzleBoard =
      tiles
        .find(_.currentPosition === swap.firstPosition)
        .flatMap { t1 =>
          tiles
            .find(_.currentPosition === swap.secondPosition)
            .map { t2 =>
              PuzzleBoardImpl(
                tiles
                  .diff(Seq(t1, t2)) ++
                  Seq(Tile(t1.originalPosition, swap.secondPosition), Tile(t2.originalPosition, swap.firstPosition))
              )
            }
        }
        .getOrElse(this)

    override def isSolution: Boolean = tiles.forall(_.isInRightPlace)
  }

  /** The factory method for creating a new instance of the [[PuzzleBoard]] trait with a random arrangement of [[Tile]]s, given
    * the dimensions of the created [[PuzzleBoard]].
    * @param rows
    *   the number of rows of the created [[PuzzleBoard]]
    * @param columns
    *   the number of columns of the created [[PuzzleBoard]]
    * @return
    *   a new instance of the [[PuzzleBoard]] trait
    */
  def apply(rows: Int, columns: Int): PuzzleBoard =
    PuzzleBoard(
      Random
        .shuffle((0 until rows).flatMap(r => (0 until columns).map(Position(_, r))))
        .zipWithIndex
        .map(t => Tile(t._1, Position(t._2 % columns, t._2 / columns)))
    )

  /** The factory method for creating a new instance of the [[PuzzleBoard]] trait given the [[Tile]]s which constitutes the puzzle
    * ordered following the initial arrangement for the created [[PuzzleBoard]].
    * @param tiles
    *   the [[Tile]]s that should be contained into this [[PuzzleBoard]]
    * @return
    *   a new instance of the [[PuzzleBoard]] trait
    */
  def apply(tiles: Seq[Tile]): PuzzleBoard = PuzzleBoardImpl(tiles)
}
