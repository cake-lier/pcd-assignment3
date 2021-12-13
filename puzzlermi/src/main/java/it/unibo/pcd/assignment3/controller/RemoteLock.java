package it.unibo.pcd.assignment3.controller;

public interface RemoteLock {

    void lock(Peer peer);

    void unlock(Peer peer);
}
