package ec.e.net.crew;

import ec.util.HexStringUtils;
import ec.util.NestedIOException;
import ec.util.Native;
import java.io.OutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import ec.e.start.FragileRootHolder;
import ec.e.net.steward.NetworkSender;

/**
   A thread to read bytes from an InputStream and pass them to a
   ByteReceiver inside the vat.<p>

   Created by RawConnection when a Socket becomes available.  This
   will be either after an outgoing connection succeeds, or after an
   incoming connection has been detected in ListenThread.<p>

   The thread is not start()'d until SendThread.enable() is called
   (which calls RawConnection.enable(), which in turn calls
   RecvThread.start()).  This allows the entire protocol stack inside
   the vat to be set up so none of the receivers have to care about
   queueing.
  */
class RecvThread extends Thread {
    static Trace tr = new Trace("ec.e.net.RecvThread");
    // Index: 4=isCompressing, 2=isDoingCrypto, 1=isDoingMac
    //                                             0   1   2   3   4   5   6   7
    static private final int[] theHeaderLengths = {4, 24, -1, 24,  1, 24, -1, 24};
    
    private InputStream myInputStream;
    private FragileRootHolder myInnerReceiver;
    private RawConnection myConnection;
    private boolean myTerminateFlag;
    private int myBufferLength;

    private byte[] myHeader = new byte[4];  // Protocol negotation header
    private boolean myChangeProtocolIsOk = false;

    // The following fields are for aggragating messages
    private boolean myIsAggragating = false;
    private Vector myMessagesToPass = new Vector();

    // The following fields are for encryption
    private boolean myIsDoingCrypto = false;
    private Decrypt3DES myDecrypt = null;

    // The following fields are for MAC calculation
    private boolean myIsDoingMac = false;
    private byte[] myMACKey; 
    private MessageDigest mySHA1;
    private byte[] myMAC;

    // The following fields are for compression
    private boolean myIsCompressingMsgLengths = false;
    private boolean myIsCompressing = false;
    private boolean myUseSmallZip;

    // Spam for total timing
    // only used if tracing
    private long myTotalTime = 0;

    /**
       Construct a new RecvThread.

       @param inputStream stream to read data from.
       @param receiver a FragileRootHolder to an OutputStream to write data to.
       @param connection a RawConnection to send problem reports to.
       @param bufferLength number of bytes to read at one time.  A new
        buffer of this length is allocated for each read, so it
        shouldn't be too big.
      */
    public RecvThread(InputStream inputStream, FragileRootHolder innerReceiver,
                      RawConnection connection, int bufferLength, String remoteAddr, boolean killerhack) {
        super("RecvThread-" + remoteAddr);
        myInputStream = inputStream ;
        myInnerReceiver = innerReceiver;
        myConnection = connection;
        myTerminateFlag = false;
        myBufferLength = bufferLength;
        if (killerhack) new Thread(new Killer(myConnection)).start();
    }

    /**
     * The actual connection receive thread -- the asynchronous part.
     */
    public void run() {
        /* At this level, all errors are considered unrecoverable. We catch
           them all and pitch them to our connection. */
        try {
            /* Loop reading blobs until somebody tells us to stop or we get
               blown away by an error. */
            if (tr.tracing)
                tr.$("running...");
            while (!myTerminateFlag) {
                readAndProcessMessage();
            }
            if (tr.tracing)
                tr.$("I've been asked to shutdown");
        } catch (Throwable t) {
            if (tr.tracing)
                tr.$("caught exception: " + t);
            /* If the connection thread exits, it will close the socket, which
               in turn will cause us to catch an IOException here. But that
               should not be treated as an error, since it's the normal way we
               get unblocked during a shutdown. So don't bother telling the
               keeper if we catch an exception and the terminate flag is set.
               Suicide is not an error (here). */
            if (!myTerminateFlag) {
                myConnection.noticeProblem(t);
            }
        }
        finally {
            if (tr.tracing)
                tr.$("terminated");
            if (!myTerminateFlag) {
                myConnection.shutdown();
            }
            try {
                synchronized (myInnerReceiver.vatLock()) {
                    ((OutputStream) myInnerReceiver.held()).close();
                }
            }
            catch (Exception e) {
                myConnection.noticeProblem(e);
            }
            finally {
                myConnection = null;
                myInnerReceiver = null;
            }
        }
    }

    /**
       Shutdown this thread.  The thread doesn't actually exit until
       the read unblocks.
     */
    void shutdown() {
        myTerminateFlag = true;
    }

