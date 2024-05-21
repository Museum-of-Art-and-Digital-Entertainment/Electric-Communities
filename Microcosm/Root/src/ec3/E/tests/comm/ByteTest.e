package ec.e.tests.comm;

import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkListener;
import ec.e.net.steward.NetworkSender;
import ec.e.net.steward.ByteListener;
import ec.e.net.steward.ByteConnection;
import ec.e.net.steward.ENetAddrLocal;
import ec.e.net.steward.ENetAddrRemote;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public eclass ByteTest implements ELaunchable, NetworkListener, NetworkConnection {
    EEnvironment myEnv;
    ByteTestReceiver myReceiver;
    DataOutputStream myInnerSender;
    ByteListener myInnerListener;
    NetworkConnection myInnerConnection;

    emethod go(EEnvironment env) {
        myEnv = env;
        myReceiver = new ByteTestReceiver(this);
        String addr = myEnv.getProperty("addr");
        if (addr == null) {
            myInnerListener = new ByteListener(myEnv.vat(), new ENetAddrLocal(0), (NetworkListener) this);
        }
        else {
            this <- send(addr);
        }
    }

    emethod send(String addr) {
        InetAddress ip;
        int port;
        int colon = addr.indexOf(':');

        if (colon < 0) {
            throw new Error("gotta specify the port");
        }
        port = Integer.parseInt(addr.substring(colon+1));
        addr = addr.substring(0, colon);

        try {
            ip = InetAddress.getByName(addr);
        }
        catch (UnknownHostException e) {
            throw new Error("unknown host " + addr);
        }
        ENetAddrRemote remote = new ENetAddrRemote(ip, port);
        System.out.println("connecting to " + remote);
        myInnerConnection = (NetworkConnection) new ByteConnection(myEnv.vat(), remote, (NetworkConnection) this, myReceiver);
    }

    emethod dosend() {
        System.out.println("sending ferd");
        try {
            myInnerSender.writeBytes("ferd is a ferd");
            myInnerSender.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ferd away");
    }

    emethod doreply() {
        System.out.println("sending reply");
        try {
            myInnerSender.writeBytes("yes, he is");
            myInnerSender.flush();
            myInnerSender.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("reply away");
    }

    /* start of NetworkListener interface */

    local void listening(ENetAddrLocal listenAddr) {
        System.out.println("listening at " + listenAddr);
    }

    local void noticeConnection(NetworkConnection inner, ENetAddrLocal localAddr, ENetAddrRemote remoteAddr) {
        System.out.println("noticeConnection " + inner);
        myInnerConnection = inner;
        NetworkSender innerSender = myInnerConnection.incomingEnable((NetworkConnection) this, myReceiver) ;
        innerSender.enable();
        myInnerSender = new DataOutputStream(new BufferedOutputStream(innerSender));
    }

    local void noticeProblem(Throwable t) {
        System.err.println("ByteTest noticeProblem:");
        t.printStackTrace();
    }

    local void shutdown() {
        System.out.println("shutdown");
    }

    /* end of NetworkListener interface */

    /* start of NetworkConnection interface */

    local NetworkSender incomingEnable(NetworkConnection outer, OutputStream outerReceiver) {
        throw new RuntimeException("unimplemented");
    }

    local void outgoingEnable(NetworkSender innerSender, ENetAddrLocal localAddr) {
        System.out.println("outgoingEnable " + innerSender);
        innerSender.enable();
        myInnerSender = new DataOutputStream(new BufferedOutputStream(innerSender));
        this <- dosend();
    }

/*  local void noticeProblem(Throwable t)  already have one */

/*  local void shutdown()  already have one */

    local OutputStream getReceiver() {
        throw new RuntimeException("unimplemented");
    }

    /* end of NetworkConnection interface */
}

public class ByteTestReceiver extends OutputStream {
    ByteTest myTester;

    public ByteTestReceiver(ByteTest tester) {
        myTester = tester;
    }

    public void write(int b) throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void write(byte b[]) throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void write(byte b[], int off, int len) throws IOException {
        System.out.print("<<<");
        System.out.write(b, off, len);
        System.out.println("<<<");
        if (b.length > 0 && b[0] == 'f') {
            myTester <- doreply();
        }
    }

    public void flush() throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void close() throws IOException {
    }
}
