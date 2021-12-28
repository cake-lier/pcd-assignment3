package it.unibo.pcd.assignment3.puzzlermi.view.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.view.SelectionManager;

import java.util.Optional;

public class SelectionManagerImpl implements SelectionManager {
    private final Controller controller;
	private Optional<Position> selectedPosition;

    public SelectionManagerImpl(final Controller controller) {
        this.controller = controller;
        this.selectedPosition = Optional.empty();
    }

	@Override
    public void selectPosition(final Position position) {
		if (this.selectedPosition.isPresent() && !this.selectedPosition.get().equals(position)) {
            this.controller.swap(this.selectedPosition.get(), position);
            this.clearSelection();
		} else {
			this.selectedPosition = Optional.of(position);
		}
	}

    @Override
    public void clearSelection() {
        this.selectedPosition = Optional.empty();
    }
}
