package ec.e.db;

import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;

// For collecting information on encoded strings
import java.util.Hashtable;

public class RtEncoderDataOutputStream implements DataOutput {
    static private final Trace tr = new Trace("ec.e.db.RtEncoderDataOutputStream");

    private Hashtable myFastStrings;

    DataOutputStream myOutputStream;

    public RtEncoderDataOutputStream(OutputStream is, Hashtable fastStrings) {
        myOutputStream = new DataOutputStream(is);
        myFastStrings = fastStrings;
    }

    public void writeLong(long v) throws IOException {
        putIntegral(v);
    }
    
    public void writeInt(int v) throws IOException {
        putIntegral(v);
    }

    public void writeShort(int v) throws IOException {
        putIntegral(v);
    }

    public void writeUTF(String s) throws IOException {
        Integer strID = (Integer)myFastStrings.get(s);
        if (null != strID) {
            putIntegral(strID.intValue());
        } else {
            int inlen = s.length();
            int outlen = 0;
            for (int i=0; i<inlen; i++) {   // Calc length of output in bytes
                int c = s.charAt(i);
                if (c > 0 && c < 0x80) outlen++;
                else if (c < 0x800) outlen += 2;
                else outlen += 3;
            }
            putIntegral(outlen);            // Write the byte length of the UTF
            for (int i=0; i<inlen; i++) {   // Write the string as UTF bytes
                int c = s.charAt(i);
                if (c > 0 && c < 0x80) myOutputStream.writeByte(c);
                else if (c < 0x800) {
                    myOutputStream.writeByte( 0xc0 | ((c>>6)&0x1f) );
                    myOutputStream.writeByte( 0x80 | (c&0x3f) );
                } else {
                    myOutputStream.writeByte( 0xe0 | ((c>>12)&0x0f) );
                    myOutputStream.writeByte( 0x80 | ((c>>6)&0x3f) );
                    myOutputStream.writeByte( 0x80 | (c&0x3f) );
                }
            }
        }
    }

    /**
     * Write an integer (up to long length) to the output stream.
     * <p>The length in the stream is encoded in the initial bits of
     * the first byte:
     * <p> 00 - One byte -32 .. 31
     * <p> 01 - Two bytes -2**13 .. 2**13-1
     * <p> 10 - Three bytes -2**21 .. 2**21-1
     * <p> 11 - Next 2 bits determine the length as follows:
     * <p> 1100 - Four bytes -2**27 .. 2**27-1
     * <p> 1101 - Five bytes -2**35 .. 2**35-1
     * <p> 1110 - Seven bytes -2**51 .. 2**51-1
     * <p> 1111 - Nine bytes -9223372036854775808 .. 9223372036854775808
     */
    private void putIntegral(long v) throws IOException {
        long pv = (v<0 ? v ^ 0xffffffffffffffffL : v);  // make initial bits zero
        if (pv <= 0x1f) {                   // 1 byte to stream
            myOutputStream.writeByte( (int)(v & 0x3f) );
        } else if (pv <= 0x1fff) {          // 2 bytes to stream
            myOutputStream.writeByte( 0x40 | (int)( (v>>8) &0x3f) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        } else if (pv <= 0x1fffff) {        // 3 bytes to stream
            myOutputStream.writeByte( 0x80 | (int)( (v>>16) &0x3f) );
            myOutputStream.writeByte( (int)( (v>>8) & 0xff) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        } else if (pv <= 0x07ffffff) {      // 4 bytes to stream
            myOutputStream.writeByte( 0xc0 | (int)( (v>>24) &0x0f) );
            myOutputStream.writeByte( (int)( (v>>16) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>8) & 0xff) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        } else if (pv <= 0x07ffffffffL) {       // 5 bytes to stream
            myOutputStream.writeByte( 0xd0 | (int)( (v>>32) &0x0f) );
            myOutputStream.writeByte( (int)( (v>>24) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>16) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>8) & 0xff) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        } else if (pv <= 0x07ffffffffffffL) {   // 7 bytes to stream
            myOutputStream.writeByte( 0xe0 | (int)( (v>>48) &0x0f) );
            myOutputStream.writeByte( (int)( (v>>40) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>32) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>24) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>16) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>8) & 0xff) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        } else {                                // 9 bytes to stream
            myOutputStream.writeByte( 0xf0  );
            myOutputStream.writeByte( (int)( (v>>56) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>48) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>40) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>32) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>24) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>16) & 0xff) );
            myOutputStream.writeByte( (int)( (v>>8) & 0xff) );
            myOutputStream.writeByte( (int)(v & 0xff) );
        }
    }

// Pass along methods

    public void write(int b) throws IOException {
        myOutputStream.write(b);
    }
    public void write(byte[] b) throws IOException {
        myOutputStream.write(b);
    }
    public void write(byte[] b, int off, int len) throws IOException {
        myOutputStream.write(b, off, len);
    }
    public void writeBoolean(boolean v) throws IOException {
        myOutputStream.writeBoolean(v);
    }
    public void writeByte(int v) throws IOException {
        myOutputStream.writeByte(v);
    }
    public void writeChar(int v) throws IOException {
        myOutputStream.writeChar(v);
    }
    public void writeFloat(float v) throws IOException {
        myOutputStream.writeFloat(v);
    }
    public void writeDouble(double v) throws IOException {
        myOutputStream.writeDouble(v);
    }
    public void writeBytes(String s) throws IOException {
        myOutputStream.writeBytes(s);
    }
    public void writeChars(String s) throws IOException {
        myOutputStream.writeChars(s);
    }
    
}

