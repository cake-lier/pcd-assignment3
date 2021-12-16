package it.unibo.pcd.assignment3.controller;

import java.util.SortedMap;

public interface LocalGatekeeper {

    SortedMap<Peer, RemotePuzzle> getLocalPuzzles();

    SortedMap<Peer, RemoteSemaphore> getLocalSemaphores();
}
