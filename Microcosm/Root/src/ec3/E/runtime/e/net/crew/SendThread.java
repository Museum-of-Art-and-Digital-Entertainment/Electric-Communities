package ec.e.net.crew;

import ec.util.assertion.Assertion;
import ec.util.HexStringUtils;
import ec.util.Native;
import ec.util.NestedIOException;
import java.io.OutputStream;
import ec.e.net.steward.NetworkSender;
import ec.e.util.crew.Queue;
import ec.e.util.crew.QueueReader;
import ec.e.util.crew.QueueWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Deflater;

/**
   A thread for writing data to an OutputStream that might block.
   Incoming chunks of data are enqueued.  The thread dequeues chunks
   and writes them.  Exceptions during the actual writing are sent to
   the RawConnection for delivery back into the vat via
   noticeProblem().
<p>
 This class generates network packets in the following forms:

 <p>If no encryption, no mac, no compression: (dataLength, data)

 <p>If no encryption, no mac, compression: 
            (n,commlength) (commLength,compressedData)

    <p> commLength is the length of the data in the message on the comm link
    compressedData is the data compressed with ZIP.

    <p>If commLength < 128 (2**7) then n == 1.  (One high bit zero)
       If commLength < 16,384 (2**14) then n == 2.  (Two high bits are 10)
       If commLength < 2,097,152 (2**21) then n == 3.  (Three high bits are 110).
          <p>Since the maximum length packet is currently 1024*1024, the above
          encoding is sufficent.  If longer packets become supported, then
          If commLength < 2**28 then n == 4.  (Four high bits are 1110) and
          If commLength < 2**31 then n == 5.  (Five high bits are 11110). 
          N.B. In Java, array.length is a 31 bit int, limiting the maximum 
          length we must support.

 <p>encryption, mac, no compression is no longer supported.

 <p>If no encryption, mac, compression:
            (n,commLength) (20,MAC) (commLength,compressedData)

 <p>The MAC is always calculated over the uncompressed data.  N.B. the
    PktSender class prepends a 4 byte message length field to each message,
    so the MAC includes the message length(s).

 <p>If encryption, mac, compression:
    (n,commLength) (20,SHA1-MAC) (commLength, compressedData) (pad)

    <p> pad is padding to make the actual packet length a multiple of the
    encryption block size.

    <p>The whole packet is encrypted with the encryption algorthm and mode
    specified when encryption is turned on.
 */
public class SendThread extends NetworkSender implements Runnable {
    static Trace tr = new Trace("ec.e.net.SendThread");

    OutputStream myOutputStream;
    RawConnection myConnection;
    QueueReader myReader;
    QueueWriter myWriter;

    // The following fields are for aggragating messages
    private static final int MAX_AGGRAGATION = 1024;
    private boolean myIsAggragating = false;
    private int myAggragateLength;
    private byte[] myAggragation = new byte[MAX_AGGRAGATION];
    private int myAggragationCount = 0; // For event messages

    // The following fields are for encryption
    private boolean myIsDoingCrypto = false;
    private Encrypt3DES myEncrypt = null;

    // The following fields are for MAC calculation
    private boolean myIsDoingMac = false;
    private byte[] myMacKey; 
    private MessageDigest mySHA1;
    private int myMacLen = 0;

    // The following fields are for compression
    private boolean myIsCompressingMsgLengths = false;
    private boolean myIsCompressing = false;
    private boolean myUseSmallZip;

    // Spam only field for total comp/MAC/encrypt time
    // only used or updated if we are event tracing
    private long myTotalTime = 0;

    /**
       Make a new SendThread.

       @param out where to write bytes.
       @param connection where to send things we can't deal with.
      */
    public SendThread(OutputStream out, RawConnection connection) {
        myOutputStream = out;
        myConnection = connection;
        Queue q = new Queue(new Object());
        myReader = q.reader();
        myWriter = q.writer();
    }

    /**
       Body of the thread.  Responsible for dequeueing chunks and
       writing them.  Calls RawConnection.shutdown() before exiting.
       Will exit if a null is dequeued.
      */
    public void run() {
        QueueObject chunk;
        try {
            if (tr.debug && Trace.ON)
                tr.debugm("running...");
            while ((chunk = (QueueObject) myReader.nextElement()) != null) {
                addElement(chunk);
                while (myReader.hasMoreElements()) {
                    chunk = (QueueObject)myReader.nextElement();
                    if (null == chunk) break;
                    addElement(chunk);
                }
                flushElements();
            }
            if (tr.debug && Trace.ON)
                tr.debugm("I've been asked to shutdown");
        } catch (Exception e) {
            if (tr.debug && Trace.ON)
                tr.debugm("caught exception: " + e);
            myConnection.noticeProblem(e);
        }
        if (tr.debug && Trace.ON)
            tr.debugm("terminated");
        myConnection.shutdown();
        myConnection = null;
    }

