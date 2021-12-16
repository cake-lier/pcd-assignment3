package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.LocalGatekeeper;
import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteGatekeeper;
import it.unibo.pcd.assignment3.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.controller.RemoteSemaphore;
import it.unibo.pcd.assignment3.controller.RemoteSystem;
import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.model.impl.PuzzleBoardImpl;
import it.unibo.pcd.assignment3.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class RemoteSystemImpl implements RemoteSystem {
    private final Peer self;
    private final View view;
    private final PuzzleBoard board;
    private final LocalGatekeeper localGatekeeper;

    public RemoteSystemImpl(final Peer self, final View view, final PuzzleBoard board)
        throws AlreadyBoundException, RemoteException {
        this.self = Objects.requireNonNull(self);
        this.view = Objects.requireNonNull(view);
        this.board = Objects.requireNonNull(board);
        final var registry = LocateRegistry.createRegistry(self.getPort());
        final var gatekeeper = new GatekeeperImpl(
            this.self,
            new RemotePuzzleImpl(this.board, this::onSwapPerformed),
            new RemoteSemaphoreImpl()
        );
        this.localGatekeeper = gatekeeper;
        registry.bind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
    }

    public RemoteSystemImpl(final Peer self, final Peer buddy, final View view)
        throws NotBoundException, AlreadyBoundException {
        this.self = Objects.requireNonNull(self);
        this.view = Objects.requireNonNull(view);
        final Registry registry;
        final Registry buddyRegistry;
        final RemoteGatekeeper buddyGatekeeper;
        final SortedMap<Peer, RemoteSemaphore> peersSemaphores;
        try {
            registry = LocateRegistry.createRegistry(self.getPort());
            buddyRegistry = LocateRegistry.getRegistry(buddy.getHost(), buddy.getPort());
            buddyGatekeeper = (RemoteGatekeeper) buddyRegistry.lookup("Gatekeeper");
            peersSemaphores = buddyGatekeeper.getRemoteSemaphores();
        } catch (final RemoteException ex) {
            throw new IllegalStateException("Something went wrong in the initialization phase", ex);
        }
        final var peersToContact = this.acquireRemoteLocks(peersSemaphores);
        try {
            final var puzzles = buddyGatekeeper.getRemotePuzzles();
            this.board = new PuzzleBoardImpl(puzzles.get(puzzles.firstKey()).getTiles());
            final var selfPuzzle = new RemotePuzzleImpl(this.board, this::onSwapPerformed);
            final var selfPuzzleStub = (RemotePuzzle) UnicastRemoteObject.exportObject(selfPuzzle, 0);
            final var selfSemaphore = new RemoteSemaphoreImpl();
            final var selfSemaphoreStub = (RemoteSemaphore) UnicastRemoteObject.exportObject(selfSemaphore, 0);
            for (final var peer: peersToContact.keySet()) {
                ((RemoteGatekeeper) LocateRegistry.getRegistry(peer.getHost(), peer.getPort()).lookup("Gatekeeper"))
                    .registerPeer(
                        self,
                        selfPuzzleStub,
                        selfSemaphoreStub
                    );
            }
            final var gatekeeper = new GatekeeperImpl(
                self,
                selfPuzzle,
                selfPuzzleStub,
                selfSemaphore,
                selfSemaphoreStub,
                buddyGatekeeper.getRemotePuzzles(),
                buddyGatekeeper.getRemoteSemaphores()
            );
            this.localGatekeeper = gatekeeper;
            registry.bind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
        } catch (final RemoteException ex) {
            this.releaseRemoteLocks(peersToContact.values());
            throw new IllegalStateException(ex);
        }
        this.releaseRemoteLocks(peersToContact.values());
    }

    @Override
    public List<Tile> getTiles() {
        return this.board.getTiles();
    }

    @Override
    public void requestSwap(final Tile firstTile, final Tile secondTile) {
        this.acquireRemoteLocks(this.localGatekeeper.getLocalSemaphores());
        final var leftPeers = new TreeSet<Peer>();
        for (final var entry: this.localGatekeeper.getLocalPuzzles().entrySet()) {
            try {
                entry.getValue().swap(firstTile, secondTile);
            } catch (final RemoteException ex) {
                leftPeers.add(entry.getKey());
            }
        }
        final var peersToContact = new TreeMap<>(this.localGatekeeper.getLocalSemaphores());
        for (final var leftPeer: leftPeers) {
            peersToContact.remove(leftPeer);
        }
        // Tell all peersToContact to remove leftPeers
        this.releaseRemoteLocks(peersToContact.values());
    }

    private SortedMap<Peer, RemoteSemaphore> acquireRemoteLocks(final SortedMap<Peer, RemoteSemaphore> peersSemaphores) {
        final var leftPeers = new TreeSet<Peer>();
        for (final var entry: peersSemaphores.entrySet()) {
            try {
                entry.getValue().acquire(this.self);
            } catch (final RemoteException ex) {
                leftPeers.add(entry.getKey());
            }
        }
        final var peersToContact = new TreeMap<>(peersSemaphores);
        for (final var leftPeer: leftPeers) {
            peersToContact.remove(leftPeer);
        }
        // Tell all peersToContact to remove leftPeers
        return peersToContact;
    }

    private void releaseRemoteLocks(final Collection<RemoteSemaphore> semaphores) {
        for (final var semaphore: semaphores) {
            try {
                semaphore.release(this.self);
            } catch (final RemoteException ignored) {}
            // Remove unreached
        }
    }

    private void onSwapPerformed(final Tile firstTile, final Tile secondTile) {
        this.board.swap(firstTile, secondTile);
        this.view.displayTiles(this.board.getTiles());
        if (this.board.isSolution()) {
            this.view.displaySolution();
        }
    }
}
