package ec.app.net;

import ec.trace.Trace;
import ec.util.HexStringUtils;
import ec.e.util.SimpleQueue;
import ec.e.util.SimpleQueueWriter;
import ec.e.util.SimpleQueueReader;
import ec.e.run.ELaunchable;
import ec.e.run.EEnvironment;
import ec.e.run.Vat;
import ec.e.file.EStdio;
import ec.e.io.EInputHandler;
import ec.e.io.EConsoleMaker;
import ec.e.quake.TimeMachine;
import ec.e.lang.EString;
import ec.e.net.NetworkConnection;
import ec.e.net.NetworkSender;
import ec.e.net.NetworkListener;
import ec.e.net.ByteListener;
import ec.e.net.ByteConnection;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Hashtable;
import ec.e.net.PLSControllerServer;
import ec.e.net.Registrar;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRef;


public eclass Intercepter implements ELaunchable, EInputHandler {
    TimeMachine tm;
    PLSControllerServer pls = (PLSControllerServer) EUniChannel.construct(PLSControllerServer.class);
    EUniDistributor pls_dist = EUniChannel.getDistributor(pls);
    String SpoofedRID;
    String myLastLine = "" ;
    int myLastIndex = -1 ;
    Hashtable myConnections = new Hashtable();
    
    emethod go(EEnvironment env) {
        try {
            String location = env.getProperty("Target") ;
            String PLSController = env.getProperty("PLSController");
            
            EConsoleMaker consoleMaker = EConsoleMaker.summon(env);
            consoleMaker.makeConsole(this, EStdio.in(), null);

            tm = TimeMachine.summon(env);
            
            Registrar registrar = Registrar.summon(env);
            registrar.onTheAir();

            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef targetRef = importer.importRef(location );
            SturdyRef PLSControllerRef = importer.importRef(PLSController);

            PLSControllerRef.followRef(pls_dist);

            // XXX commented out in move from ec3 to ec4.  The program
            // won't work without this, but there needs to be some way
            // to extract this info given sufficient privaledge.  It
            // can be gotten out with a SturdyRef*Exporter and
            // appropriate string processing, so that could be used to
            // extract it here.  Not worth doing until someone wants
            // to use this again.  -emm

            //SpoofedRID = targetRef.myRemoteRID;
            String cmd = "lookup " + SpoofedRID ;
            String expect = SpoofedRID + " --> " ;
        
            EString result = (EString) EUniChannel.construct(EString.class);
            EUniDistributor result_dist = EUniChannel.getDistributor(result);
        
            pls <- command(cmd, result_dist);
            ewhen result (String reply) {
                if (reply.startsWith(expect)) {
                    reply = reply.substring(expect.length());
                    reply = reply.substring(0, reply.length()-1);
                    EStdio.out().println("rid " + SpoofedRID + " currently registered at location " + reply);
                    new SpoofListener(env.vat(), "localhost:0", reply, this);
                }
            }
        }
        catch (Throwable t) {
            EStdio.reportException(t);
            tm <- suicide(1);
        }
    }

    emethod handleInput(String line) {
         boolean incoming;
         int index;

         if (line.length() == 0) {
             line = myLastLine;
         }
         myLastLine = line;
         EStdio.out().println(">>" + line);
         if (line.startsWith("i")) {
             incoming = true;
             line = line.substring(1);
         }
         else if (line.startsWith("o")) {
             incoming = false;
             line = line.substring(1);
         }
         else {
             EStdio.out().println("must specify [i]ncoming or [o]utgoing connection");
             return;
         }
         if (line.length() == 0) {
             index = myLastIndex;
         }
         else {
             index = Integer.parseInt(line);
             myLastIndex = index;
         }
         SpoofConnection conn = (SpoofConnection)myConnections.get(new Integer(index));
         if (conn == null) {
             EStdio.out().println("connection " + index + " does not exist");
             return;
         }
         conn.release(incoming);
    }

    emethod addConnection(SpoofConnection conn, int index) {
        myConnections.put(new Integer(index), conn);
    }

    emethod listening(String listenAddr) {
        String cmd = "add " + SpoofedRID + " " + listenAddr;
        EStdio.out().println(cmd);
        EString result = (EString) EUniChannel.construct(EString.class);
        EUniDistributor result_dist = EUniChannel.getDistributor(result);
        pls <- command(cmd, result_dist);
        ewhen result (String reply) {
            EStdio.out().println(reply);
        }
    }
}

