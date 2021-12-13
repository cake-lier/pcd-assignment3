package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.AddressBook;
import it.unibo.pcd.assignment3.controller.Controller;
import it.unibo.pcd.assignment3.controller.RemoteLock;
import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.model.impl.PuzzleBoardImpl;
import it.unibo.pcd.assignment3.view.View;

import java.rmi.registry.LocateRegistry;
import java.util.Objects;

class ControllerImpl implements Controller {
    private final PuzzleBoard board;
    private final View view;
    private final AddressBook addressBook;

    ControllerImpl(final int rows, final int columns, final View view, final AddressBook addressBook) {
        this.board = new PuzzleBoardImpl(rows, columns);
        this.view = Objects.requireNonNull(view);
        this.addressBook = Objects.requireNonNull(addressBook);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public void swap(final Tile firstTile, final Tile secondTile) {
        this.addressBook.getPeers().forEach(p -> {
            final var registry = LocateRegistry.getRegistry(p.getHost(), p.getPort());
            final var sharedSemaphore = ((RemoteLock) registry.lookup("SharedPuzzleBoard"));
            sharedSemaphore.acquireLock(self);
        });
        this.board.swap(firstTile, secondTile);
        this.view.displayTiles(this.board.getTiles());
    }
}
