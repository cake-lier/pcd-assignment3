package it.unibo.pcd.assignment3.puzzlermi.view;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;

public interface SelectionManager {

    void selectPosition(Position position);

    void clearSelection();
}
