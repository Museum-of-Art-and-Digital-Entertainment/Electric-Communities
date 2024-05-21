/*
  ENetAddr.java -- Classes representing network address for the E comm system

  Eric Messick and Chip Morningstar
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
  A simple class which represents an extended IP address -- an IP address
  together with a port number.

  XXX This needs to be made more abstract, so that it is more generic than IP
  addresses. This means all the method protocol that assumes IP addresses
  really doesn't belong here. Probably ENetAddr should be an interface and
  everything below should simply implement it.
*/
public class ENetAddr {
    private InetAddress myInetAddress;
    private int myPortNumber;

    /**
     * Private constructor so nobody else can mess with this.
     */
    private ENetAddr() {
        /* Good fences make good neighbors. */
    }

    /**
     * Construct a new ENetAddr given an IP address and a port number.
     *
     * @param ip An IP address
     * @param portNumber A port at that IP address
     */
    protected ENetAddr(InetAddress ip, int portNumber) {
        myInetAddress = ip;
        myPortNumber = portNumber;
    }

    /**
     * @return A hash code that accounts for both the IP address and port.
     */
    public int hashCode() {
        return(myInetAddress.hashCode() ^ myPortNumber);
    }

    /**
     * Test if another object is an ENetAddr denoting the same address as this.
     *
     * @param other The other object to test for equality.
     * @return true iff this and other denote the same net address.
     */
    public boolean equals(Object other) {
        if (other != null && other instanceof ENetAddr) {
            ENetAddr otherAddr = (ENetAddr)other;
            return otherAddr.myPortNumber == myPortNumber &&
                otherAddr.equals(myInetAddress);
        }
        return false;
    }

    /**
     * Return my port number.
     * @return my port number
     */
    public int getPortNumber() {
        return myPortNumber;
    }

    /**
     * Return my IP address.
     * @return my IP address
     */
    public InetAddress getInetAddress() {
        return myInetAddress;
    }

    /**
     * Produce a printable representation of this.
     * @return A nicely formatted string representing this address.
     */
    public String toString() {
        return myInetAddress + ":" + myPortNumber;
    }

    /**
     * Produce an ENetAddr of a given port on the local host.
     *
     * @param port The local port number to use.
     * @return A new ENetAddr denoting the given port on the local host
     */
    static public ENetAddr getLocalHost(int port) throws UnknownHostException {
        /* XXX - should unique these in Hashtable */
        InetAddress inet = InetAddress.getLocalHost();
        return new ENetAddr(inet, port);
    }

    /**
     * Test if a given IP address is localhost (127.0.0.1).
     *
     * @param addr An InetAddress to test
     * @return true iff given address is localhost.
     */
    private boolean isLocalHost(InetAddress addr) {
        byte a[] = addr.getAddress();
        return a[0] == 127 && a[1] == 0 && a[2] == 0 && a[3] == 1;
    }

    /**
     * Strip junk from a string representation of an IP address.
     *
     * @param addrStr A string representing an InetAddr
     * @return Just the address portion of the string.
     */
    private String justInetAddress(String addrStr) {
        int x = addrStr.indexOf('/');
        if (x == -1)
            return addrStr;
        else
            return addrStr.substring(x + 1);
    }
}

/**
 * Represent a local network address.
 */
public class LocalENetAddr extends ENetAddr {
    /**
     * Construct a local address given a port number (which is all that
     * matters locally).
     *
     * @param portNumber The port number. 0 means OS should assign the port.
     */
    public LocalENetAddr(int portNumber) {
        super(null, portNumber);
        try {
            myInetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new Error("Can't find the local host " + e);
        }
    }
}

/**
 * Represent a remote network address.
 */
public class RemoteENetAddr extends ENetAddr {
    /**
     * Construct a remote address given an IP address and port.
     */
    public RemoteENetAddr(InetAddress ip, int portNumber) {
        super(ip, portNumber);
    }
}
