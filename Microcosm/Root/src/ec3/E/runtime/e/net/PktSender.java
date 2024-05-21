package ec.e.net;

import ec.util.assertion.Assertion;
import ec.util.HexStringUtils;
import ec.util.NativeSteward;
import ec.util.NestedIOException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Deflater;
import ec.e.net.steward.NetworkSender;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.Pkt3DESEncrypt;
import ec.e.file.EStdio;

/**
 This class generates network packets in the following forms:
 <p>If no encryption: (4,msgLength) (msgLength, message)
 <p>If encryption and no compression:
    (4,msgLength) (20,SHA1-MAC) (msgLength, message) (pad)
    pad is padding to make the actual packet length a multiple of 8 bytes.
    The whole packet is encrypted with 3DES in CBS mode.
 <p>If there is encryption and compression:
    (n,commLength) (20,SHA1-MAC) (commLength, compressedData) (pad)
    <p> commLength is the length of the data in the message on the comm link
    <p> pad is padding to make the actual packet length a multiple of 8 bytes
    compressedData is the sequence (4,msgLength) (msgLength, message) 
    compressed with ZIP.
    <p>If commLength < 128 (2**7) then n == 1.  (One high bit zero)
       If commLength < 16,384 (2**14) then n == 2.  (Two high bits are 10)
       If commLength < 2,097,152 (2**21) then n == 3.  (Three high bits are 110).
          <p>Since the maximum length packet is currently 1024*1024, the above
          encoding is sufficent.  If longer packets become supported, then
          If commLength < 2**28 then n == 4.  (Four high bits are 1110) and
          If commLength < 2**31 then n == 5.  (Five high bits are 11110). 
          N.B. In Java, array.lenght is a 31 bit int, limiting the maximum 
          length we must support.
    <p>The whole packet is encrypted with 3DES in CBS mode.
 <p>The SHA1-MAC is always calculated over the unencrypted message length as
    a 4 byte field and the unencrypted message.
 */

public class PktSender extends NetworkSender {
    static private final Trace tr = new Trace("ec.e.net.PktSender");

    private NetworkSender myOuterSender;
    private PktConnection myPktConnection;
    private String myRemoteAddr;
    private byte myTmpBuf[];
    private boolean myEncrypting = false;
    // The following fields are only valid if myEncrypting == true
    private MessageDigest mySHA1; 
    private byte[] myMACKey;
    private Pkt3DESEncrypt myPkt3DESEncrypt;
    private boolean myIsCompressing;


    public PktSender(PktConnection conn, NetworkSender outerSender,
                     String remoteAddr) {
        myPktConnection = conn;
        myOuterSender = outerSender;
        myRemoteAddr = remoteAddr;
        myTmpBuf = new byte[1];
    }

    public void close() throws IOException {
        myOuterSender.close();
    }

    public void enable() throws IOException {
        myOuterSender.enable();
    }

    public void flush() throws IOException {
        myOuterSender.flush();
    }

    public String toString() {
        return "PktSender(" + myRemoteAddr + ")" ;
    }

    public void write(int b) throws IOException {
        myTmpBuf[0] = (byte) b;
        write(myTmpBuf, 0, 1);
    }
    
    public void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException {
        myOuterSender.changeProtocol(isAggragating, macType, macKey, 
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip);
        myPktConnection.startEncrypting(macKey, encryptionKey, inIV, isCompressing);

    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (len > PktReceiver.MAX_PACKET_LENGTH) {
            throw new IOException("Packet too large: " + len + " > " +
                                  PktReceiver.MAX_PACKET_LENGTH);
        }
        if (tr.debug && Trace.ON) {
            traceMessage(b, off, len, "sending packet data  ");
        }
            
        long startTime = tr.event ? NativeSteward.queryTimer() : 0;
        byte[] bufB = new byte[len+4];  // Get buffer for output
        bufB[0] = (byte) ((len >> 24) & 0xff) ;
        bufB[1] = (byte) ((len >> 16) & 0xff) ;
        bufB[2] = (byte) ((len >>  8) & 0xff) ;
        bufB[3] = (byte) ((len      ) & 0xff) ;
        if (tr.event && Trace.ON) {
            tr.eventm("Sending packet("+len+") transmitted="
                         +(len+4));
        }
        System.arraycopy(b, off, bufB, 4, len);
        myOuterSender.write(bufB);
    }

    private void traceMessage (byte msg[], int off, int len, String note) {
        String msgString = HexStringUtils.byteArrayToReadableHexString(msg, off, len);
        tr.errorm(note + " (length " + len + ") " + msgString);
    }
}
