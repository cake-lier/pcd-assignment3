package it.unibo.pcd.assignment3.puzzlermi.view;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;

/**
 * The entity responsible for managing the selection of the [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]]s in the
 * grid-like view that displays the puzzle.
 */
public interface SelectionManager {

    /**
     * Allows the player to select a {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} given the {@link Position} of it into
     * the grid-like view that displays the puzzle. The origin of the {@link Position} is in the top-left corner, the x-axis
     * points right and the y-axis points down. After selecting two tiles, the selection is cleared and the player can start
     * selecting {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile}s again. This is because when two
     * {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile}s are selected we assume the player wants to swap those two. So the
     * {@link it.unibo.pcd.assignment3.puzzlermi.controller.Controller} gets notified and the {@link View} cleared.
     * @param position the {@link Position} in the grid that displays the puzzle of the
     *                 {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} to select
     */
    void selectPosition(Position position);

    /**
     * Clears the {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} selection previously made by the user, returning the view
     * to its original unaltered state.
     */
    void clearSelection();
}