    public void write(int b) throws IOException {
        throw new IOException("Illegal routine called");
    }

    public void write(byte b[]) throws IOException {
        if (Trace.comm.debug && Trace.ON) Trace.comm.debugm("enqueueing to " 
                + myConnection.myRemoteAddr 
                + "\n" + HexStringUtils.byteArrayToReadableHexString(b));
        myWriter.enqueue(new Chunk(b));
    }

    /**
       Enqueue a chunk to be written later.  Other write calls are
       similar.

       @param b the bytes to be written.
       @param off offset to start writing at.
       @param len how many bytes to write.
      */
    public void write(byte b[], int off, int len) throws IOException {
        throw new IOException("Illegal routine called");
    }

    public void flush() throws IOException { 
        // XXX Check for need to do something on flush
    }

    /**
       Enqueues a null.  run() will shutdown and exit after draining
       the queue to this point.
      */
    public void close() throws IOException {
        myWriter.enqueue(null);
    }

    /**
     * Get the current IV for decryption
     */
    /*package*/ byte[] /*NilOK*/ getIV() {
        if (null != myEncrypt) return myEncrypt.getIV();
        return null;
    }

    /**
       Called when the protocol stack out from us is ready to receive
       bytes.  We pass this to RawConnection, which starts RecvThread,
       allowing bytes to flow.
      */
    public void enable() {
        myConnection.enable();
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
        if (Trace.comm.debug && Trace.ON) tr.debugm(
                                    "enqueueing CryptoChange, agg="+isAggragating 
                                    +" MACt="+macType+" ENCt="+encryptionType
                                    +" Comp="+isCompressing+" Small="+useSmallZip);

        myWriter.enqueue(new CryptoChange(isAggragating, macType, macKey,
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip));
        //We must synchronously inform ReceiveThread of the change in protocol
        myConnection.changeProtocol(isAggragating, macType, macKey,
                                    encryptionType, encryptionKey, outIV, inIV,
                                    isCompressing, useSmallZip);
    }

    private void addElement(QueueObject elem) throws IOException {
        if (elem instanceof Chunk) {
            Chunk c = (Chunk) elem;
            if (tr.verbose && Trace.ON) tr.verbosem("addElement="+c.myBuffer.length);
            if (!myIsAggragating || myAggragateLength + c.myBuffer.length+4 > myAggragation.length) {
                flushElements();
                if (c.myBuffer.length+4 > myAggragation.length) {
                    sendBytesWithLength(c.myBuffer);
                    return;
                }
            }
            int len = c.myBuffer.length;
            myAggragateLength = msgLength(len, myAggragation, 
                                    myAggragateLength, myIsCompressingMsgLengths);
            System.arraycopy(c.myBuffer, 0, myAggragation,
                             myAggragateLength, len);
            myAggragateLength += len;
            myAggragationCount++;
        } else {
            flushElements();    // Write previous stuff under old rules
            CryptoChange c = (CryptoChange) elem;
            if (tr.debug && Trace.ON) tr.debugm("doing CryptoChange, agg="+c.myIsAggragating 
                                +" MACt="+c.myMacType+" ENCt="+c.myEncryptionType
                                +" Comp="+c.myIsCompressing+" Small="+c.myUseSmallZip);

            myIsAggragating = c.myIsAggragating;    // Save aggragation switch

            if ("None".equals(c.myMacType)) {
                myIsDoingMac = false;
            } else if ("SHA1".equals(c.myMacType)) {
                myIsDoingMac = true;
                myMacKey = c.myMacKey;
                myMacLen = 20;
                try {
                    mySHA1 = MessageDigest.getInstance("SHA");
                } catch(NoSuchAlgorithmException e) {
                    tr.errorm("Unable to build SHA", e);
                    throw new NestedIOException("Unable to build SHA", e);
                }
            }  else {
                throw new IOException("Invalid MAC type "+c.myMacType);
            }
 
            if ("None".equals(c.myEncryptionType)) {
                myIsDoingCrypto = false;
            } else if ("3DES".equals(c.myEncryptionType)) {
                myIsDoingCrypto = true;
                myEncrypt = new Encrypt3DES(c.myEncryptionKey, c.myOutIV);
            } else {
                throw new IOException("Invalid encryption type "+c.myEncryptionType);
            }
            if (c.myIsCompressing) {
                myIsCompressingMsgLengths = false;
                myIsCompressing = true;
                myUseSmallZip = c.myUseSmallZip;
            } else {
                myIsCompressing = false;
                myIsCompressingMsgLengths = myIsAggragating;
            }
        }
    }

