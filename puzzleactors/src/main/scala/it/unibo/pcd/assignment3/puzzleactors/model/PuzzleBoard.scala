package it.unibo.pcd.assignment3.puzzleactors.model

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps
import it.unibo.pcd.assignment3.puzzleactors.model.PuzzleBoard.PuzzleBoardImpl

import scala.util.Random

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PuzzleBoardImpl], name = "impl")
  )
)
trait PuzzleBoard {

  val tiles: Seq[Tile]

  def swap(firstPosition: Position, secondPosition: Position): PuzzleBoard

  def isSolution: Boolean
}

object PuzzleBoard {

  private[model] final case class PuzzleBoardImpl(tiles: Seq[Tile]) extends PuzzleBoard {

    override def swap(firstPosition: Position, secondPosition: Position): PuzzleBoard =
      tiles
        .find(_.currentPosition === firstPosition)
        .flatMap { t1 =>
          tiles
            .find(_.currentPosition === secondPosition)
            .map { t2 =>
              PuzzleBoardImpl(
                tiles
                  .diff(Seq(t1, t2)) ++
                  Seq(Tile(t1.originalPosition, secondPosition), Tile(t2.originalPosition, firstPosition))
              )
            }
        }
        .getOrElse(this)

    override def isSolution: Boolean = tiles.forall(_.isInRightPlace)
  }

  def apply(rows: Int, columns: Int): PuzzleBoard =
    PuzzleBoard(
      Random
        .shuffle((0 until rows).flatMap(r => (0 until columns).map(Position(_, r))))
        .zipWithIndex
        .map(t => Tile(t._1, Position(t._2 % columns, t._2 / columns)))
    )

  def apply(tiles: Seq[Tile]): PuzzleBoard = PuzzleBoardImpl(tiles)
}