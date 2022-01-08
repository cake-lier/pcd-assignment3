package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.controller.LocalGatekeeper;
import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteGatekeeper;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteSemaphore;
import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.PuzzleBoard;
import it.unibo.pcd.assignment3.puzzlermi.model.impl.PuzzleBoardImpl;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class ControllerImpl implements Controller {
    private final Peer self;
    private final PuzzleBoard board;
    private final LocalGatekeeper localGatekeeper;
    private final Executor executor;

    ControllerImpl(final int rows, final int columns, final View view, final Peer self)
        throws AlreadyBoundException, RemoteException {
        this.self = Objects.requireNonNull(self);
        this.executor = Executors.newSingleThreadExecutor();
        this.board = new PuzzleBoardImpl(rows, columns);
        final var gatekeeper = new GatekeeperImpl(
            this.self,
            new RemotePuzzleImpl(this.board, Objects.requireNonNull(view)),
            new RemoteSemaphoreImpl()
        );
        this.localGatekeeper = gatekeeper;
        LocateRegistry.createRegistry(self.getPort()).bind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
        view.displayTiles(this.board.getTiles());
    }

    ControllerImpl(final View view, final Peer self, final Peer buddy)
        throws AlreadyBoundException, NotBoundException, RemoteException {
        this.self = Objects.requireNonNull(self);
        this.executor = Executors.newSingleThreadExecutor();
        final var buddyGatekeeper =
            (RemoteGatekeeper) LocateRegistry.getRegistry(buddy.getHost(), buddy.getPort()).lookup("Gatekeeper");
        final var remoteSemaphores = buddyGatekeeper.getRemoteSemaphores();
        final var gonePeers = new TreeSet<Peer>();
        this.acquireRemoteSemaphores(remoteSemaphores, gonePeers);
        try {
            final var remotePuzzles = buddyGatekeeper.getRemotePuzzles();
            gonePeers.forEach(remotePuzzles::remove);
            this.board = new PuzzleBoardImpl(remotePuzzles.get(buddy).getTiles());
            final var selfPuzzle = new RemotePuzzleImpl(this.board, Objects.requireNonNull(view));
            final var selfPuzzleStub = (RemotePuzzle) UnicastRemoteObject.exportObject(selfPuzzle, 0);
            final var selfSemaphore = new RemoteSemaphoreImpl();
            final var selfSemaphoreStub = (RemoteSemaphore) UnicastRemoteObject.exportObject(selfSemaphore, 0);
            final var gatekeeper = new GatekeeperImpl(
                self,
                selfPuzzle,
                selfPuzzleStub,
                selfSemaphore,
                selfSemaphoreStub,
                remotePuzzles,
                remoteSemaphores
            );
            this.localGatekeeper = gatekeeper;
            LocateRegistry.createRegistry(self.getPort())
                          .bind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
            for (final var peer : remoteSemaphores.keySet()) {
                try {
                    final var peerGatekeeper =
                        ((RemoteGatekeeper) LocateRegistry.getRegistry(peer.getHost(), peer.getPort()).lookup("Gatekeeper"));
                    peerGatekeeper.registerPeer(self, selfPuzzleStub, selfSemaphoreStub);
                    peerGatekeeper.unregisterPeers(gonePeers);
                } catch (final RemoteException ignored) {}
            }
            view.displayTiles(this.board.getTiles());
        } finally {
            this.releaseRemoteSemaphores(remoteSemaphores.values());
        }
    }

    @Override
    public void exit() {
        this.executor.execute(() -> {
            final var remoteSemaphores = this.localGatekeeper.getLocalSemaphores();
            final var gonePeers = new TreeSet<Peer>();
            this.acquireRemoteSemaphores(remoteSemaphores, gonePeers);
            try {
                gonePeers.add(this.self);
                for (final var peer: remoteSemaphores.keySet()) {
                    try {
                        ((RemoteGatekeeper) LocateRegistry.getRegistry(peer.getHost(), peer.getPort())
                                                          .lookup("Gatekeeper"))
                                                          .unregisterPeers(gonePeers);
                    } catch (final RemoteException | NotBoundException ignored) {}
                }
            } finally {
                this.releaseRemoteSemaphores(remoteSemaphores.values());
            }
            System.exit(0);
        });
    }

    @Override
    public void swap(final Position firstPosition, final Position secondPosition) {
        this.executor.execute(() -> {
            final var remoteSemaphores = this.localGatekeeper.getLocalSemaphores();
            final var gonePeers = new TreeSet<Peer>();
            this.acquireRemoteSemaphores(remoteSemaphores, gonePeers);
            try {
                final var remotePuzzles = this.localGatekeeper.getLocalPuzzles();
                gonePeers.forEach(remotePuzzles::remove);
                remotePuzzles.forEach((k, v) -> {
                    try {
                        v.swap(firstPosition, secondPosition);
                    } catch (final RemoteException ex) {
                        gonePeers.add(k);
                    }
                });
                gonePeers.forEach(remoteSemaphores::remove);
                for (final var peer: remoteSemaphores.keySet()) {
                    try {
                        ((RemoteGatekeeper) LocateRegistry.getRegistry(peer.getHost(), peer.getPort())
                                                          .lookup("Gatekeeper"))
                                                          .unregisterPeers(gonePeers);
                    } catch (final RemoteException | NotBoundException ignored) {}
                }
            } finally {
                this.releaseRemoteSemaphores(remoteSemaphores.values());
            }
        });
    }

    private void acquireRemoteSemaphores(
        final SortedMap<Peer, RemoteSemaphore> remoteSemaphores,
        final SortedSet<Peer> gonePeers
    ) {
        remoteSemaphores.forEach((k, v) -> {
            try {
                v.acquire(this.self);
            } catch (final RemoteException ex) {
                gonePeers.add(k);
            }
        });
        gonePeers.forEach(remoteSemaphores::remove);
    }

    private void releaseRemoteSemaphores(final Collection<RemoteSemaphore> semaphores) {
        for (final var semaphore: semaphores) {
            try {
                semaphore.release(this.self);
            } catch (final RemoteException ignored) {}
        }
    }
}
