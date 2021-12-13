package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.Controller;
import it.unibo.pcd.assignment3.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

public class ExtraPeerControllerBuilder {
    private final String localHost;
    private final String remoteHost;
    private final int rows;
    private final int columns;
    private final View view;
    private int remotePort;
    private int localPort;
    private boolean built;

    public ExtraPeerControllerBuilder(final String localHost,
                                      final String remoteHost,
                                      final int rows,
                                      final int columns,
                                      final View view) {
        this.localHost = Objects.requireNonNull(localHost);
        this.remoteHost = Objects.requireNonNull(remoteHost);
        this.rows = rows;
        this.columns = columns;
        this.view = Objects.requireNonNull(view);
        this.remotePort = 1099;
        this.localPort = 1099;
        this.built = false;
    }

    public ExtraPeerControllerBuilder setRemotePort(final int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public ExtraPeerControllerBuilder setLocalPort(final int localPort) {
        this.localPort = localPort;
        return this;
    }

    public Controller build() throws RemoteException, AlreadyBoundException, NotBoundException {
        // What to do with the puzzle?
        if (this.built) {
            throw new IllegalArgumentException("This builder has already built its object");
        }
        this.built = true;
        return new ControllerImpl(
            this.rows,
            this.columns,
            this.view,
            new RemoteSystemImpl(this.localHost, this.localPort, this.remoteHost, this.remotePort)
        );
    }
}