class SpoofListener implements NetworkListener 
{
    static private final Trace tr = new Trace("ec.e.net.SpoofListener");

    private NetworkListener myByteListener;
    private String myRelayAddr;
    private Intercepter myHandler;
    private Vat myVat;
    private int myIndex = 0;
    
    SpoofListener(Vat vat, String listenAddr, String relayAddr, Intercepter handler) {
        myByteListener = new ByteListener(vat, listenAddr, (NetworkListener)this);
        myRelayAddr = relayAddr;
        myHandler = handler;
        myVat = vat;
    }

    // start of NetworkListener interface
    public void listening(String listenAddr) {
        tr.errorm("listening at " + listenAddr);
        myHandler <- listening(listenAddr);
    }
    
    public void noticeConnection(NetworkConnection outer, String localAddr, String remoteAddr) {
        tr.errorm("accepted connection from " + remoteAddr + " index " + myIndex);
        SpoofConnection conn = new SpoofConnection(outer, localAddr, remoteAddr, myRelayAddr, myHandler, myVat, myIndex);
        myHandler <- addConnection(conn, myIndex++);
    }
    
    public void noticeProblem(Throwable t, boolean listenProblem) {
        tr.errorm("SpoofListener noticeProblem", t);
    }
    
    public void shutdown() {
        myByteListener.shutdown();
    }

    public void suspend() {
        myByteListener.suspend();
    }

    public void resume() {
        myByteListener.resume();
    }
}


class SpoofConnection extends OutputStream implements NetworkConnection
{
    static private final Trace tr = new Trace("ec.e.net.SpoofConnection");
    
    NetworkSender myOuterSender;
    private Intercepter myHandler;
    private SpoofConnectionOutgoing myOutgoingConnection;
    private int myIndex;
    private SimpleQueueWriter myIncomingQueueWriter;
    private SimpleQueueReader myIncomingQueueReader;
    private SimpleQueueWriter myOutgoingQueueWriter;
    private SimpleQueueReader myOutgoingQueueReader;
    
    SpoofConnection(NetworkConnection outerConnection, String localAddr, String remoteAddr, String relayAddr, Intercepter handler, Vat vat, int index) {
        if (tr.debug && Trace.ON) tr.$("new SpoofConnection from " + remoteAddr);
        myOutgoingConnection = new SpoofConnectionOutgoing(vat, this, relayAddr, index);
        myHandler = handler;
        myIndex = index;
        myOuterSender = outerConnection.incomingSetup((NetworkConnection)this, (OutputStream)this);
        try {
            myOuterSender.enable();
        }
        catch (IOException e) {
            noticeProblem(e);
        }
        SimpleQueue queue = new SimpleQueue();
        myIncomingQueueWriter = queue.writer();
        myIncomingQueueReader = queue.reader();
        queue = new SimpleQueue();
        myOutgoingQueueWriter = queue.writer();
        myOutgoingQueueReader = queue.reader();
    }
    
    // start of NetworkConnection interface
    public NetworkSender incomingSetup(NetworkConnection inner, OutputStream innerReceiver) {
        // end of the line.  we don't do this
        throw new RuntimeException("unimplemented");
    }
    
    public void outgoingSetup(NetworkSender outerSender, String localAddr) {
        // no outgoing connections
        throw new RuntimeException("unimplemented");
    }
    
    public void noticeProblem(Throwable t) {
        tr.errorm("SpoofConnection noticeProblem", t);
    }
    
