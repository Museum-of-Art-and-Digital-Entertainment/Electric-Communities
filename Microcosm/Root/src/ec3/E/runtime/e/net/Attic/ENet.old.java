package ec.e.net;

import ec.e.cap.ERestrictedException;
import ec.e.netbugs.InetAddrWorkaround;
import ec.regexp.RegularExpression;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/* Sturdy steward: must be steward because it references java.net.
   References Java.net.Socket which is not sturdy, but it's unproblematic
   because it is only passing thru here. */

/**
  General interface for manufacturing a connection from a pair of addresses
  and a socket.
*/
public interface ENetConnectionFactory {
    public Object manufacture(Socket socket, ENetAddr localAddr,
                              ENetAddr remoteAddr, boolean incoming)
        throws ERestrictedException, IOException;
}

/**
  Implements a raw capability to the network.
*/
final public class ENet {
    static Trace tr = new Trace(false, "[ENet]");

    private int myDefaultPort;
    private RtComMonitor myMonitor;

    static private boolean created = false;

    /**
     * Produce the (single) raw network capability. This should be stored in
     * the EEnvironment. <p>
     *
     * To retrieve the capability from an EEnvironment env: <p>
     *   ENet net = (ENet)env.get("net.root"); <p>
     *
     * @param env The EEnvironment we are executing in
     * @param port The default port number to use.
     * @return An ENet object with unrestricted access to the net.
     */
    static public ENet createRootCapability(int port) {
        if (created)
            return(null); /* there can be only ONE! */
        created = true;
        return new ENet(port);
    }

    /**
     * Construct an unrestricted ENet. Only callable in class.
     *
     * @param port The default port number to use.
     */
    private ENet(int defaultPort) {
        if (tr.tracing)
            tr.$("Creating root ENet capability");

        myDefaultPort = defaultPort;
        myMonitor = new RtComMonitor();
    }

    /**
     * Produce an ENetListener to listen for new connections.
     *
     * @param port The port number to listen on
     * @param cf A connection factory to make a connection object as needed
     * @return A new ENetListener that listens on that port
     * @see ec.e.net.RtComMonitor listen()
     */
    public ENetListener listen(int port, ENetConnectionFactory cf)
            throws IOException {
        return myMonitor.listen(port, new ENetListenerFactory(this, cf));
    }

    /**
     * Open a connection to some address on the network.
     *
     * @param address The address to connect to.
     * @param cf A connection factory to make a connection object as needed
     * @return The object to start talking to on that connection
     * @see ec.e.net.RtComMonitor connect()
     *
     * Note that the address to connect to is passed in as a string.
     * Use either "domain.name:port" or "127.0.0.1:34". The :port is optional.
     *  The default port that the ENet was created with will be used if you
     * don't specify one.
     */
    public Object connect(String address, ENetConnectionFactory cf)
            throws ERestrictedException, IOException { /*@BLOCK METHOD */
        return myMonitor.connect(fromString(address), cf);/*@BLOCK */
    }


    /**
     * This holds a regular expression that matches a dotted quad.
     */
    private static RegularExpression reNumericAddr;
    static {
        String el = "\\([0-9]\\{1,3\\}\\)";
        String d  = "\\.";
        reNumericAddr =
            new RegularExpression("^" + el + d + el + d + el + d + el + "$");
    }

