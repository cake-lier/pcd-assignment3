package it.unibo.pcd.assignment3.puzzleactors.view

import it.unibo.pcd.assignment3.puzzleactors.controller.Controller
import it.unibo.pcd.assignment3.puzzleactors.model.Position
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.AnyOps

trait SelectionManager {

  def clearSelection(): Unit

  def selectPosition(position: Position): Unit
}

object SelectionManager {

  private class SelectionManagerImpl(controller: Controller) extends SelectionManager {
    private var selectedPosition: Option[Position] = None

    override def clearSelection(): Unit = selectedPosition = None

    override def selectPosition(position: Position): Unit =
      selectedPosition.filter(_ !== position) match {
        case Some(p) =>
          controller.swap(p, position)
          clearSelection()
        case _ => selectedPosition = Some(position)
      }
  }

  def apply(controller: Controller): SelectionManager = new SelectionManagerImpl(controller)
}