    public void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        myIncomingQueueWriter.enqueue(null);
    }
    
    public OutputStream getReceiver() {
        // no outgoing connections
        throw new RuntimeException("unimplemented");
        //return (OutputStream)this;
    }

    // Nop these two statistics routines from NetworkConnection
    public void updateSendCounts(int messageLength, int compressedLength) {}
    public void updateReceivedCounts(int messageLength, int compressedLength) {}

    // start of OutputStream interface
    public void write(int b) throws IOException {
        throw new RuntimeException("unimplemented");
    }
    
    public void write(byte b[]) throws IOException {
        tr.errorm(myIndex + "-I >> " + HexStringUtils.byteArrayToReadableHexString(b));
        myIncomingQueueWriter.enqueue(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void flush() throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void close() throws IOException {
        tr.errorm(myIndex + "-I **close()**");
        myIncomingQueueWriter.enqueue(null);
    }

    void relay(byte b[]) {
        tr.errorm(myIndex + "-O >> " + HexStringUtils.byteArrayToReadableHexString(b));
        myOutgoingQueueWriter.enqueue(b);
    }

    void relayClose() {
        tr.errorm(myIndex + "-O **close()**");
        myOutgoingQueueWriter.enqueue(null);
    }

    void release(boolean incoming) {
        EStdio.out().println("releasing " + myIndex + (incoming ? "-I" : "-O"));
        if (incoming) {
            if (myIncomingQueueReader.hasMoreElements()) {
                byte b[] = (byte[])myIncomingQueueReader.nextElement();
                try {
                    if (b == null) {
                        tr.errorm("releasing " + myIndex + "-I **close()**");
                        myOutgoingConnection.relayClose();
                    }
                    else {
                        tr.errorm("releasing " + myIndex + "-I >> " + HexStringUtils.byteArrayToReadableHexString(b));
                        myOutgoingConnection.relay(b);
                    }
                }
                catch (IOException e) {
                    noticeProblem(e);
                }
            }
            else {
                EStdio.out().println("queue is empty");
            }
        }
        else {
            if (myOutgoingQueueReader.hasMoreElements()) {
                byte b[] = (byte[])myOutgoingQueueReader.nextElement();
                try {
                    if (b == null) {
                        tr.errorm("releasing " + myIndex + "-O **close()**");
                        myOuterSender.close();
                    }
                    else {
                        tr.errorm("releasing " + myIndex + "-O >> " + HexStringUtils.byteArrayToReadableHexString(b));
                        myOuterSender.write(b);
                    }
                }
                catch (IOException e) {
                    noticeProblem(e);
                }
            }
            else {
                EStdio.out().println("queue is empty");
            }
                        
        }
    }
}


class SpoofConnectionOutgoing extends OutputStream implements NetworkConnection
{
    static private final Trace tr = new Trace("ec.e.net.SpoofConnectionOutgoing");

    private SpoofConnection myIncomingConnection;
    private NetworkSender myOuterSender;
    private int myIndex;
    
    SpoofConnectionOutgoing(Vat vat, SpoofConnection incomingConnection, String relayAddr, int index) {
        myIncomingConnection = incomingConnection;
        myIndex = index;
        new ByteConnection(vat, relayAddr, (NetworkConnection)this, (OutputStream)this);
    }
    
    // start of NetworkConnection interface
    public NetworkSender incomingSetup(NetworkConnection inner, OutputStream innerReceiver) {
        throw new RuntimeException("unimplemented, no incoming SpoofConnectionOutgoing's");
    }
    
    public void outgoingSetup(NetworkSender outerSender, String localAddr) {
        myOuterSender = outerSender;
        try {
            myOuterSender.enable();
        }
        catch (IOException e) {
            noticeProblem(e);
        }
    }
    
    public void noticeProblem(Throwable t) {
        tr.errorm("SpoofConnectionOutgoing connection problem", t);
    }

    public void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        tr.errorm("SpoofConnectionOutgoing shutdown");
    }

    public OutputStream getReceiver() {
        return this;
    }

    // Nop these two statistics routines from NetworkConnection
    public void updateSendCounts(int messageLength, int compressedLength) {}
    public void updateReceivedCounts(int messageLength, int compressedLength) {}
    

    // start of OutputStream interface
    public void write(int b) throws IOException {
        throw new RuntimeException("unimplemented");
    }
    
    public void write(byte b[]) throws IOException {
        myIncomingConnection.relay(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void flush() throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void close() throws IOException {
        myIncomingConnection.relayClose();
    }

    void relay(byte b[]) throws IOException {
        myOuterSender.write(b);
    }

    void relayClose() throws IOException {
        myOuterSender.close();
    }
}