    /**
     * Produce an ENetAddr from a string. The string must match against
     * one of the patterns: "domain.name[:port]" or "127.0.0.1[:port]" or
     * "domain.name/127.0.0.1:34". Further, it must pass various lookups that
     * verify it as a valid address.
     *
     * @param s The string to parse
     * @return The ENetAddr described by the string
     */
    ENetAddr fromString(String s)
            throws ERestrictedException, UnknownHostException {
        int port;
        byte a[] = null;
        boolean haveDottedQuad = false;
        boolean haveName = false;
        String name;
        String number;
        String revName;
        InetAddress addresses[];
        InetAddress address = null;
        InetAddress revAddress;

        if (tr.tracing)
            tr.$("validating address " + s);

        /* extract and check the port */
        int colon = s.indexOf(':');
        if (colon < 0) {
            port = myDefaultPort;
        } else {
            port = Integer.parseInt(s.substring(colon+1));
            s = s.substring(0, colon);
        }

        int slash = s.indexOf('/');
        if (slash < 0) { /* only one form of address */
            if (tr.tracing)
                tr.$("only one form of address: " + s);
            /* get numeric address */
            if (reNumericAddr.Match(s)) {
                if (tr.tracing)
                    tr.$("address is numeric: " + s);
                haveDottedQuad = true;
                name = null;
                number = s;
            } else {
                if (tr.tracing)
                    tr.$("address NOT numeric: " + s);
                haveName = true;
                name = s;
                number = null;
            }
        } else {
            name = s.substring(0, slash);
            number = s.substring(slash+1);
            if (tr.tracing)
                tr.$("both forms of address, name:" + name + ", number:" +
                     number);
            if (reNumericAddr.Match(number))
                haveDottedQuad = true;
            if (!haveDottedQuad && tr.tracing)
                tr.$("numeric address didn't match pattern: '" + number + "'");
            haveName = true;
        }

        if (haveDottedQuad) {
            a = new byte[4];
            a[0] = (byte)Integer.parseInt(reNumericAddr.SubMatch(1));
            a[1] = (byte)Integer.parseInt(reNumericAddr.SubMatch(2));
            a[2] = (byte)Integer.parseInt(reNumericAddr.SubMatch(3));
            a[3] = (byte)Integer.parseInt(reNumericAddr.SubMatch(4));
        }
        if (haveName) {
            if (tr.tracing)
                tr.$("looking up " + name);
            addresses = InetAddress.getAllByName(name);
            if (haveDottedQuad) {
                for (int i=0; i<addresses.length; i++) {
                    byte b[] = addresses[i].getAddress();
                    if (a[0]==b[0] && a[1]==b[1] && a[2]==b[2] && a[3]==b[3]) {
                        address = addresses[i];
                        break;
                    }
                }
                if (address == null)
                    throw new ERestrictedException("ENet fromString: name " +
                        name + " does not map to given address: " + number);
            } else {
                if (addresses.length < 1) /* this shouldn't ever happen */
                    throw new ERestrictedException("ENet fromString: name " +
                        name + " does not map to any addresses");
                address = addresses[0];
                a = addresses[0].getAddress();
            }
        }

        /* ok, now check to see if the reverse mapping looks ok */

        /* XXX bug in InetAddress:  can't do reverse name lookups */
        /* revAddress = InetAddress.getByName(((int)a[0]&0xff) + "." +
               ((int)a[1]&0xff) + "." + ((int)a[2]&0xff) + "." +
               ((int)a[3]&0xff)); // shouldn't throw! */
        /* revName = revAddress.getHostName();// returns dotted quad on fail */
        revName = InetAddrWorkaround.ReverseNameLookup(a);

        if (tr.tracing)
            tr.$("reverse lookup returned " + revName);
        if (reNumericAddr.Match(revName))
            throw new ERestrictedException(
                "ENet fromString: reverse mapping failed");

        /* if we were handed only a dotted quad we still haven't done a
           forward lookup */
        if (haveDottedQuad && !haveName) {
            addresses = InetAddress.getAllByName(revName);
            for (int i=0; i<addresses.length; i++) {
                if (tr.tracing)
                    tr.$("forward lookup:  " + addresses[i]);
                byte b[] = addresses[i].getAddress();
                if (a[0]==b[0] && a[1]==b[1] && a[2]==b[2] && a[3]==b[3]) {
                    address = addresses[i];
                    break;
                }
            }
            if (address == null)
                throw new ERestrictedException(
                    "ENet fromString: reverse name " + revName +
                    " does not map to given address: " + number);
        }
        if (tr.tracing)
            tr.$("valid address " + address + ":" + port);
        return(new ENetAddr(address, port));
    }
}

/**
  Handle setup of inbound connections. This factory is used by the listener
  to establish a new connection when the listener gets a hit on its 'accept'
  operation.

  This factory is provided with a factory of its own to produce the actual
  connection according to whatever protocol is appropriate.
*/
class ENetListenerFactory implements ENetConnectionFactory {
    private ENet myNet;
    private ENetConnectionFactory myFactory;

    /**
     * Construct a listener given a network capability and a more primitive
     * connection factory.
     */
    ENetListenerFactory(ENet net, ENetConnectionFactory factory) {
        myNet = net;
        myFactory = factory;
    }
    
    /**
     * Return a connection object for a new connection.
     *
     * @param socket The socket to actually do I/O on.
     * @param localAddr The location IP address/port
     * @param remoteAddr The remote IP address/port
     * @param incoming Direction of connection (true=>inbound)
     * @return A newly manufactured connection as decribed by the parameters
     */
    public Object manufacture(Socket socket, ENetAddr localAddr,
                              ENetAddr remoteAddr, boolean incoming)
            throws ERestrictedException, IOException
    {
        /* @BLOCK METHOD */
        ENetAddr them = null;
        try {
            them = myNet.fromString(remoteAddr.toString());
        } catch (UnknownHostException e) {
            throw new InternalError(
                "ENet.fromString shouldn't fail with a numeric address " + e);
        }
        /* @BLOCK */
        return(myFactory.manufacture(socket, localAddr, them, incoming));
    }
}

/*

  DNS attack: Information leaks outside of the limits stated for an
  environment's connection capabilities.  The leak occurs when an address is
  looked up to see if it can be connected to.  If restriction is domain.name
  based, it can be filtered on before being passed to DNS.  If an ipaddr is
  provided to connect to instead, a reverse lookup will have to be done if
  only domain name filtering is provided.  This reverse lookup can leak
  several bytes of information, along with the ipaddress of the nameserver
  handling the request.  To be most effective, both domain and ipaddr
  filtering should be used for outgoing connections.  Incoming connections
  only need ipaddr filtering, since the domain name will never be provided,
  and the ipaddr is externally specified, leaving no information to be leaked.
  If possible, the domain and ipaddr restrictions should map to the same set
  of machines.

*/