    /**
     * Get the current IV for decryption
     */
    /*package*/ byte[] /*NilOK*/ getIV() {
        if (null != myDecrypt) return myDecrypt.getIV();
        return null;
    }

    /*package*/ void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException {
        if (tr.verbose && Trace.ON) tr.verbosem("CryptoChange, agg="+isAggragating 
                                    +" MACt="+macType+" ENCt="+encryptionType
                                    +" Comp="+isCompressing+" Small="+useSmallZip);

        if (!myChangeProtocolIsOk) {
            throw new IOException("Must be processing input message to change protocol");
        } 
        myIsAggragating = isAggragating;    // Save aggragation switch
        int headerLengthIndex = 0;          // For calculating the header length

        if ("None".equals(macType)) {
            myIsDoingMac = false;
        } else if ("SHA1".equals(macType)) {
            headerLengthIndex += 1;
            myIsDoingMac = true;
            myMACKey = macKey;
            myMAC = new byte[20];
            try {
                mySHA1 = MessageDigest.getInstance("SHA");
            } catch(NoSuchAlgorithmException e) {
                tr.errorm("Unable to build SHA", e);
                throw new NestedIOException("Unable to build SHA", e);
            }
        }  else {
            throw new IOException("Invalid MAC type "+macType);
        }

        if ("None".equals(encryptionType)) {
            myIsDoingCrypto = false;
        } else if ("3DES".equals(encryptionType)) {
            headerLengthIndex += 2;
            myIsDoingCrypto = true;
            myDecrypt = new Decrypt3DES(encryptionKey, inIV);
        } else {
            throw new IOException("Invalid encryption type "+encryptionType);
        }
        if (isCompressing) {
            headerLengthIndex += 4;
            myIsCompressingMsgLengths = false;
            myIsCompressing = true;
            myUseSmallZip = useSmallZip;
        } else {
            myIsCompressing = false;
            myIsCompressingMsgLengths = myIsAggragating;

        }
        int len = theHeaderLengths[headerLengthIndex];
        if (len < 0) {
            throw new IOException("Invalid combination of link options, MAC="
                    +macType+" Crypto="+encryptionType+" Compress="
                    +myIsCompressing);
        }
        myHeader = new byte[len];
    }


    /**
       Process input data.
     */
    private void readAndProcessMessage() throws IOException {
        long cryptoTime = 0;
        long macTime = 0;
        long zipTime = 0;
        long startTime = 0;

        // Read the header
        fillArray(myHeader, 0, myHeader.length);

        if (myIsDoingCrypto) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            myDecrypt.decrypt(myHeader);
            if (tr.event && Trace.ON) cryptoTime += Native.queryTimer() - startTime;
        }
        if (tr.verbose && Trace.ON) {
            tr.verbosem("Decrypted header: "
                + HexStringUtils.byteArrayToReadableHexString(myHeader));
        }

        int length;
        int cryptoBlockOffset = 0; // Message data decrypted with header
        if (myIsCompressing || myIsCompressingMsgLengths) {
            int l1 = myHeader[0];    // First byte of the length
            if (0 == (l1 & 0x80)) {           // len < 128
                length = l1;
                cryptoBlockOffset = 3;
            } else {
                byte[] lenHead;
                if (1 == myHeader.length) {
                    if (0x80 == (l1 & 0xc0)) {
                        lenHead = new byte[2];
                    } else if (0xc0 == (l1 & 0xe0)) {
                        lenHead = new byte[3];
                    } else if (0xe0 == (l1 & 0xf0)) {
                        lenHead = new byte[4];
                    } else  {                         
                        throw new IOException(
                            "Invalid compressed length code"
                            + HexStringUtils.byteArrayToReadableHexString(myHeader));
                    }
                    fillArray(lenHead, 1, lenHead.length);
                } else {
                    lenHead = myHeader;
                }
                 
                if (0x80 == (l1 & 0xc0)) { // len < 16,384
                    length = ((l1 & 0x3f) << 8) |
                             ((lenHead[1] & 0xff));
                    cryptoBlockOffset = 2;
                } else if (0xc0 == (l1 & 0xe0)) { // len < 2,097,152
                    length = ((l1 & 0x1f)          << 16) |
                             ((lenHead[1] & 0xff) <<  8) |
                             ((lenHead[2] & 0xff))       ;
                    cryptoBlockOffset = 1;
                } else if (0xe0 == (l1 & 0xf0)) { // len < 2**28
                    length = ((l1 & 0x0f)          << 24) |
                             ((lenHead[1] & 0xff) << 16) |
                             ((lenHead[2] & 0xff) <<  8) |
                             ((lenHead[3] & 0xff))       ;
                    // cryptoBlockOffset = 0; already set
                } else  {                         
                    throw new IOException(
                            "Invalid compressed length code"
                            + HexStringUtils.byteArrayToReadableHexString(myHeader));
                }
            }
        } else {            // Not compressing
            length = ((myHeader[0] & 0xff) << 24) |
                     ((myHeader[1] & 0xff) << 16) |
                     ((myHeader[2] & 0xff) <<  8) |
                      (myHeader[3] & 0xff)        ;
        }
        if (tr.verbose && Trace.ON) {
            tr.verbosem("incoming packet len = " + length);
        }
        if (length > NetworkSender.MAX_PACKET_LENGTH || length < 0) {
            throw new IOException("Packet too large: " + length +
                                  " > " + NetworkSender.MAX_PACKET_LENGTH);
        }
        byte[] message = new byte[
                        myIsDoingCrypto ? ((length-cryptoBlockOffset+7)
                                        &0xfffffff8)
                                        + cryptoBlockOffset
                                     : length];
        int compressedLength = length + myHeader.length;
        if (cryptoBlockOffset > 0) { 
            // Copy compressed data decrypted with header
            System.arraycopy(myHeader,24-cryptoBlockOffset, 
                             message,0, cryptoBlockOffset);
        }
        if (myIsDoingMac) {
            System.arraycopy(myHeader,4-cryptoBlockOffset, 
                         myMAC, 0, 20); // Save MAC
        }

