package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteSystem;
import it.unibo.pcd.assignment3.model.Tile;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RemoteSystemImpl implements RemoteSystem {

    public RemoteSystemImpl(final String localHost, final int localPort, final String remoteHost, final int remotePort)
        throws RemoteException, AlreadyBoundException, NotBoundException {
        final var registry = LocateRegistry.createRegistry(localPort);
        final var self = new PeerImpl(localHost, localPort);
        final var addressBook = new AddressBookImpl(Set.of(self));
        registry.bind("AddressBook", addressBook);
        registry.bind("RemotePuzzle", null);
        final var peersToContact = new HashSet<Peer>(Set.of(new PeerImpl(remoteHost, remotePort)));
        final var peersContacted = new HashSet<Peer>(Set.of(self));
        do {
            final var peer = new ArrayList<>(peersToContact).get(0);
            final var newPeers = ((MutableAddressBook) LocateRegistry.getRegistry(peer.getHost(), peer.getPort())
                                                                     .lookup("AddressBook"))
                                                                     .registerPeer(self);
            peersContacted.add(peer);
            newPeers.removeAll(peersContacted);
            peersToContact.addAll(newPeers);
        } while (!peersToContact.isEmpty());
    }

    @Override
    public void swap(final Tile firstTile, final Tile secondTile) {

    }
}
