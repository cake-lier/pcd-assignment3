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

/**
 * An implementation of the {@link Controller} trait.
 */
class ControllerImpl implements Controller {
    private final Peer self;
    private final PuzzleBoard board;
    private final LocalGatekeeper localGatekeeper;
    private final Executor executor;

    /**
     * Constructor for creating the {@link Controller} of the application when associated to the first {@link Peer} in a game
     * session.
     * @param rows the number of rows of the {@link PuzzleBoard}
     * @param columns the number of columns of the {@link PuzzleBoard}
     * @param view the {@link View} component to be usd by this {@link Controller}
     * @param self the {@link Peer} associated to this {@link Controller} instance
     * @throws RemoteException if it could not create the RMI registry or export or bind the stub of the {@link RemoteGatekeeper}
     */
    ControllerImpl(final int rows, final int columns, final View view, final Peer self) throws RemoteException {
        this.self = Objects.requireNonNull(self);
        this.executor = Executors.newSingleThreadExecutor();
        this.board = new PuzzleBoardImpl(rows, columns);
        final var gatekeeper = new GatekeeperImpl(
            this.self,
            new RemotePuzzleImpl(this.board, Objects.requireNonNull(view)),
            new RemoteSemaphoreImpl()
        );
        this.localGatekeeper = gatekeeper;
        LocateRegistry.createRegistry(self.getPort()).rebind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
        view.displayTiles(this.board.getTiles());
    }

    /**
     * Constructor for creating the {@link Controller} of the application when associated to an "extra {@link Peer}", a
     * {@link Peer} which is not the first in joining a game session.
     * @param view the {@link View} component to be usd by this {@link Controller}
     * @param self the {@link Peer} associated to this {@link Controller} instance
     * @param buddy the {@link Peer} to be contacted for joining the desired game session
     * @throws NotBoundException if the {@link RemoteGatekeeper} of the buddy {@link Peer} is not found
     * @throws RemoteException if an error occurs in a remote method, namely because the connection was severed
     */
    ControllerImpl(final View view, final Peer self, final Peer buddy)
        throws NotBoundException, RemoteException {
        this.self = Objects.requireNonNull(self);
        this.executor = Executors.newSingleThreadExecutor();
        final var buddyGatekeeper =
            (RemoteGatekeeper) LocateRegistry.getRegistry(buddy.getHost(), buddy.getPort()).lookup("Gatekeeper");
        final var initialRemoteSemaphores = buddyGatekeeper.getRemoteSemaphores();
        final var gonePeers = new TreeSet<Peer>();
        this.acquireRemoteSemaphores(initialRemoteSemaphores, gonePeers);
        try {
            final var remoteSemaphores = buddyGatekeeper.getRemoteSemaphores();
            gonePeers.forEach(remoteSemaphores::remove);
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
                          .rebind("Gatekeeper", UnicastRemoteObject.exportObject(gatekeeper, 0));
            for (final var peer : remoteSemaphores.keySet()) {
                try {
                    final var peerGatekeeper =
                        ((RemoteGatekeeper) LocateRegistry.getRegistry(peer.getHost(), peer.getPort()).lookup("Gatekeeper"));
                    peerGatekeeper.registerPeer(self, selfPuzzleStub, selfSemaphoreStub);
                    peerGatekeeper.unregisterPeers(gonePeers);
                } catch (final RemoteException ignored) {}
            }
            view.displayTiles(this.board.getTiles());
            this.releaseRemoteSemaphores(remoteSemaphores.values());
        } finally {
            this.releaseRemoteSemaphores(initialRemoteSemaphores.values());
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

    /* Acquires all the given RemoteSemaphores and puts the Peers associated to the ones that throw RemoteException in the given
     * set.
     */
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

    /* Releases the given previously acquired RemoteSemaphores. */
    private void releaseRemoteSemaphores(final Collection<RemoteSemaphore> semaphores) {
        for (final var semaphore: semaphores) {
            try {
                semaphore.release(this.self);
            } catch (final RemoteException ignored) {}
        }
    }
}
