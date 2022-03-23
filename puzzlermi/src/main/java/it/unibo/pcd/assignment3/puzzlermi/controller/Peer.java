package it.unibo.pcd.assignment3.puzzlermi.controller;

/**
 * A peer of the distributed system, a node where an application runs and through which a player can play.
 */
public interface Peer extends Comparable<Peer> {

    /**
     * Returns the hostname of this peer.
     * @return the hostname of this peer
     */
    String getHost();

    /**
     * Returns the port of this peer
     * @return the port of this peer
     */
    int getPort();
}
