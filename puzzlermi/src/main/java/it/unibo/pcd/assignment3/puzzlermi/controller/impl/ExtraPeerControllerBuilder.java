package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;

/**
 * A builder for creating a new instance of the {@link Controller} interface to be used when it is associated with an "extra
 * {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer}", a {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer}
 * which is not the first in joining its desired game session.
 */
public class ExtraPeerControllerBuilder {
    private final String localHost;
    private final String remoteHost;
    private final View view;
    private int remotePort;
    private int localPort;
    private boolean built;

    /**
     * Default constructor.
     * @param localHost the hostname of the {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} associated with this
     *                  application
     * @param remoteHost the hostname of the "buddy {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer}", the
     *                   {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} to be contacted by the one associated to
     *                   this application for joining the desired game session
     * @param view the {@link View} component to be usd by the built {@link Controller}
     */
    public ExtraPeerControllerBuilder(final String localHost, final String remoteHost, final View view) {
        this.localHost = Objects.requireNonNull(localHost);
        this.remoteHost = Objects.requireNonNull(remoteHost);
        this.view = Objects.requireNonNull(view);
        this.remotePort = 1099;
        this.localPort = 1099;
        this.built = false;
    }

    /**
     * Sets the port of the "buddy {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer}", the
     * {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} to be contacted by the one associated to
     * this application for joining the desired game session.
     * @param remotePort the value of the port to set
     * @return this {@link ExtraPeerControllerBuilder} instance
     */
    public ExtraPeerControllerBuilder setRemotePort(final int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    /**
     * Sets the port of the {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer} associated with this application.
     * @param localPort the value of the port to set
     * @return this {@link ExtraPeerControllerBuilder} instance
     */
    public ExtraPeerControllerBuilder setLocalPort(final int localPort) {
        this.localPort = localPort;
        return this;
    }

    /**
     * Builds a new instance of {@link Controller}.
     * @return a new instance of {@link Controller}
     * @throws RemoteException if the methods called fail while executing remotely
     * @throws NotBoundException if the {@link it.unibo.pcd.assignment3.puzzlermi.controller.RemoteGatekeeper} of the "buddy
     *                           {@link it.unibo.pcd.assignment3.puzzlermi.controller.Peer}" is not found
     */
    public Controller build() throws RemoteException, NotBoundException {
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

    @Override
    public String toString() {
        return "ExtraPeerControllerBuilder{localHost='" + this.localHost + "', remoteHost='" + this.remoteHost + "', view="
               + this.view + ", remotePort=" + this.remotePort + ", localPort=" + this.localPort + ", built=" + this.built + "}";
    }
}
