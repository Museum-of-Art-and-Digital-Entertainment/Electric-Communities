package ec.e.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/* XXX the word transceiver is consistently spelled wrong all throughout */

class RtTranceiver {
    private long myOutMessageCount = 0;
    private long myInMessageCount = 0;
    
    static Trace tr = new Trace(false, "[RtTranceiver]");
    public static Trace ptr = new Trace(false, "[RtTranceiverProfile]");
    private static int ourSerialcounter = 1 ;
    
    private int mySerialNumber;
    private Socket myLocalSocket;
    private DataInputStream myInputStream;
    private DataOutputStream myOutputStream;
    private RtConnection myConnection;
    private RtRegistrarServer myLocalRegistrarServer;
    private String myLocalRegistrarId;
    private String myRemoteRegistrarId;
    private String myLocalPublisherId;
    private String myRemotePublisherId;
    private String myLocalSearchPath;
    private String myRemoteSearchPath;
    private String myLocalPidCertificate;
    private String myRemotePidCertificate;
    private ENetAddr myLocalAddr;
    private ENetAddr myRemoteAddr;
    private boolean myIncomingConnection;
    
    RtTranceiver(RtRegistrarServer registrarServer, String registrarId,
            String publisherId, String searchPath, Socket sock,
            ENetAddr localAddr, ENetAddr remoteAddr, String remoteRegistrarId,
            String remotePath, boolean incoming) throws IOException {
        mySerialNumber = nextSerialNumber();
        
        if (tr.tracing)
            tr.$("New RtTranceiver[" + mySerialNumber + "] Rid:" +
                 registrarId + ", Pid:" + publisherId + ", searchPath:" +
                 searchPath + ", myLocalAddr:" + localAddr +
                 ", myRemoteAddr:" + remoteAddr + ", incoming:" + incoming);
        myLocalSocket = sock ;
        myInputStream = new DataInputStream(
            new BufferedInputStream(sock.getInputStream()));
        myOutputStream = new DataOutputStream(
            new BufferedOutputStream(sock.getOutputStream()));
        myLocalRegistrarServer = registrarServer;
        myLocalRegistrarId = registrarId;
        myLocalPublisherId = publisherId;
        myLocalSearchPath = searchPath ;
        myLocalAddr = localAddr ;
        myLocalPidCertificate = "<InsertCertificateHere>" ;
        myRemoteAddr = remoteAddr ;
        myRemoteSearchPath = remotePath ;
        myRemoteRegistrarId = remoteRegistrarId ;
        myRemotePublisherId = null;
        myRemotePidCertificate = null;
        myIncomingConnection = incoming ;
    }
    
    static synchronized private int nextSerialNumber() {
        return ++ourSerialcounter;
    }
    
    final private static int IWANT = 1 ;
    final private static int INFO = 2 ;
    final private static int GO = 3 ;
    final private static int QUIT = 4 ;
    final private static int UNKNOWN = -1 ;
    
