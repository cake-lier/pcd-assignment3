package it.unibo.pcd.assignment3.puzzlermi.controller;

import java.util.SortedMap;

public interface LocalGatekeeper {

    SortedMap<Peer, RemotePuzzle> getLocalPuzzles();

    SortedMap<Peer, RemoteSemaphore> getLocalSemaphores();
}
