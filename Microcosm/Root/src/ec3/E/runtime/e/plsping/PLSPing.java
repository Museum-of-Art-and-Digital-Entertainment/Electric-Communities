package ec.e.plsping;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import ec.e.net.EARL;
import ec.e.net.Msg;
import ec.e.net.MsgConnection;
import ec.e.net.InvalidURLException;
import ec.e.net.crew.NetAddr;


public class PLSPing 
{
    public static String state = "starting PLSPing";
    
    public static void main(String args[]) {
        try {
            new PLSPingWatchdog().start();
            if (args.length < 1) {
                usage();
            }
            DataInputStream plsRefInStream = null;
            state = "opening file with PLS SturdyRef in it: " + args[0];
            try {
                plsRefInStream = new DataInputStream(new FileInputStream(args[0]));
            }
            catch (IOException e) {
                fail(e);
            }

            String url = null;
            state = "reading SturdyRef from file " + args[0];
            try {
                url = plsRefInStream.readUTF();
            }
            catch (IOException e) {
                fail(e);
            }

            state = "closing SturdyRef file " + args[0];
            try {
                plsRefInStream.close();
            }
            catch (IOException e) {
                fail(e);
            }
        
            EARL earl = null;
            state = "parsing PLS SturdyRef from file: " + args[0] + " url = <<" + url + ">>";
            try {
                earl = new EARL(url);
            }
            catch (InvalidURLException e) {
                fail(e);
            }
            String plsSearchPath[] = earl.searchPath();
            String plsRegistrarID = earl.registrarID();

            if (plsSearchPath.length < 1) {
                fail("got an empty search path while " + state);
            }
            
            NetAddr plsAddr = null;
            state = "looking up hostname to contact: " + plsSearchPath[0];
            try {
                plsAddr = new NetAddr(plsSearchPath[0]);
            }
            catch (UnknownHostException e) {
                fail(e);
            }

            Socket clientSocket = null;
            state = "trying to connect to " + plsSearchPath[0];
            try {
                clientSocket = new Socket(plsAddr.getInetAddress(), plsAddr.getPortNumber());
                
            }
            catch (IOException e) {
                fail(e);
            }

            state = "getting input and output streams";
            DataInputStream clientInStream = null;
            DataOutputStream clientOutStream = null;
            try {
                clientInStream = new DataInputStream(clientSocket.getInputStream());
                clientOutStream = new DataOutputStream(clientSocket.getOutputStream());
            }
            catch (IOException e) {
                fail(e);
            }

            byte[] IWANTpacket = null;
            state = "building startup IWANT packet";
            try {
                IWANTpacket = buildStartupPacket(MsgConnection.TOK_IWANT, plsRegistrarID, null);
            }
            catch (IOException e) {
                fail(e);
            }

            state = "writing IWANT packet" ;
            try {
                clientOutStream.writeInt(IWANTpacket.length);
                clientOutStream.write(IWANTpacket);
            }
            catch (IOException e) {
                fail(e);
            }

            state = "reading response to IWANT packet";
            int length = -1;
            try {
                length = clientInStream.readInt();
            }
            catch (IOException e) {
                fail(e);
            }
            if (length > 1000) {
                fail("bogus packet length: " + length + " while " + state);
            }
            byte response[] = new byte[length];
            try {
                clientInStream.readFully(response);
            }
            catch (IOException e) {
                fail(e);
            }

            state = "checking response from PLS" ;
            try {
                DataInputStream packetIn = new DataInputStream(new ByteArrayInputStream(response));
                byte header = packetIn.readByte();
                if (header != Msg.STARTUP) {
                    fail("didn't get STARTUP message while " + state);
                }
                int token = packetIn.readInt();
                if (token != MsgConnection.TOK_TRY) {
                    fail("didn't get TOK_TRY message while " + state);
                }
                String arg1 = packetIn.readUTF();
                String arg2 = packetIn.readUTF();
            }
            catch(IOException e) {
                fail(e);
            }
            
            System.exit(0);
        }
        catch (Throwable t) {
            fail("internal error in PLSPing while " + state, t);
        }
    }

    public static byte[] buildStartupPacket(int token, String arg1, String arg2)
    throws IOException {
        ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(outbuf);
        msgOut.writeByte(Msg.STARTUP);
        msgOut.writeInt(token);
        msgOut.writeUTF((arg1==null) ? "" : arg1);
        msgOut.writeUTF((arg2==null) ? "" : arg2);
        return outbuf.toByteArray();
    }

    public static void usage() {
        fail("usage: java ec.e.plsping.PLSPing <ProcessLocationServerSturdyRefFile>");
    }

    public static void fail(String s) {
        System.err.println(s);
        System.exit(1);
    }

    public static void fail(String s, Throwable t) {
        t.printStackTrace();
        fail(s);
    }

    public static void fail(Throwable t) {
        t.printStackTrace();
        fail("error occurred while " + state);
    }

    public static void fail() {
        fail("timeout occured while " + state);
    }
}

class PLSPingWatchdog extends Thread 
{
    public synchronized void run() {
        try {
            wait(30000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        PLSPing.fail();
    }
}
