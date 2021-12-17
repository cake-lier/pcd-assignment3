package it.unibo.pcd.assignment3.view;

import it.unibo.pcd.assignment3.model.Position;

public interface SelectionManager {

    void selectPosition(Position position);

    void clearSelection();
}