    private void flushElements() throws IOException {
        if (tr.verbose && Trace.ON) tr.verbosem("flushElements="+myAggragateLength);
        if (0 != myAggragateLength) {
            sendBytes(myAggragation, 0, myAggragateLength, null,
                    myAggragateLength-(4*myAggragationCount));
            myAggragateLength = 0;
            myAggragationCount = 0;
        }
        myOutputStream.flush();
    }

    private void sendBytesWithLength(byte[] b) throws IOException {

        if (tr.verbose && Trace.ON) tr.verbosem("sendBytesWithLength="+b.length);
        byte[] len = new byte[4];
        int lenlen = msgLength(b.length, len, 
                                0, myIsCompressingMsgLengths);
        byte[] clen = new byte[lenlen];
        System.arraycopy(len, 0, clen, 0, lenlen);
        
        sendBytes(b, 0, b.length, clen, b.length);
    }

    private void sendBytes(byte[] b, int off, int len,
                           /*NilOK*/byte[] lenField, int rawLength) throws IOException {

        if (tr.verbose && Trace.ON) tr.verbosem("sendBytes="+len);
        if (!myIsCompressing && !myIsCompressingMsgLengths) {
            int lineLen = 0;
            // If we're not compressing, we're not doing mac or encryption either
            if (null != lenField) {
                myOutputStream.write(lenField);
                lineLen += lenField.length;
            }
            myConnection.updateSendCounts(rawLength, lineLen+len);
            if (tr.event && Trace.ON) tr.eventm("Sending message len="+len);
            myOutputStream.write(b, off, len);
            return;
        }
        long cryptoTime = 0;
        long macTime = 0;
        long zipTime = 0;
        long startTime = 0;

        if (tr.event && Trace.ON) startTime = Native.queryTimer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream deflaterOS = baos;
        if (myIsCompressing) {
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, myUseSmallZip);
            deflaterOS = new DeflaterOutputStream(baos, deflater);
        }
        baos.write(new byte[myMacLen+4]); // Write space for length + MAC
        if (null != lenField) {
            deflaterOS.write(lenField);
        }
        deflaterOS.write(b, off, len);
        deflaterOS.close();
        // Get the length and the length of the sent length field
        int commLength = baos.size() - 24;

        int offset = -1;
        if (commLength < 128) { 
            offset = 3;
        } else if (commLength < 16384) {
            offset = 2;
        } else if (commLength < 2097152) {
            offset = 1;
        } else {
            throw new IOException("Packet too large: " + commLength 
                    + " >= 2,097,152");
        }
        // If not a multiple of 64 bits (8 bytes), pad for encryption
        if (myIsDoingCrypto && 0 != ((commLength-offset) & 7)) {   
            byte[] fill = new byte[8 - ((commLength-offset)&7)];
            baos.write(fill);     // Pad to multiple of 8 bytes
        }
        byte[] data = baos.toByteArray();
        if (tr.event && Trace.ON) zipTime += Native.queryTimer() - startTime;

