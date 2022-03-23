package it.unibo.pcd.assignment3.puzzlermi.view;

import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

import java.util.List;

/**
 * The View component of this application. It should capture user input and be notified of changes into the Model component which
 * should appear to the user.
 */
public interface View {

    /**
     * Shows a new arrangement of {@link Tile}s into the grid-like view which displays the puzzle. The {@link Tile}s are arranged
     * following their current position.
     * @param tiles the new arrangement of {@link Tile}s to be displayed
     */
    void displayTiles(List<Tile> tiles);

    /**
     * Displays the message to the player indicating the completion of the game.
     */
    void displaySolution();
}
