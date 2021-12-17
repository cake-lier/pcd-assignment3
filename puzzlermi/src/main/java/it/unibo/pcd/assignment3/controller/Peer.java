package it.unibo.pcd.assignment3.controller;

public interface Peer extends Comparable<Peer> {

    String getHost();

    int getPort();
}