    public boolean startup() throws IOException, RtTranceiverException {
        int state;
        int token;
        String error;
        String except;
        String line[];
        
        if (tr.tracing)
            tr.$(this + " startup protocol, incoming:" + myIncomingConnection);
        
        /* XXX need to worry about blocking by the other side. Perhaps when we
           have interruptable reads we can start a timer thread. */
        
        /* the protocol:
           
           << IWANT id
           >> 220 IAM id        520 Sorry, not me.      210 TRY path
           << INFO Rid Pid path cert
           >> 223 INFO Rid Pid path cert                5xx problem report...
           << GO
           >> 222 GO
           
        */
        
        if (myIncomingConnection) {
            state = 1 ;
        } else {
            myConnection = myLocalRegistrarServer.getConnectionToRegistrar(
                myRemoteRegistrarId, null, myRemoteSearchPath, true);
            sendline("IWANT " + myRemoteRegistrarId);
            state = 2 ;
        }
        error = null;
        except = null;
        while (error == null && except == null) {
            line = getline();
            if (line == null) {
                return false;
            }
            try {
                token = Integer.parseInt(line[1]);
            } catch (NumberFormatException e) {
                if (line[1].equalsIgnoreCase("iwant"))
                    token = IWANT ;
                else if (line[1].equalsIgnoreCase("info"))
                    token = INFO ;
                else if (line[1].equalsIgnoreCase("go"))
                    token = GO ;
                else if (line[1].equalsIgnoreCase("quit"))
                    token = QUIT ;
                else
                    token = UNKNOWN;
            }
            if (token >= 500 && token <= 599) {
                except = line[0] ;
                break;
            }
            switch (state) {
                case 1:
                    if (token == IWANT) {
                        if (line[2].equals(myLocalPublisherId) ||
                                line[2].equals(myLocalRegistrarId)) {
                            state = 3;
                            sendline("220 IAM " + line[2]);
                        } else
                            error = "221 Sorry, not me." ;
                    }
                    else
                        error = "500 Protocol error, expected IWANT, got: " +
                            line[0] ;
                    break;
                case 2:
                    if (token == 220) { /* IAM */
                        if (line[3].equals(myRemoteRegistrarId)) {
                            state = 4;
                            sendline("INFO " + myLocalRegistrarId + " " +
                                myLocalPublisherId + " " + myLocalSearchPath +
                                " " + myLocalPidCertificate);
                        } else
                            error = "522 wrong id in IAM, expected " +
                                myRemoteRegistrarId + " got: " + line[0];
                    } else if (token == 210) { /* 210 TRY addr */
                        myConnection.extendSearchVector(line[3]);
                        myLocalSocket.close();
                        return false;
                    } else if (token == 221) { /* Sorry, not me. */
                        myLocalSocket.close();
                        return false;
                    } else
                        error =
                            "500 Protocol error, expected IAM or TRY, got: " +
                            line[0] ;
                    break;
                case 3:
                    if (token == INFO) { /* INFO Rid Pid path cert */
                        if (tr.tracing)
                            tr.$(this + " looking up connection");
                        myConnection =
                            myLocalRegistrarServer.getConnectionToRegistrar(
                                line[2], line[3], line[4], true);
                        if (tr.tracing)
                            tr.$(this + " extending search vector '" +
                                 line[4] + "'");
                        myConnection.extendSearchVector(line[4]);
                        myRemoteRegistrarId = line[2] ;
                        myRemotePublisherId = line[3] ;
                        myRemoteSearchPath = line[4] ;
                        myRemotePidCertificate = line[5] ;
                        if (!verifyCertificate(myRemotePidCertificate,
                                               myRemoteRegistrarId,
                                               myRemotePublisherId)) {
                            error = "553 certificate not accepted" ;
                            break;
                        }
                        
                        if (myConnection.newTranceiverLives(this,
                                myIncomingConnection)) {
                            state = 5;
                            sendline("223 INFO " + myLocalRegistrarId + " " +
                                myLocalPublisherId + " " + myLocalSearchPath +
                                " " + myLocalPidCertificate);
                        } else
                            error = "229 Duplicate connection, dropping.";
                    } else
                        error = "500 Protocol error, expected INFO, got: " +
                            line[0] ;
                    break;
                case 4:
                    if (token == 223) { /* 223 INFO Rid Pid path cert */
                        if (tr.tracing) tr.$(this + " looking up connection");
                        myConnection =
                            myLocalRegistrarServer.getConnectionToRegistrar(
                                line[3], line[4], line[5], true);
                        if (tr.tracing)
                            tr.$(this + " extending search vector '" +
                                 line[5] + "'");
                        myConnection.extendSearchVector(line[5]);
                        if (!myRemoteRegistrarId.equals(line[3]) &&
                                !myRemoteRegistrarId.equals(line[4])) {
                            error = "524 id mismatch between IAM and INFO: " +
                                line[0] ;
                            break;
                        }
                        myRemoteRegistrarId = line[3] ;
                        myRemotePublisherId = line[4] ;
                        myRemoteSearchPath = line[5] ;
                        myRemotePidCertificate = line[6] ;
                        if (!verifyCertificate(myRemotePidCertificate,
                                               myRemoteRegistrarId,
                                               myRemotePublisherId)) {
                            error = "554 certificate not accepted" ;
                            break;
                        }
                        
                        if (myConnection.newTranceiverLives(this,
                                myIncomingConnection)) {
                            state = 6;
                            sendline("GO");
                        } else {
                            if (tr.tracing)
                                tr.$(this + " my side dies at first cut.");
                            error = "QUIT";
                        }
                    } else if (token == 229) { /* Dup. connection, dropping. */
                        if (tr.tracing)
                            tr.$(this + " remote duplicate detected.");
                        myLocalSocket.close();
                        return false;
                    } else
                        error = "500 Protocol error, expected INFO, got: " +
                            line[0] ;
                    break;
                case 5:
                    if (token == GO) {
                        if (myConnection.newTranceiverLives(this,
                                myIncomingConnection)) {
                            sendline("225 GO");
                            if (tr.tracing)
                                tr.$(this + " this tranceiver lives.");
                            myConnection.setTranceiver(this);
                            return true;
                        } else {
                            sendline("228 BYE");
                            if (tr.tracing)
                                tr.$(this + " my side dies at second cut.");
                            myLocalSocket.close();
                            return false;
                        }
                    } else if (token == QUIT) {
                        if (tr.tracing)
                            tr.$(this + " got QUIT from remote side.");
                        myLocalSocket.close();
                        return false;
                    } else
                        error = "500 Protocol error, expected GO, got: " +
                            line[0] ;
                    break;
                case 6:
                    if (token == 225) { /* GO */
                        if (myConnection.newTranceiverLives(this,
                                myIncomingConnection)) {
                            if (tr.tracing)
                                tr.$(this + " this tranceiver lives.");
                            myConnection.setTranceiver(this);
                            return true;
                        } else {
                            if (tr.tracing)
                                tr.$(this + " my side dies at second cut.");
                            myLocalSocket.close();
                            return false;
                        }
                    } else if (token == 228) { /* BYE */
                        if (tr.tracing)
                            tr.$(this + " remote duplicate detected.");
                        myLocalSocket.close();
                        return false;
                    } else
                        error = "500 Protocol error, expected GO, got: " +
                            line[0] ;
                    break;
                default:
                    except = "RtTranceiver startup: unknown state: " + state ;
                    break;
                }
        }
        if (error != null) {
            sendline(error);
            if (except == null)
                except = "Local end reported error: " + error ;
        }
        if (except == null)
            except = "RtTranceiver: This never happens!" ;
        myLocalSocket.close();
        throw new RtTranceiverException(except);
    }
    
