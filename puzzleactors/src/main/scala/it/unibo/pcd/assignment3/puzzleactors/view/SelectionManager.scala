package it.unibo.pcd.assignment3.puzzleactors.view

import it.unibo.pcd.assignment3.puzzleactors.controller.Controller
import it.unibo.pcd.assignment3.puzzleactors.model.Position
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps

/** The entity responsible for managing the selection of the [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]]s in the
  * grid-like view that displays the puzzle.
  *
  * It must be constructed through its companion object.
  */
trait SelectionManager {

  /** Clears the [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] selection previously made by the user, returning the view to
    * its original unaltered state.
    */
  def clearSelection(): Unit

  /** Allows the player to select a [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] given the position of it into the
    * grid-like view that displays the puzzle. The origin of the [[Position]] is in the top-left corner, the x-axis points right
    * and the y-axis points down. After selecting two tiles, the selection is cleared and the player can start selecting
    * [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]]s again. This is because when two
    * [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]]s are selected we assume the player wants to swap those two. So the
    * [[Controller]] gets notified and the [[View]] cleared.
    * @param position
    *   the [[Position]] in the grid that displays the puzzle of the [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] to
    *   select
    */
  def selectPosition(position: Position): Unit
}

/** Companion object to the [[SelectionManager]] trait, containing its factory method. */
object SelectionManager {

  /* An implementation of the SelectionManager trait. */
  private class SelectionManagerImpl(controller: Controller) extends SelectionManager {
    private var selectedPosition: Option[Position] = None

    override def clearSelection(): Unit = selectedPosition = None

    override def selectPosition(position: Position): Unit =
      selectedPosition.filter(_ =/= position) match {
        case Some(p) =>
          controller.swap(p, position)
          clearSelection()
        case _ => selectedPosition = Some(position)
      }
  }

  /** The factory method for creating a new instance of the [[SelectionManager]] trait given the [[Controller]] component to be
    * notified when the player selects two [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]]s.
    * @param controller
    *   the [[Controller]] component of this application
    * @return
    *   a new instance of the [[SelectionManager]] trait
    */
  def apply(controller: Controller): SelectionManager = new SelectionManagerImpl(controller)
}
