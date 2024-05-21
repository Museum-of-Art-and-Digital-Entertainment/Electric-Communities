package ec.e.net;

import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.NetworkSender;
import ec.e.file.EStdio;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PLSConnection extends OutputStream implements NetworkConnection
{
    static private final Trace tr = new Trace("ec.e.net.PLSConnection");
    
    NetworkSender myOuterSender;
    ProcessLocationServerHelper myProcessLocationServerHelper;
    
    public PLSConnection(ProcessLocationServerHelper helper, NetworkConnection outerConnection, String localAddr, String remoteAddr) {
        if (tr.debug && Trace.ON) tr.$("new PLSConnection from " + remoteAddr);
        myProcessLocationServerHelper = helper;
        myOuterSender = outerConnection.incomingSetup((NetworkConnection)this, (OutputStream)this);
        try {
            myOuterSender.enable();
        }
        catch (IOException e) {
            noticeProblem(e);
        }
    }

    private void sendPacket(byte packetType, int token, String arg) {
        if (tr.verbose && Trace.ON) tr.$("sendPacket(" + packetType + ", " + MsgConnection.tokName(token) + ", " + arg + ")");
        try {
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(outbuf);
            msgOut.writeByte(packetType);
            msgOut.writeInt(token);
            msgOut.writeUTF(arg);
            msgOut.writeUTF("");
            myOuterSender.write(outbuf.toByteArray());
        }
        catch (IOException e) {
            noticeProblem(e);
        }
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
        // ignore problems, could be just some bozo yapping at us.
        if (tr.debug && Trace.ON) {
            tr.$("PLSConnection noticeProblem (probably uninteresting):");
            EStdio.reportException(t, false /* the trace reveals the reporting location */);
        }
    }
    
    public void noticeShutdown(byte[] sendIV, byte[] receiveIV) {
        if (tr.debug && Trace.ON) tr.$("don't care about shutdown");
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
    
    // should be an IWANT packet, respond with TRY or NOT_ME or an error.
    public void write(byte b[]) throws IOException {
        try {
            DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(b));

            byte header = packetIn.readByte();
            int token = packetIn.readInt();
            String arg1 = packetIn.readUTF();
            String arg2 = packetIn.readUTF();
            packetIn.close();

            if (tr.verbose && Trace.ON) tr.$("received packet " + MsgConnection.tokName(token) + " " + arg1 + " " + arg2);
            switch (header) {
                case Msg.PROTOCOL_ERROR:
                    sendPacket(Msg.PROTOCOL_ERROR, MsgConnection.TOK_ERR_PROTOCOL, "incoming protocol version 0.0 is not supported, use version " + Msg.Version);
                    break;
                case Msg.PROTOCOL_VERSION:
                    int theirVersionInt = token;
                    String theirVersion = arg1;
                    for (int i=0; i<Msg.Version.length; i++) {
                        if (theirVersion.equals(Msg.Version[i])) {
                            return;
                        }
                    }
                    String err = "incoming protocol version " + theirVersion;
                    err += " is not supported, use versions ";
                    for (int i=0; i<Msg.Version.length-1; i++) {
                        err += (Msg.Version[i] + ", ");
                    }
                    err += Msg.Version[Msg.Version.length-1];
                    sendPacket(Msg.STARTUP, MsgConnection.TOK_ERR_PROTOCOL, err);
                    break;
                case Msg.STARTUP:
                    if (token != MsgConnection.TOK_IWANT) {
                        sendPacket(Msg.STARTUP, MsgConnection.TOK_ERR_PROTOCOL, "Expected TOK_IWANT packet");
                        return;
                    }
                    String wantedRegistrarID = arg1;
                    String searchPath = myProcessLocationServerHelper.get(wantedRegistrarID);
                    if (tr.verbose && Trace.ON) tr.$("RID=" + wantedRegistrarID + ", result=" + searchPath);
                    if (searchPath == null) {
                        sendPacket(Msg.STARTUP, MsgConnection.TOK_NOT_ME, "I don't know " + wantedRegistrarID);
                        return;
                    }
                    sendPacket(Msg.STARTUP, MsgConnection.TOK_TRY, searchPath);
                    break;
                default:
                    sendPacket(Msg.STARTUP, MsgConnection.TOK_ERR_PROTOCOL, "Expected Msg.STARTUP packet");
                    return;
            }
            
        }
        catch (Exception e) {
            sendPacket(Msg.STARTUP, MsgConnection.TOK_ERR_PROTOCOL, "Exception handling TOK_IWANT packet: " + e);
        }
    }

    public void write(byte b[], int off, int len) throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void flush() throws IOException {
        throw new RuntimeException("unimplemented");
    }

    public void close() throws IOException {
        if (tr.debug && Trace.ON) tr.$("don't care about close");
    }
}
