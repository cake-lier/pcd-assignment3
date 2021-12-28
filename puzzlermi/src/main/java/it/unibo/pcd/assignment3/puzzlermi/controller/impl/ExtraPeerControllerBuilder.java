package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

public class ExtraPeerControllerBuilder {
    private final String localHost;
    private final String remoteHost;
    private final View view;
    private int remotePort;
    private int localPort;
    private boolean built;

    public ExtraPeerControllerBuilder(final String localHost, final String remoteHost, final View view) {
        this.localHost = Objects.requireNonNull(localHost);
        this.remoteHost = Objects.requireNonNull(remoteHost);
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
        if (this.built) {
            throw new IllegalArgumentException("This builder has already built its object");
        }
        this.built = true;
        return new ControllerImpl(
            this.view,
            new PeerImpl(this.localHost, this.localPort),
            new PeerImpl(this.remoteHost, this.remotePort)
        );
    }
}
