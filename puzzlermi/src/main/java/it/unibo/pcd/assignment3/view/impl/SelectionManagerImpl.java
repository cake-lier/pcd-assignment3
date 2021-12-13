package it.unibo.pcd.assignment3.view.impl;

import it.unibo.pcd.assignment3.controller.Controller;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.view.SelectionManager;

import java.util.Optional;

public class SelectionManagerImpl implements SelectionManager {
    private final Controller controller;
	private Optional<Tile> selectedTile;

    public SelectionManagerImpl(final Controller controller) {
        this.controller = controller;
        this.selectedTile = Optional.empty();
    }

	@Override
    public void selectTile(final Tile tile, final Runnable onSwapPerformed) {
		if (this.selectedTile.isPresent()) {
            if (!this.selectedTile.get().equals(tile)) {
                this.controller.swap(this.selectedTile.get(), tile);
            }
            onSwapPerformed.run();
            this.selectedTile = Optional.empty();
		} else {
			this.selectedTile = Optional.of(tile);
		}
	}
}
