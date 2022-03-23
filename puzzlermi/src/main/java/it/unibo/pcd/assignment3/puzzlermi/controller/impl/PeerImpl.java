package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;

import java.io.Serializable;

/**
 * A record implememntation of the {@link Peer} interface.
 * @param host the hostname of this peer
 * @param port the port of this peer
 */
public record PeerImpl(String host, int port) implements Peer, Serializable {

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public int compareTo(final Peer other) {
        return this.host.compareTo(other.getHost()) != 0
               ? this.host.compareTo(other.getHost())
               : (this.port - other.getPort());
    }
}