    private void sendline(String s) throws IOException {
        if (tr.tracing)
            tr.$(this + " sending:  '" + s + "'");
        myOutputStream.writeBytes(s + "\n");
        myOutputStream.flush();
    }
    
    private String getline()[] throws IOException {
        String line = myInputStream.readLine();
        if (line == null) {
            if (tr.tracing)
                tr.$(this + " received: EOF");
            return null;
        }
        if (tr.tracing)
            tr.$(this + " received: '" + line + "'");
        int i = 2 ;
        int space = 0 ;
        while ((space = line.indexOf(' ', space)) >= 0) {
            i++;
            space++;
        }
        if (i < 6)
            i = 6 ;
        String ret[] = new String[i];
        ret[0] = line;
        i = 1 ;
        space = 0 ;
        int nextspace = 0;
        while ((nextspace = line.indexOf(' ', space)) >= 0) {
            ret[i++] = line.substring(space, nextspace);
            space = nextspace + 1;
        }
        ret[i] = line.substring(space);
        return ret;
    }
    
    private boolean verifyCertificate(String cert, String rRid, String rPid) {
        /* XXX perhaps we should check something here... */
        return true;
    }
    
    RtMsgQEntry getMessage() {
        try {
            long requestID;
            int msgType = myInputStream.readInt();
            if (tr.tracing)
                tr.$(this + " Message type is " +
                     RtMsg.stringForMessage(msgType));
            if (RtMsg.validMessage(msgType) == false) {
                tr.$(this + " Message type " + msgType +
                     " is invalid, closing connection");
                throw new RtRuntimeException("Invalid message type " +
                                             msgType);
            }
            int msgSize = myInputStream.readInt();
            if (tr.tracing)
                tr.$(this + " Message size is " + msgSize);
            if (msgSize > 100000) {
                tr.$(this + " Message size " + msgSize +
                     " is bogusly huge, closing connection");
                throw new RtRuntimeException("Bogus huge message size " +
                                             msgSize);
            }
            byte msg[] = new byte[msgSize];
            
            myInputStream.readFully(msg, 0, msgSize);
            /*if (tr.tracing)
                  traceMessage(msg, this + " Read message type " + msgType); */
            
            if (ptr.tracing)
                ptr.$(this + " Received message " + myInMessageCount++ +
                      " at " + System.currentTimeMillis());
            return(new RtMsgQEntry(msgType, msgSize, (long)0, msg));
        } catch (Exception e) {
            tr.$(this + " Connection caught exception reading");
            myConnection.suspendConnection();
            return null;
        }
    }
    
