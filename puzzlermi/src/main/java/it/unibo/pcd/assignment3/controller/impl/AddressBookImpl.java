package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.AddressBook;
import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteLock;

import java.util.SortedSet;
import java.util.TreeSet;

public class AddressBookImpl implements AddressBook {
    private final SortedSet<Peer> peers;
    private final RemoteLock lock;

    public AddressBookImpl() {
        this.peers = new TreeSet<>();
        this.lock = new RemoteLockImpl();
    }

    @Override
    public SortedSet<Peer> getPeers(final Peer peer) {
        this.lock.lock(peer);
        try {
            return new TreeSet<>(this.peers);
        } finally {
            this.lock.unlock(peer);
        }
    }

    @Override
    public void registerPeer(final Peer peer) {
        this.lock.lock(peer);
        try {
            this.peers.add(peer);
        } finally {
            this.lock.unlock(peer);
        }
    }

    @Override
    public void unregisterPeer(final Peer peer) {
        this.lock.lock(peer);
        try {
            this.peers.remove(peer);
        } finally {
            this.lock.unlock(peer);
        }
    }
}
