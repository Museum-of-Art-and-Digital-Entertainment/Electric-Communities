// CompactInputStream.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** @private
  */
public class CompactInputStream extends InputStream {
    static final int INVALID_BUFFER_COUNT = 8;

    InputStream in;
    int booleanCount = INVALID_BUFFER_COUNT;
    int booleanBuffer;
    Vector stringVector = new Vector(128);

    public CompactInputStream(InputStream in) {
        super();
        this.in = in;
    }

    public int read() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        return in.read();
    }

    public int read(byte value[]) throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        return in.read(value, 0, value.length);
    }

    public int read(byte value[], int offset, int length) throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        return in.read(value, offset, length);
    }

    public long skip(long n) throws IOException {
        if (n > 0) {
            booleanCount = INVALID_BUFFER_COUNT;
            return in.skip(n);
        }

        return n;
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        in.close();
    }

    public void mark(int readlimit) {
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }

    public void readFully(byte value[]) throws IOException {
        readFully(value, 0, value.length);
    }

    public void readFully(byte value[], int offset, int length)
        throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        int total, count;
        InputStream in = this.in;

        total = 0;

        while (total < length) {
            count = in.read(value, offset + total, length - total);
            if (count < 0)
                throw new EOFException();

            total += count;
        }
    }

    public int skipBytes(int total) throws IOException {
        int orig;
        InputStream in = this.in;

        orig = total;

        while (total > 0) {
            total -= skip(total);
        }

        return orig;
    }

    public boolean readCompactBoolean() throws IOException {
        boolean value;
        int buf = booleanBuffer;
        int count = booleanCount;

        if (count >= 8) {
            buf = in.read();
            if (buf < 0)
                throw new EOFException();

            count = 0;
        }

        value = ((buf & (1 << count)) != 0);
        count++;

        booleanBuffer = buf;
        booleanCount = count;

        return value;
    }

    public boolean readBoolean() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        int c = in.read();

        if (c < 0)
            throw new EOFException();

        return (c != 0);
    }

    public byte readByte() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        int c = in.read();

        if (c < 0)
            throw new EOFException();

        return (byte)c;
    }

    public int readUnsignedByte() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        int c = in.read();

        if (c < 0)
            throw new EOFException();

        return c;
    }

    public short readShort() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        InputStream in = this.in;
        int c1 = in.read();
        int c2 = in.read();

        if ((c1 | c2) < 0)
             throw new EOFException();

        return (short)((c1 << 8) + (c2 << 0));
    }

    public int readUnsignedShort() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        InputStream in = this.in;
        int c1 = in.read();
        int c2 = in.read();

        if ((c1 | c2) < 0)
             throw new EOFException();

        return (c1 << 8) + (c2 << 0);
    }

    public char readChar() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        InputStream in = this.in;
        int c1 = in.read();
        int c2 = in.read();

        if ((c1 | c2) < 0)
             throw new EOFException();

        return (char)((c1 << 8) + (c2 << 0));
    }

    /** This reads in specially packed ints.
      */
    public int readCompactInt() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        InputStream in = this.in;
        int c, value, shift;
        boolean neg;

        c = in.read();
        if (c < 0)
            throw new EOFException();
        else if (c == 0x040) {
            return Integer.MIN_VALUE;
        }

        neg = (c & 0x40) != 0;
        value = c & 0x3f;

        if ((c & 0x80) != 0) {
            c = in.read();
            if (c < 0)
                throw new EOFException();

            value = value | ((c & 0x7f) << 6);
            if ((c & 0x80) != 0) {
                c = in.read();
                if (c < 0)
                    throw new EOFException();

                value = value | ((c & 0x7f) << 13);
                if ((c & 0x80) != 0) {
                    c = in.read();
                    if (c < 0)
                        throw new EOFException();

                    value = value | ((c & 0x7f) << 20);
                    if ((c & 0x80) != 0) {
                        c = in.read();
                        if (c < 0)
                            throw new EOFException();

                        value = value | ((c & 0x7f) << 27);
                    }
                }
            }
        }

        if (neg)
            value = -value;

        return value;
    }

    public int readInt() throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        InputStream in = this.in;
        int c1 = in.read();
        int c2 = in.read();
        int c3 = in.read();
        int c4 = in.read();

        if ((c1 | c2 | c3 | c4) < 0)
             throw new EOFException();

        return ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0));
    }

    public long readLong() throws IOException {
        return (readInt() << 32L) + (readInt() & 0xffffffffL);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public String readCompactUTF() throws IOException {
        int id;
        String str;

        id = readCompactInt();

        // There are two special ids for null and "".

        if (id == 0) {
            return null;
        } else if (id == 1) {
            return "";
        }

        // If the id is less than zero then this is a new string and the
        // absolute value is the length of the string.

        if (id < 0) {
            str = readUTFBytes(-id);
            stringVector.addElement(str);
            return str;
        }

        // We need to subtract 2 because of the special ids.

        return (String)stringVector.elementAt(id - 2);
    }

    public String readUTF() throws IOException {
        int utflen;

        utflen = readUnsignedShort();
        if (utflen == 0xffff)
            return null;

        return readUTFBytes(utflen);
    }

    private final String readUTFBytes(int utflen) throws IOException {
        booleanCount = INVALID_BUFFER_COUNT;
        char str[] = new char[utflen];
        int count = 0;
        int strlen = 0;

        while (count < utflen) {
            int c = readUnsignedByte();
            int char2, char3;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    // 0xxxxxxx
                    count++;
                    str[strlen++] = (char)c;
                    break;
                case 12: case 13:
                    // 110x xxxx   10xx xxxx
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException();
                    str[strlen++] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException();
                    char2 = readUnsignedByte();
                    char3 = readUnsignedByte();
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException();
                    str[strlen++] = (char)(((c & 0x0F) << 12) |
                                           ((char2 & 0x3F) << 6) |
                                           ((char3 & 0x3F) << 0));
                default:
                    // 10xx xxxx,  1111 xxxx
                    throw new UTFDataFormatException();
                }
        }

        return new String(str, 0, strlen);
    }
}