    synchronized void reallySendBlobBytes(byte msg[], int msgType)
            throws RtConnectionFailedException {
        try {
            if (tr.tracing)
                tr.$(this + " writing message type " +
                     RtMsg.stringForMessage(msgType));
            /*if (tr.tracing)
                traceMessag(msg, this + " Writing message type " + msgType); */
            if (ptr.tracing)
                ptr.$(this + " Sending message " + myOutMessageCount++ +
                      " at " + System.currentTimeMillis());
            myOutputStream.writeInt(msgType);
            myOutputStream.writeInt(msg.length);
            myOutputStream.write(msg);
            myOutputStream.flush();
        } catch (Exception e) {
            if (tr.tracing) {
                tr.$(this + " Exception trying to send msg");
                e.printStackTrace();
            }
            myConnection.suspendConnection();
            throw new RtConnectionFailedException(this +
                " exception sending message: " + e);
        }
    }
    
    void teardown() {
        if (tr.tracing)
            tr.$("teardown(), closing socket");
        try {
            myLocalSocket.close();
        } catch (Exception e) {
            /* XXX bad exception usage? -- fix? */
            if (tr.tracing) {
                tr.$(this + " exception closing socket");
                e.printStackTrace();
            }
        }
        myConnection.setTranceiver(null);
    }
    
    private void traceMessage(byte msg[], String note) {
        int i;
        char msgChars[] = new char[msg.length * 2];
        for (i = 0; i < msg.length; i++) {
            int j = i * 2;
            byte b = msg[i];
            
            msgChars[j] = makeCharacterForNumber((b >> 4) & 15);
            msgChars[j+1] = makeCharacterForNumber(b & 15);
        }
        String s = new String(msgChars);
        tr.$(note + " (length " + msg.length + ") " + s);
        /* tr.printStackTrace(); */
    }
    
    private char makeCharacterForNumber(int n) {
        if (n > 9)
            return (char)('a' + n - 10);
        else 
            return (char)('0' + n);
    }
    
    public String toString() {
        if (myRemoteRegistrarId == null)
            return "RtTranceiver(" + mySerialNumber + ", " +
                myLocalRegistrarId + "<->" + myRemoteAddr + ")";
        return "RtTranceiver(" + mySerialNumber + ", " + myLocalRegistrarId +
            "<->" + myRemoteRegistrarId + ")";
    }
}

public class RtTranceiverException extends Exception {
    public RtTranceiverException(String message) {
        super(message);
    }
}
