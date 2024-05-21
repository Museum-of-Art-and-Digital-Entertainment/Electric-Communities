package ec.e.net;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.InflaterInputStream;
import ec.e.net.steward.NetworkConnection;
import ec.e.net.steward.Pkt3DESDecrypt;
import ec.ssl.MD5MessageDigest;
import ec.util.HexStringUtils;
import ec.util.assertion.Assertion;

/**
 This class receives network packets in the following forms:
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
public class PktReceiver extends OutputStream {
    static private final Trace tr = new Trace("ec.e.net.PktReceiver");

    // XXX this should probably be negotiated at connection setup time
    public static final int MAX_PACKET_LENGTH = 1024 * 1024 ;

    private PktConnection myPktConnection;
    private OutputStream myInnerReceiver;
    private String myRemoteAddr;
    private byte myTmpBuf[];
    private byte myPktHeader[];
    private int myLength;
    private byte myIncomingPkt[];
    private int myPosition;
    private int myLengthPosition;
    private boolean myLengthFlag;
    private boolean myEncrypting = false;
    // The following fields are only valid if myEncrypting == true
    private MessageDigest mySHA1;
    private byte[] myMACKey;// The secret key for computing the MAC
    private byte[] myMAC;   // The MAC field from the header for the current message
    private Pkt3DESDecrypt myPkt3DESDecrypt;
    private boolean myIsCompressing;
    // The following fields are only valid if myIsCompressing == true
    private ByteArrayInputStream myBAIS;
    private InflaterInputStream myInflatorIS;
    private int myCryptoBlockOffset = 0;

    public PktReceiver(PktConnection conn, String remoteAddr) {
        myPktConnection = conn;
        myRemoteAddr = remoteAddr;
        myTmpBuf = new byte[1];
        myPktHeader = new byte[24];
        myPosition = -1;
        myLengthPosition = 0 ;
        myLengthFlag = true;
    }

    public void close() throws IOException {
        myInnerReceiver.close();
        myInnerReceiver = null;
    }

    public void flush() throws IOException {
    }

    public void setInnerReceiver(OutputStream inner) {
        myInnerReceiver = inner;
    }

    public String toString() {
        return "PktReceiver(" + myRemoteAddr + ")";
    }
    
    /*package*/ void startEncrypting(byte[] macKey, byte[] desKey,
                                     byte[] inIV, boolean isCompressing) 
    throws IOException {
        myMACKey = macKey;
        myMAC = new byte[20];
        myPkt3DESDecrypt = new Pkt3DESDecrypt(desKey, inIV);
        try {
            mySHA1 = MessageDigest.getInstance("SHA");
        } catch(NoSuchAlgorithmException e) {
            throw new IOException("Unable to build SHA" + e);
        }
        myEncrypting = true;
        myIsCompressing = isCompressing;
        if (tr.verbose) tr.verbosem("isCompressing="+isCompressing);
    }

    public void write(int b) throws IOException {
        myTmpBuf[0] = (byte) b;
        write(myTmpBuf, 0, 1);
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (tr.verbose) tr.verbosem("Got " + len + " bytes: " 
                + HexStringUtils.byteArrayToReadableHexString(b, off, len));
 
        while (len > 0) {
            if (myLengthFlag) {
                int lenToGo = myEncrypting ?
                                myPktHeader.length - myLengthPosition :
                                4 - myLengthPosition;
                if (tr.verbose) tr.verbosem("myLengthPosition=" 
                            + myLengthPosition + " lenToGo=" + lenToGo);
                if (len >= lenToGo) {
                    System.arraycopy(b, off, myPktHeader, 
                                     myLengthPosition, lenToGo);
                    off += lenToGo;
                    len -= lenToGo;
                    myLengthFlag = false;
                    if (myEncrypting) {
                        myPkt3DESDecrypt.decrypt(myPktHeader);
                    }
                    if (tr.verbose) {
                        tr.verbosem("Decrypted header: "
                            + HexStringUtils.byteArrayToReadableHexString(myPktHeader));
                    }
                    myCryptoBlockOffset = 0; // Message data decrypted with header
                    if (myIsCompressing) {
                        int l1 = myPktHeader[0];    // First byte of the length
                        if (0 == (l1 & 0x80)) {           // len < 128
                            myLength = l1;
                            myCryptoBlockOffset = 3;
                        } else if (0x80 == (l1 & 0xc0)) { // len < 16,384
                            myLength = ((l1 & 0x3f) << 8) |
                                       ((myPktHeader[1] & 0xff));
                            myCryptoBlockOffset = 2;
                        } else if (0xc0 == (l1 & 0xe0)) { // len < 2,097,152
                            myLength = ((l1 & 0x1f)             << 16) |
                                       ((myPktHeader[1] & 0xff) <<  8) |
                                       ((myPktHeader[2] & 0xff))       ;
                            myCryptoBlockOffset = 1;
                        } else if (0xe0 == (l1 & 0xf0)) { // len < 2**28
                            myLength = ((l1 & 0x0f)             << 24) |
                                       ((myPktHeader[1] & 0xff) << 16) |
                                       ((myPktHeader[2] & 0xff) <<  8) |
                                       ((myPktHeader[3] & 0xff))       ;
                            // myCryptoBlockOffset = 0; already set
                        } else  {                         
                            throw new IOException(
                                    "Invalid compressed length code="
                                    + Integer.toString(l1,16));
                        }
                    } else {            // Not compressing
                        myLength = ((myPktHeader[0] & 0xff) << 24) |
                                   ((myPktHeader[1] & 0xff) << 16) |
                                   ((myPktHeader[2] & 0xff) <<  8) |
                                    (myPktHeader[3] & 0xff)        ;
                    }
                    if (tr.verbose) {
                        tr.verbosem("incoming packet len = " + myLength);
                    }
                    if (myLength > MAX_PACKET_LENGTH || myLength < 0) {
                        throw new IOException("Packet too large: " + myLength +
                                              " > " + MAX_PACKET_LENGTH);
                    }
                    myIncomingPkt = new byte[
                                    myEncrypting ? ((myLength-myCryptoBlockOffset+7)
                                                    &0xfffffff8)
                                                    + myCryptoBlockOffset
                                                 : myLength];
                    if (myCryptoBlockOffset > 0) { 
                        // Copy compressed data decrypted with header
                        System.arraycopy(myPktHeader,24-myCryptoBlockOffset, 
                                         myIncomingPkt,0, myCryptoBlockOffset);
                    }
                    myPosition = myCryptoBlockOffset;
                    if (myEncrypting) {
                        System.arraycopy(myPktHeader,4-myCryptoBlockOffset, 
                                     myMAC, 0, 20); // Save MAC
                    }
                } else {
                    System.arraycopy(b, off, myPktHeader, myLengthPosition,
                                     len);
                    myLengthPosition += len ;
                    return;
                }
            }
            int pktToGo = myIncomingPkt.length - myPosition ;
            if (len >= pktToGo) {
                System.arraycopy(b, off, myIncomingPkt, myPosition, pktToGo);
                off += pktToGo;
                len -= pktToGo;
                ////////////////////////////////////////////////
                if (myEncrypting) {
                    myPkt3DESDecrypt.decrypt(myIncomingPkt, myCryptoBlockOffset,
                                    myIncomingPkt.length - myCryptoBlockOffset);
                    myPkt3DESDecrypt.reset();
                    if (myIsCompressing) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(myIncomingPkt);
                        InflaterInputStream inflatorIS = new InflaterInputStream(bais);
                        myLength = 0;
                        for (int i=0; i<4; i++) {
                            myLength <<=8;
                            int input = inflatorIS.read();
                            Assertion.test(input != -1, "EOF received reading length");
                            myLength |= input & 0xff;
                        }
                        myIncomingPkt = new byte[myLength];
                        int offset = 0;
                        while (offset < myIncomingPkt.length) {
                            int read = inflatorIS.read(myIncomingPkt, offset, 
                                                       myIncomingPkt.length-offset);
                            if (-1 == read) break;
                            offset += read;
                        }
                        if (offset != myLength) {
                            throw new IOException(
                                        "incoming packet not deflated properly,"
                                        +" expectedLength="+myLength
                                        +" deflatedLength="+offset);
                        }
                    } else if (myIncomingPkt.length != myLength) {
                        byte[] a = new byte[myLength];
                        System.arraycopy(myIncomingPkt, 0, a, 0, myLength);
                        myIncomingPkt = a;
                    }
                    byte digest[] = computeMAC(myIncomingPkt);
                    if (!MessageDigest.isEqual(digest, myMAC)) {
                        if (tr.error) {
                            traceMessage(myMAC,   
                                    0, myMAC.length,   
                                    "checksum mismatch, [remote checksum]:");
                            traceMessage(digest,   
                                    0, digest.length,   
                                    "checksum mismatch, [local checksum]:");
                            traceMessage(myIncomingPkt, 
                                    0, myIncomingPkt.length, 
                                    "checksum mismatch, [data         ]:");
                        }
                        throw new IOException(
                                    "incoming packet checksum mismatch");
                    }
                }
                ////////////////////////////////////////////////
                myLengthPosition = 0 ;
                myLengthFlag = true;
                if (tr.debug) {
                    traceMessage(myIncomingPkt, 0, myIncomingPkt.length,
                                 "incoming packet data");
                } else if (tr.event) {
                    tr.eventm("Received packet length="+myIncomingPkt.length);
                }
                myInnerReceiver.write(myIncomingPkt);
            } else {
                System.arraycopy(b, off, myIncomingPkt, myPosition, len);
                myPosition += len ;
                return;
            }
        }

    }

    private byte[] computeMAC(byte[] b) {
        int len = b.length;
        mySHA1.reset();                 //Initialize a new hash
        mySHA1.update(myMACKey);            // The MAC key
        byte[] l = new byte[4];
        l[0] = (byte)((len >> 24) & 0xff);  // The message length
        l[1] = (byte)((len >> 16) & 0xff);
        l[2] = (byte)((len >>  8) & 0xff);
        l[3] = (byte)((len      ) & 0xff);
        mySHA1.update(l);
        mySHA1.update(b);                   // The message
        return mySHA1.digest(myMACKey);     // The MAC key again
    }

    private void traceMessage (byte msg[], int off, int len, String note) {
        String msgString = HexStringUtils.byteArrayToReadableHexString(msg, off, len);
        tr.errorm(note + " (length " + len + ") " + msgString);
    }
}
