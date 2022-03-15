package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteSemaphore;

import java.util.Optional;
import java.util.concurrent.Semaphore;

public class RemoteSemaphoreImpl implements RemoteSemaphore {
    private final Semaphore semaphore;
    private Optional<Peer> permitOwner;

    public RemoteSemaphoreImpl() {
        this.semaphore = new Semaphore(1);
        this.permitOwner = Optional.empty();
    }

    @Override
    public void acquire(final Peer peer) {
        if (this.permitOwner.isEmpty() || !this.permitOwner.get().equals(peer)) {
            this.semaphore.acquireUninterruptibly();
        }
        this.permitOwner = Optional.of(peer);
    }

    @Override
    public void release(final Peer peer) {
        if (this.permitOwner.map(p -> !p.equals(peer)).orElse(false)) {
            throw new IllegalStateException();
        }
        this.permitOwner = Optional.empty();
        this.semaphore.release();
    }
}
