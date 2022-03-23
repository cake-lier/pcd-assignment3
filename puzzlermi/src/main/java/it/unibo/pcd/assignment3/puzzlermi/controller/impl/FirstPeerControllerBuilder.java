package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

/**
 * A builder for creating a new instance of the {@link Controller} interface to be used when it is associated with the first
 * {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} in a game session.
 */
public class FirstPeerControllerBuilder {
    private static final String ALREADY_BUILT_ERROR = "This builder has already built its object";

    private final String localHost;
    private final int rows;
    private final int columns;
    private final View view;
    private int localPort;
    private boolean built;

    /**
     * Default constructor.
     * @param localHost the hostname of the {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} associated with this
     *                  application
     * @param rows the number of rows of the {@link it.unibo.pcd.assignment3.puzzlermi.model.PuzzleBoard}
     * @param columns the number of columns of the {@link it.unibo.pcd.assignment3.puzzlermi.model.PuzzleBoard}
     * @param view the {@link View} component to be usd by the built {@link Controller}
     */
    public FirstPeerControllerBuilder(final String localHost,
                                      final int rows,
                                      final int columns,
                                      final View view) {
        this.localHost = Objects.requireNonNull(localHost);
        this.rows = rows;
        this.columns = columns;
        this.view = Objects.requireNonNull(view);
        this.localPort = 1099;
        this.built = false;
    }

    /**
     * Sets the port of the {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} associated with this application.
     * @param localPort the value of the port to set
     * @return this {@link FirstPeerControllerBuilder} instance
     */
    public FirstPeerControllerBuilder setLocalPort(final int localPort) {
        this.localPort = localPort;
        return this;
    }

    /**
     * Builds a new instance of {@link Controller}.
     * @return a new instance of {@link Controller}
     * @throws RemoteException if it could not create the RMI registry or export or bind the stub of the
     *                         {@link it.unibo.pcd.assignment3.puzzlermi.controller.RemoteGatekeeper}
     */
    public Controller build() throws RemoteException {
        if (this.built) {
            throw new IllegalArgumentException(ALREADY_BUILT_ERROR);
        }
        this.built = true;
        return new ControllerImpl(
            this.rows,
            this.columns,
            this.view,
            new PeerImpl(this.localHost, this.localPort)
        );
    }

    @Override
    public String toString() {
        return "FirstPeerControllerBuilder{localHost='" + this.localHost + "', rows=" + this.rows + ", columns=" + this.columns
               + ", view=" + this.view + ", localPort=" + this.localPort + ", built=" + this.built + "}";
    }
}
