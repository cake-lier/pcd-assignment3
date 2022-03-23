package it.unibo.pcd.assignment3.puzzlermi.controller;

import java.util.SortedMap;

/**
 * The local component of the gatekeeper entity, which is the interface that the local {@link Peer} shows to the
 * {@link Controller} of the application.
 */
public interface LocalGatekeeper {

    /**
     * Returns the local copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     * corresponding {@link RemotePuzzle}s.
     * @return the local copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     *         corresponding {@link RemotePuzzle}s
     */
    SortedMap<Peer, RemotePuzzle> getLocalPuzzles();

    /**
     * Returns the local copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     * corresponding {@link RemoteSemaphore}s.
     * @return the local copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     *         corresponding {@link RemoteSemaphore}s
     */
    SortedMap<Peer, RemoteSemaphore> getLocalSemaphores();
}