        if (myIsDoingMac) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            byte[] mac = computeMAC(b, off, len, lenField);
            if (tr.verbose && Trace.ON) tr.verbosem("MAC is " 
                    + HexStringUtils.byteArrayToReadableHexString(mac));
            System.arraycopy(mac, 0, data, 4, myMacLen);
            Assertion.test(mac.length == myMacLen, "MAC length error");
            if (tr.event && Trace.ON) macTime += Native.queryTimer() - startTime;
        }

        switch (offset) {
         case 1:
            data[1] = (byte)(((commLength >> 16) & 0x1f) | 0xc0);
            data[2] = (byte) ((commLength >>  8) & 0xff);
            data[3] = (byte) (commLength         & 0xff);
            break;
         case 2:
            data[2] = (byte)(((commLength >> 8) & 0x3f) | 0x80);
            data[3] = (byte) (commLength & 0xff);
            break;
         case 3:
            data[3] = (byte) (commLength & 0x7f);
            break;
         default: Assertion.fail("Case out of range, offset="+offset);
        }

        if (myIsDoingCrypto) {
            if (tr.event && Trace.ON) startTime = Native.queryTimer();
            if (tr.verbose && Trace.ON) tr.verbosem("encrypting " + (data.length-offset) + " bytes: " 
                    + HexStringUtils.byteArrayToReadableHexString(data, offset, data.length-offset));
            myEncrypt.encrypt(data, offset, data.length-offset);
            if (tr.event && Trace.ON) cryptoTime += Native.queryTimer() - startTime;
        }

        myConnection.updateSendCounts(rawLength, data.length-offset);

        if (tr.event && Trace.ON) {         // Generate event message
            int slen = data.length - offset;
            String msg = 0 != myAggragationCount ?
                    "Sending aggragation of " + myAggragationCount :
                    "Sending";
            msg += " len="+len+" Compressed="+slen
                    +" "+((slen*100)/len)+"%";
            if (0 != cryptoTime) msg+= " CryptoTime="+cryptoTime;
            if (0 != macTime) msg+= " MACTime="+macTime;
            if (0 != zipTime) msg+= " ZipTime="+zipTime;
            myTotalTime += cryptoTime + macTime + zipTime;
            if (0 != myTotalTime) msg += " TotalTime="+myTotalTime;
            tr.eventm(msg);
        }

        myOutputStream.write(data, offset, data.length-offset);
        return;
    }

    private byte[] computeMAC(byte[] b, int off, int len,
                              /*NilOK*/byte[] lenField) {
        if (tr.verbose && Trace.ON) tr.verbosem("Calculating MAC on (length "+len+"):" 
                + HexStringUtils.byteArrayToReadableHexString(b, off, len));
        mySHA1.reset();                 //Initialize a new hash
        mySHA1.update(myMacKey);            // The MAC key
        if (null != lenField) {
            mySHA1.update(lenField);
        }
        mySHA1.update(b, off, len);
        return mySHA1.digest(myMacKey);     // The MAC key again
    }

    static /*package*/ int msgLength(int len, byte[] buf, int off, boolean compressed) 
            throws IOException {
        if (compressed) {
            if (len < 128) { 
                buf[off++] = (byte) (len & 0x7f);
            } else if (len < 16384) {
                buf[off++] = (byte)(((len >> 8) & 0x3f) | 0x80);
                buf[off++] = (byte) (len & 0xff);
            } else if (len < 2097152) {
                buf[off++] = (byte)(((len >> 16) & 0x1f) | 0xc0);
                buf[off++] = (byte) ((len >>  8) & 0xff);
                buf[off++] = (byte) (len         & 0xff);
            } else {
                throw new IOException("Packet too large: " + len 
                        + " >= 2,097,152");
            }
        } else {
            buf[off++] = (byte)( (len>>24) & 0xff);
            buf[off++] = (byte)( (len>>16) & 0xff);
            buf[off++] = (byte)( (len>> 8) & 0xff);
            buf[off++] = (byte)( (len    ) & 0xff);
        }
        return off;
    }
}

/** Marker class for things to queue in the output queue
 */
class QueueObject {
}

/**
 * Structure to hold a change in protocol request in the queue
 */
class CryptoChange extends QueueObject {
    boolean myIsAggragating;
    String myMacType;
    byte[] myMacKey; 
    String myEncryptionType;
    byte[] myEncryptionKey;
    byte[] myOutIV; 
    byte[] myInIV;
    boolean myIsCompressing;
    boolean myUseSmallZip;
     
    CryptoChange(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) {
        myIsAggragating = isAggragating;
        myMacType = macType;
        myMacKey = macKey;
        myEncryptionType = encryptionType;
        myEncryptionKey = encryptionKey;
        myOutIV = outIV;
        myInIV = inIV;
        myIsCompressing = isCompressing;
        myUseSmallZip = useSmallZip;
    }
}

/**
   Structure to hold args to a write() call, so it can be enqueued.
  */
class Chunk extends QueueObject {
    /*package*/ byte myBuffer[];
    
    public Chunk(byte b[]) {
        myBuffer = b;
    }
}

