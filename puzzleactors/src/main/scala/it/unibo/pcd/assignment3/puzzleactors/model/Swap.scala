package it.unibo.pcd.assignment3.puzzleactors.model

/** A swap between two [[Tile]]s, a move that the player can do to solve the puzzle.
  *
  * It must be constructed through its companion object.
  */
trait Swap {

  /** Returns the [[Position]] of the first [[Tile]] to swap. */
  val firstPosition: Position

  /** Returns the [[Position]] of the second [[Tile]] to swap. */
  val secondPosition: Position
}

/** Companion object to the [[Swap]] trait, containing its factory and extractor methods. */
object Swap {

  /* An implementation of the Swap trait. */
  private final case class SwapImpl(firstPosition: Position, secondPosition: Position) extends Swap

  /** The factory method for creating a new instance of the [[Swap]] trait given the [[Position]]s of the two [[Tile]]s to be
    * swapped.
    * @param firstPosition
    *   the [[Position]] of the first [[Tile]] to swap
    * @param secondPosition
    *   the [[Position]] of the second [[Tile]] to swap
    * @return
    *   a new instance of the [[Swap]] trait
    */
  def apply(firstPosition: Position, secondPosition: Position): Swap = SwapImpl(firstPosition, secondPosition)

  /** The extractor method for destructuring a [[Swap]] instance into its components: the [[Position]]s of the two [[Tile]]s to
    * swap.
    * @param swap
    *   the instance of [[Swap]] to destructure
    * @return
    *   an [[Option]] containing a [[Tuple2]] with the components of the given [[Swap]] instance
    */
  def unapply(swap: Swap): Option[(Position, Position)] = Some((swap.firstPosition, swap.secondPosition))
}
