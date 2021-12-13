package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteLock;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteLockImpl implements RemoteLock {
    private final Lock lock;
    private final Condition canBeLocked;
    private Optional<Peer> permitOwner;

    public RemoteLockImpl() {
        this.lock = new ReentrantLock();
        this.canBeLocked = this.lock.newCondition();
        this.permitOwner = Optional.empty();
    }

    @Override
    public void lock(final Peer peer) {
        this.lock.lock();
        try {
            while (this.permitOwner.isPresent() && !this.permitOwner.get().equals(peer)) {
                try {
                    this.canBeLocked.await();
                } catch (InterruptedException ignored) {}
            }
            this.permitOwner = Optional.of(peer);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void unlock(final Peer peer) {
        this.lock.lock();
        try {
            if (this.permitOwner.isPresent() && !this.permitOwner.get().equals(peer)) {
                throw new IllegalStateException();
            }
            this.permitOwner = Optional.empty();
            this.canBeLocked.signal();
        } finally {
            this.lock.unlock();
        }
    }
}
