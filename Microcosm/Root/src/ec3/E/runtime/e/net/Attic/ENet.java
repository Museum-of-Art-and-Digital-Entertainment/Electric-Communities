/*
  ENet.java -- Implementation of the low-level ENet interface for the Vat

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.Vat;

/**
 * A capability to establish communications connections over the network.
 * Connections may be considered incoming or outgoing depending on who
 * establishes them (incoming connections are established by other parties on
 * the net; outgoing connections are established by us) but connections, once
 * established, represent bidirectional communications pathways independent of
 * who established them. Connections established by this package are low-level
 * connections, in that they provide raw socket I/O of packets of bytes with
 * no interpretation or segmentation of the data streams presumed.
 */
class ENetSteward implements ENet {
    private Vat myVat;

    /**
     * Represents the (low-level) network inside a vat.
     */
    ENetSteward(Vat vat) {
        myVat = vat;
    }

    /**
     * Establish a new outgoing (low-level) connection.
     *
     * @param remoteAddress The remote address to connect to.
     * @returns An object with which to make use of this new connection.
     */
    public ENetConnection connect(ENetAddr remoteAddress) {
        RtNetConnectionThread connectionThread =
            new RtNetConnectionThread(remoteAddress);
        Tether connectionThreadHolder = new Tether(myVat, connectionThread);
        ENetConnection connection =
            new ENetConnectionSteward(connectionThreadHolder);
        return connection;
    }

    /**
     * Listen for new incoming (low-level) connections.
     *
     * @param localAddress The local address to listen for connections on.
     * @param keeper An object that will be notified of new connections.
     * @returns An object which can be used to stop listening on this address.
     */
    public ENetListener listen(ENetAddr localAddress,
                               ENetListenerKeeper keeper) {
        ENetListenerKeeper keeperSuccessor;
        Tether keeperHolder =
            myVat.makeFragileRoot((Seismologist) keeperSuccessor);
        RtNetListenerThread listenerThread =
            new RtNetListenerThread(localAddress, keeperHolder);
        Tether listenerThreadHolder = new Tether(myVat, listenerThread);
        ENetListener listener = new ENetListenerSteward(listenerThreadHolder);
        RtEnvelope msg; msg <- ENetListenerKeeper.enable(listener);
        keeper <- EObject.order(msg, &keeperSuccessor);
        listenerThread.startup();
        return listener;
    }
}
