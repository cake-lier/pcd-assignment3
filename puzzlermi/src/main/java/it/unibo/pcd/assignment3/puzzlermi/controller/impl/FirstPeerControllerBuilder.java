package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

public class FirstPeerControllerBuilder {
    private final String localHost;
    private final int rows;
    private final int columns;
    private final View view;
    private int localPort;
    private boolean built;

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

    public FirstPeerControllerBuilder setLocalPort(final int localPort) {
        this.localPort = localPort;
        return this;
    }

    public Controller build() throws RemoteException, AlreadyBoundException {
        if (this.built) {
            throw new IllegalArgumentException("This builder has already built its object");
        }
        this.built = true;
        return new ControllerImpl(
            this.rows,
            this.columns,
            this.view,
            new PeerImpl(this.localHost, this.localPort)
        );
    }
}
