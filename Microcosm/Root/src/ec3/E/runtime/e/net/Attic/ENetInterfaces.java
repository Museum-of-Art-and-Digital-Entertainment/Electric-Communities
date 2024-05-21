/*
  ENetInterfaces.java -- Interface declarations for the E network comm system.

  Chip Morningstar
  based on earlier work by Eric Messick and Gordie Freedman
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;

import ec.e.start.Seismologist;

/**
 * A capability to establish communications connections over the network.
 * Connections may be considered incoming or outgoing depending on who
 * establishes them (incoming connections are established by other parties on
 * the net; outgoing connections are established by us) but connections, once
 * established, represent bidirectional communications pathways independent of
 * who established them.
 */
interface ENet {
    /**
     * Listen for new incoming connections.
     *
     * @param localAddress The local address to listen for connections on.
     * @param keeper An object that will be notified of new connections.
     * @returns An object which can be used to stop listening on this address.
     */
    ENetListener listen(ENetAddr localAddress, ENetListenerKeeper keeper);

    /**
     * Establish a new outgoing connection.
     *
     * @param remoteAddress The remote address to connect to.
     * @returns An object with which to make use of this new connection.
     */
    ENetConnection connect(ENetAddr remoteAddress);
}

/**
 * An object for controlling an active listener listening for incoming
 * connections over the network.
 */
interface ENetListener {
    /**
     * Stop listening for new incoming connections.
     */
    void shutdown();
}

/**
 * An object to notify about events with respect to an active ENetListener.
 */
einterface ENetListenerKeeper extends Seismologist {
    /**
     * Sent to enable a listener keeper by informing it who its listener is
     *
     * @param listener The keeper's listener
     */
    enable(ENetListener listener);

    /**
     * Sent when a new inbound connection is established.
     *
     * @param connection The new connection itself. Typically the keeper will
     *  want to call enable() on it.
     */
    noticeNewConnection(ENetConnection connection);

    /**
     * Sent when an error condition or other problem develops with an active
     * ENetlistener.
     *
     * @param problem An indication of the problem, encapsulated in a Java
     *  Exception object.
     */
    noticeProblem(Exception problem);
}

/**
 * An object representing an active communications pathway to and from another
 * party on the network.
 */
interface ENetConnection {
    /**
     * Configure this connection to actually participate in communications
     * with our local vat. This method must be called before a connection
     * becomes usable, but may only be called once on a given connection.
     *
     * @param receiver The object which is to receive incoming (low-level)
     *  messages received on this connection.
     * @param keeper The object which is to be notified about errors and other
     *  problems with respect to this connection.
     */
    void enable(ENetBlobReceiver receiver, ENetConnectionKeeper keeper);

    /**
     * Send a (low-level) message to the remote party.
     *
     * @param length The number of bytes to send.
     * @param blobData An array containing the bytes of data to be sent.
     */
    void sendBlob(int length, byte blobData[]);

    /**
     * Terminate this connection and cease communicating with the remote party.
     */
    void shutdown();
}

/**
 * An object to notify about events with respect to an active ENetConnection.
 */
einterface ENetConnectionKeeper extends Seismologist {
    /**
     * Sent to enable a connection keeper by informing it who its connection
     *  is.
     *
     * @param connection The keeper's connection
     */
    enable(ENetConnection connection);

    /**
     * Sent when an error condition or other problem develops with an active
     * ENetConnection.
     *
      * @param problem An indication of the problem, encapsulated in a Java
     *  Exception object.
     */
    noticeProblem(Exception problem);
}

/**
 * An object to receive inbound communications received via an ENetConnection.
 */
interface ENetBlobReceiver {
    /**
     * Called when a new (low-level) message is received from the remote party.
     *
     * @param length The number of bytes of data received.
     * @param blobData An array containing the bytes received.
     */
    void receiveBlob(int length, byte blobData[]);
}
