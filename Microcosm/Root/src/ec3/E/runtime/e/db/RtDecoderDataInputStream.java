package ec.e.db;

import java.io.DataInputStream;
import java.io.DataInput;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UTFDataFormatException;
import java.util.Hashtable;

public class RtDecoderDataInputStream implements DataInput {
    static private final Trace tr = new Trace("ec.e.db.RtDecoderDataInputStream");

    DataInputStream myInputStream;
    Hashtable myFastStrings;

    public RtDecoderDataInputStream(InputStream is, Hashtable fastStrings) {
        myInputStream = new DataInputStream(is);
        myFastStrings = fastStrings;
    }

    public long readLong() throws IOException {
        long ret = getIntegral();
        return ret;
    }
    
    public int readInt() throws IOException {
        return (int)getIntegral();
    }

    public short readShort() throws IOException {
        return (short)getIntegral();
    }

    public int readUnsignedShort() throws IOException {
        return (int)getIntegral() & 0xffff;
    }

    public String readUTF() throws IOException {
        int strCode = (int)getIntegral();
        if (strCode < 0) {
            Integer codeobj = new Integer(strCode);
            String ret = (String)myFastStrings.get(codeobj);
            return ret;         
        }
        char[] charstr = new char[strCode];
        int bytes = 0;
        int strptr = 0;
        while (bytes < strCode) {
            int c = myInputStream.readUnsignedByte();
            int c2;
            int c3;
            switch (c >> 4) {
             case 0:                //0xxxxxxx
             case 1:
             case 2:
             case 3:
             case 4:
             case 5:
             case 6:
             case 7:
                bytes++;
                charstr[strptr++] = (char)c;
                break;
             case 12:               //110xxxxx 10xxxxxx
             case 13:
                bytes += 2;
                if (bytes > strCode) throw new UTFDataFormatException();
                c2 = myInputStream.readUnsignedByte();
                if ( 0x80 != (c2&0xc0) ) throw new UTFDataFormatException();
                charstr[strptr++] = (char) ( ((c&0x1f) << 6) | (c2&0x3f) );
                break;
             case 14:               //1110xxxx 10xxxxxx 10xxxxxx
                bytes += 3;
                if (bytes > strCode) throw new UTFDataFormatException();
                c2 = myInputStream.readUnsignedByte();
                c3 = myInputStream.readUnsignedByte();
                if ( 0x80 != (c2&0xc0) || 0x80 != (c3&0xc0) ) {
                    throw new UTFDataFormatException();
                }
                charstr[strptr++] 
                        = (char) ( ((c&0xf)<<12) | ((c2&0x3f)<<6) | (c3&0x3f));
                break;
             default:               //10xxxxxx 1111xxxx
                throw new UTFDataFormatException();
            }
        }
        String ret = new String(charstr, 0, strptr);
        return ret;
    }

    /**
     * Read an integer (up to long length) from the input stream
     * <p>The length in the stream is determined by the initial bits of
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
    private long getIntegral() throws IOException {
        long ret = -1;
        long c1 = myInputStream.readUnsignedByte();
        switch ( (int)(c1 & 0xc0) >> 6) {
         case 0: {      // 1 byte
            long val = (c1 << 58) >> 58;
            ret = val;
            break;
         }
         case 1: {      // 2 bytes
            long val = (c1 << 58) >> 58;
            long c2 = myInputStream.readUnsignedByte();
            ret =  (val << 8) | c2;
            break;
         }
         case 2: {      // 3 bytes
            long val = (c1 << 58) >> 58;
            long c2 = myInputStream.readUnsignedByte();
            long c3 = myInputStream.readUnsignedByte();
            ret =  (val << 16) | (c2 << 8) | c3;
            break;
         }
         case 3: {      // Four bits defining length
            long val = (c1 << 60) >> 60;
            switch ( (int)(c1 & 0x30) >> 4) {
             case 0: {      // 4 bytes
                long c2 = myInputStream.readUnsignedByte();
                long c3 = myInputStream.readUnsignedByte();
                long c4 = myInputStream.readUnsignedByte();
                ret =  (val << 24) | (c2 << 16) | (c3 << 8) | c4;
                break;
             }
             case 1: {      // 5 bytes
                long c2 = myInputStream.readUnsignedByte();
                long c3 = myInputStream.readUnsignedByte();
                long c4 = myInputStream.readUnsignedByte();
                long c5 = myInputStream.readUnsignedByte();
                ret =  (val << 32) | (c2 << 24) | (c3 << 16) | (c4 << 8) | c5;
                break;
             }
             case 2: {      // 7 bytes
                long c2 = myInputStream.readUnsignedByte();
                long c3 = myInputStream.readUnsignedByte();
                long c4 = myInputStream.readUnsignedByte();
                long c5 = myInputStream.readUnsignedByte();
                long c6 = myInputStream.readUnsignedByte();
                long c7 = myInputStream.readUnsignedByte();
                ret =  (val << 48) | (c2 << 40) | (c3 << 32) | (c4 << 24) 
                        | (c5 << 16) | (c6 << 8) | c7;
                break;
             }
             case 3: {      // 9 bytes
                long c2 = myInputStream.readUnsignedByte();
                long c3 = myInputStream.readUnsignedByte();
                long c4 = myInputStream.readUnsignedByte();
                long c5 = myInputStream.readUnsignedByte();
                long c6 = myInputStream.readUnsignedByte();
                long c7 = myInputStream.readUnsignedByte();
                long c8 = myInputStream.readUnsignedByte();
                long c9 = myInputStream.readUnsignedByte();
                ret =  (c2 << 56) | (c3 << 48) | (c4 << 40) 
                        | (c5 << 32) | (c6 << 24) | (c7 << 16) | (c8 << 8) | c9;
                break;
             }
            }
            break;
         }
        }
        return ret;
    }

// Pass along methods

    public void readFully(byte[] b) throws IOException {
        myInputStream.readFully(b);
    }
    public void readFully(byte[] b, int off, int len) throws IOException {
        myInputStream.readFully(b, off, len);
    }
    public int skipBytes(int len) throws IOException {
        return myInputStream.skipBytes(len);
    }
    public boolean readBoolean() throws IOException {
        return myInputStream.readBoolean();
    }
    public byte readByte() throws IOException {
        return myInputStream.readByte();
    }
    public int readUnsignedByte() throws IOException {
        return myInputStream.readUnsignedByte();
    }
    public char readChar() throws IOException {
        return myInputStream.readChar();
    }
    public float readFloat() throws IOException {
        return myInputStream.readFloat();
    }
    public double readDouble() throws IOException {
        return myInputStream.readDouble();
    }
    public String readLine() throws IOException {
        return myInputStream.readLine();
    }
    
}

