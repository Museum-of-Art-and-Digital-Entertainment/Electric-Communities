package ec.e.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/* Sturdy steward: must be steward because it references java.net. Otherwise
   it's unproblematic. */

/**
  A simple class which represents an extended IP address -- an IP address
  together with a port number.
*/
public class ENetAddr implements RtCodeable {
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
    ENetAddr(InetAddress ip, int portNumber) {
        try {
            if (isLocalHost(ip))
                myInetAddress = InetAddress.getLocalHost();
            else
                myInetAddress = ip;
        } catch (UnknownHostException e) {
            myInetAddress = ip;
        }
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
     * Part of the RtCodeable interface -- always encode as our own class.
     */
    public String classNameToEncode(RtEncoder encoder) {
        return(this.getClass().getName());
    }

    /**
     * Part of the RtCodeable interface -- encode ourselves on a stream.
     *
     * @param toStream The encoder to use
     */
    public void encode(RtEncoder toStream) {
        try {
            toStream.writeInt(myPortNumber);
            toStream.writeUTF(myInetAddress.toString());
        } catch (IOException e) {
            /* XXX encode should be declared to throw IOException */
            throw new InternalError();
        }
    }

    /**
     * Part of the RtCodeable interface -- decode ourselves from a stream.
     *
     * @param fromStream The decoder to use
     */
    public Object decode(RtDecoder fromStream) {
        try {
            myPortNumber = fromStream.readInt();
            myInetAddress =
                InetAddress.getByName(justInetAddress(fromStream.readUTF()));
            return this;
        } catch (IOException e) {
            /* XXX decode should be declared to throw IOException */
            throw new InternalError();
        }
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
