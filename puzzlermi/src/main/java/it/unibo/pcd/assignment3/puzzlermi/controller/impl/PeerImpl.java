package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;

import java.io.Serializable;

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
