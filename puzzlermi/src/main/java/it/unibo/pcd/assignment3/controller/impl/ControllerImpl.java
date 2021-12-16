package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.Controller;
import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteSystem;
import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.model.impl.PuzzleBoardImpl;
import it.unibo.pcd.assignment3.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

class ControllerImpl implements Controller {
    private final RemoteSystem remoteSystem;

    ControllerImpl(final int rows, final int columns, final View view, final Peer self)
        throws AlreadyBoundException, RemoteException {
        this.remoteSystem = new RemoteSystemImpl(self, view, new PuzzleBoardImpl(rows, columns));
    }

    ControllerImpl(final View view, final Peer self, final Peer buddy)
        throws AlreadyBoundException, RemoteException, NotBoundException {
        this.remoteSystem = new RemoteSystemImpl(self, buddy, view);
    }

    @Override
    public void exit() {
        // Deregister from system
        System.exit(0);
    }

    @Override
    public List<Tile> getTiles() {
        return this.remoteSystem.getTiles();
    }

    @Override
    public void swap(final Tile firstTile, final Tile secondTile) {
        this.remoteSystem.requestSwap(firstTile, secondTile);
    }
}
