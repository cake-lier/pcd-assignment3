package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteSemaphore;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * An implementation of the {@link RemoteSemaphore} interface.
 */
public class RemoteSemaphoreImpl implements RemoteSemaphore {
    private final Semaphore semaphore;
    private Optional<Peer> permitOwner;

    /**
     * Default constructor.
     */
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RemoteSemaphoreImpl that = (RemoteSemaphoreImpl) o;
        return this.semaphore.equals(that.semaphore) && this.permitOwner.equals(that.permitOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.semaphore, this.permitOwner);
    }

    @Override
    public String toString() {
        return "RemoteSemaphoreImpl{semaphore=" + this.semaphore + ", permitOwner=" + this.permitOwner + "}";
    }
}
