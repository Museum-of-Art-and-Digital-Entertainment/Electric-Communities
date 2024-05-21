/*
  NetAddr.java -- Classes representing network address for the E comm system

  Eric Messick and Chip Morningstar
  25-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net.crew;

import ec.util.NestedException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
  A simple class which represents an extended IP address -- an IP address
  together with a port number.

  XXX This needs to be made more abstract, so that it is more generic than IP
  addresses. This means all the method protocol that assumes IP addresses
  really doesn't belong here. Probably NetAddr should be an interface and
  everything below should simply implement it.
*/
public class NetAddr {
    private InetAddress myInetAddress;
    private int myPortNumber;

    /**
     * Private constructor so nobody else can mess with this.
     */
    private NetAddr() {
        /* Good fences make good neighbors. */
    }

    public NetAddr(String addr) throws UnknownHostException {
        int colon = addr.indexOf(':');
        if (colon < 0) {
            myPortNumber = 0;
        } else {
            myPortNumber = Integer.parseInt(addr.substring(colon + 1)) ;
            addr = addr.substring(0, colon);
        }
        int slash = addr.indexOf('/');
        if (slash >= 0) {
            addr = addr.substring(slash + 1);
        }
        myInetAddress = InetAddress.getByName(addr);
    }

    /**
     * Construct a new NetAddr given an IP address and a port number.
     *
     * @param ip An IP address
     * @param portNumber A port at that IP address
     */
    // XXX was protected
    public NetAddr(InetAddress ip, int portNumber) {
        myInetAddress = ip;
        myPortNumber = portNumber;
    }

    /**
     * Construct a local address given a port number (which is all that
     * matters locally).
     *
     * @param portNumber The port number. 0 means OS should assign the port.
     */
    public NetAddr(int portNumber) {
        myPortNumber = portNumber;
        try {
            myInetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new NestedException("Can't find the local host", e);
        }
    }

    /**
     * @return A hash code that accounts for both the IP address and port.
     */
    public int hashCode() {
        return(myInetAddress.hashCode() ^ myPortNumber);
    }

    /**
     * Test if another object is an NetAddr denoting the same address as this.
     *
     * @param other The other object to test for equality.
     * @return true iff this and other denote the same net address.
     */
    public boolean equals(Object other) {
        if (other != null && other instanceof NetAddr) {
            NetAddr otherAddr = (NetAddr)other;
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
        // XXX this should be a call to justAddr, but windows can't
        // handle the dotted.quad address
        return justAddr(myInetAddress.toString()) + ":" + myPortNumber;
    }

    /**
     * Produce an NetAddr of a given port on the local host.
     *
     * @param port The local port number to use.
     * @return A new NetAddr denoting the given port on the local host
     */
    static public NetAddr getLocalHost(int port) throws UnknownHostException {
        /* XXX - should unique these in Hashtable */
        InetAddress inet = InetAddress.getLocalHost();
        return new NetAddr(inet, port);
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
     * @return Just the dotted.quad portion of the string.
     */
    private String justAddr(String addrStr) {
        int slash = addrStr.indexOf('/');
        if (slash < 0) {
            return addrStr;
        } else {
            return addrStr.substring(slash + 1);
        }
    }

    /**
     * Strip junk from a string representation of an IP address.
     *
     * @param addrStr A string representing an InetAddr
     * @return Just the name portion of the string.
     */
    private String justName(String addrStr) {
        int slash = addrStr.indexOf('/');
        if (slash < 0) {
            return addrStr;
        } else {
            return addrStr.substring(0, slash);
        }
    }
}