        // Read rest of message into the buffer allocated for it
        fillArray(message, cryptoBlockOffset, message.length-cryptoBlockOffset);

        if (myIsDoingCrypto) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            myDecrypt.decrypt(message, cryptoBlockOffset,
                            message.length - cryptoBlockOffset);
            myDecrypt.reset();
            if (tr.event && Trace.ON) cryptoTime += Native.queryTimer() - startTime;
        }
        myMessagesToPass.removeAllElements();
        int msgLengths = 0;
        if (myIsAggragating) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            ByteArrayInputStream bais = new ByteArrayInputStream(message);
            InputStream inflatorIS = bais;
            if (myIsCompressing) {
                Inflater inflater = new Inflater(myUseSmallZip);
                inflatorIS = new InflaterInputStream(bais, inflater);
            }
            aggregatedmessages: while (true) {
                length = 0;
                int input = readInflator(inflatorIS);
                if (input < 0) break aggregatedmessages;
                int count;
                if (myIsCompressingMsgLengths) {
                    if (0 == (input & 0x80)) {
                        length = input;
                        count = 0;
                    } else if (0x80 == (input & 0xc0)) { // len < 16,384
                        length = input & 0x3f;
                        count = 1;
                    } else if (0xc0 == (input & 0xe0)) { // len < 2,097,152
                        length = input & 0x1f;
                        count = 2;
                    } else if (0xe0 == (input & 0xf0)) { // len < 2**28
                        length = input & 0x0f;
                        count = 3;
                    } else  {                         
                        throw new IOException(
                                "Invalid compressed length code"
                                + HexStringUtils.byteArrayToReadableHexString(message));
                    }
                } else {
                    count = 3;
                }
                for (int i=0; i<count; i++) {
                    length <<=8;
                    input = readInflator(inflatorIS);
                    if (input < 0) break aggregatedmessages;
                    length |= input & 0xff;
                }
                if (length > NetworkSender.MAX_PACKET_LENGTH || length < 0) {
                    throw new IOException("Message too large: " + length +
                                          " > " + NetworkSender.MAX_PACKET_LENGTH);
                }

                if (0 == length) break aggregatedmessages;

                message = new byte[length];
                int offset = 0;
                while (offset < message.length) {
                    int read = inflatorIS.read(message, offset, 
                                               message.length-offset);
                    if (-1 == read) break;
                    offset += read;
                }
                if (offset != length) {
                    throw new IOException(
                                "incoming packet not deflated properly,"
                                +" expectedLength="+length
                                +" deflatedLength="+offset);
                }
                myMessagesToPass.addElement(message);
                msgLengths += message.length;
            }
            if (tr.event && Trace.ON) zipTime += Native.queryTimer() - startTime;
        } else if (message.length != length) {
            byte[] a = new byte[length];
            System.arraycopy(message, 0, a, 0, length);
            myMessagesToPass.addElement(a);
            msgLengths += a.length;
        } else {
            myMessagesToPass.addElement(message);
            msgLengths += message.length;
        }

        if (myIsDoingMac) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            byte digest[] = computeMAC(myMessagesToPass);
            if (!MessageDigest.isEqual(digest, myMAC)) {
                if (tr.error) {
                    traceMessage(myMAC,   
                            0, myMAC.length,   
                            "checksum mismatch, [remote checksum]:");
                    traceMessage(digest,   
                            0, digest.length,   
                            "checksum mismatch, [local checksum]:");
                    Enumeration itr = myMessagesToPass.elements();
                    while (itr.hasMoreElements()) {
                        byte[] msg = (byte[])(itr.nextElement());
                        int len = msg.length;
                        byte[] m = new byte[len+4];
                        m[0] = (byte)((len >> 24) & 0xff);  // The message length
                        m[1] = (byte)((len >> 16) & 0xff);
                        m[2] = (byte)((len >>  8) & 0xff);
                        m[3] = (byte)((len      ) & 0xff);
                        System.arraycopy(msg,0, m,4, len);
                        traceMessage(m, 0, m.length, 
                                "checksum mismatch, [data         ]:");
                    }
                }
                throw new IOException(
                            "incoming packet checksum mismatch");
            }
            if (tr.event && Trace.ON) macTime += Native.queryTimer() - startTime;
        }
        myConnection.updateReceivedCounts(msgLengths, compressedLength);

        Enumeration itr;
        if (tr.event && Trace.ON) {     // Generate event record message
            itr = myMessagesToPass.elements();
            String logMsg = "Processed message length(s)";
            while (itr.hasMoreElements()) {
                byte[] msg = (byte[])(itr.nextElement());
                logMsg += " "+msg.length;
            }
            if (0 != cryptoTime) logMsg+= " CryptoTime="+cryptoTime;
            if (0 != macTime) logMsg+= " MACTime="+macTime;
            if (0 != zipTime) logMsg+= " ZipTime="+zipTime;
            myTotalTime += cryptoTime + macTime + zipTime;
            if (0 != myTotalTime) logMsg += " TotalTime="+myTotalTime;
            tr.eventm(logMsg);
        }

        itr = myMessagesToPass.elements();
        while (itr.hasMoreElements()) {
            byte[] msg = (byte[])(itr.nextElement());
            if (tr.debug && Trace.ON) {
                traceMessage(msg, 0, msg.length,
                             "incoming packet data");
            }
            synchronized (myInnerReceiver.vatLock()) {
                if (Trace.comm.debug && Trace.ON) Trace.comm.debugm("Receiving from " 
                        + myConnection.myRemoteAddr 
                        + "\n" + HexStringUtils.byteArrayToReadableHexString(msg));
                myChangeProtocolIsOk = true;
                ((OutputStream) myInnerReceiver.held()).write(msg);
                myChangeProtocolIsOk = false;
            }
            msgLengths += msg.length;
        }
        myMessagesToPass.removeAllElements(); // Clean up for garbage collecter
    }

    private void fillArray(byte[] b, int off, int len) throws IOException {
        int offset = off;
        while (offset < off + len) {
            int l = myInputStream.read(b, offset, off+len-offset);
            if (l < 0) {
                throw new EOFException();
            }
            offset += l;
        }
    }

    private int readInflator(InputStream inflatorIS) throws IOException {
        try {
            return inflatorIS.read();
        } catch(EOFException e) {
            tr.eventReportException(e, "Reading length");
            return -1;
        }
    }

    private byte[] computeMAC(Vector messages) throws IOException {
        mySHA1.reset();                 //Initialize a new hash
        mySHA1.update(myMACKey);            // The MAC key
        Enumeration itr = messages.elements();
        while (itr.hasMoreElements()) {
            byte[] b = (byte[])(itr.nextElement());
            int len = b.length;
            byte[] l = new byte[4];
            int lenlen = SendThread.msgLength(len, l, 0, myIsCompressingMsgLengths);
            mySHA1.update(l, 0, lenlen);
            mySHA1.update(b);                   // The message
        }
        return mySHA1.digest(myMACKey);     // The MAC key again
    }


    private void traceMessage (byte msg[], int off, int len, String note) {
        String msgString = HexStringUtils.byteArrayToReadableHexString(msg, off, len);
        tr.errorm(note + " (length " + len + ") " + msgString);
    }
}

class Killer implements Runnable 
{
    private RawConnection myConnection;
    
    public Killer(RawConnection conn) {
        myConnection = conn;
    }
    
    public void run() {
        try {
            Thread.currentThread().sleep(600000); // I'm a 10 minute bomb...
        } catch (InterruptedException e) {}
        myConnection.shutdown();
    }
}
