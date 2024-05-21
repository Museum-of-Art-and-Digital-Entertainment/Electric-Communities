// CompactOutputStream.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

import java.io.*;

/** Don't flush() in the middle of writing lots of booleans.  ALERT!
  * @private
  */
public class CompactOutputStream extends OutputStream {
    OutputStream out;
    int booleanCount;
    int booleanBuffer;
    IdHashtable stringTable = new IdHashtable(true);
    int stringCount = 2;

    public CompactOutputStream(OutputStream out) {
        super();
        this.out = out;
    }

    private final void flushBooleanBuffer() throws IOException {
        if (booleanCount > 0) {
            out.write(booleanBuffer);
            booleanCount = 0;
            booleanBuffer = 0;
        }
    }

    public void write(int value) throws IOException {
        flushBooleanBuffer();
        out.write(value);
    }

    public void write(byte value[]) throws IOException {
        flushBooleanBuffer();
        out.write(value, 0, value.length);
    }

    public void write(byte value[], int offset, int length)
        throws IOException {
        flushBooleanBuffer();
        out.write(value, offset, length);
    }

    public void flush() throws IOException {
        flushBooleanBuffer();
        out.flush();
    }

    public void close() throws IOException {
        flushBooleanBuffer();
        out.close();
    }

    public void writeCompactBoolean(boolean value) throws IOException {
        int buf, count;

        count = booleanCount;

        if (value) {
            buf = booleanBuffer;
            buf = buf | (1 << count);
            count++;

            if (count >= 8) {
                out.write(buf);
                booleanCount = 0;
                booleanBuffer = 0;
            } else {
                booleanCount = count;
                booleanBuffer = buf;
            }
        } else {
            count++;

            if (count >= 8) {
                out.write(booleanBuffer);
                booleanCount = 0;
                booleanBuffer = 0;
            } else {
                booleanCount = count;
            }
        }
    }

    public void writeBoolean(boolean value) throws IOException {
        if (value)
            out.write(1);
        else
            out.write(0);
    }

    public void writeByte(int value) throws IOException {
        flushBooleanBuffer();
        out.write(value);
    }

    public void writeShort(int value) throws IOException {
        OutputStream out = this.out;

        flushBooleanBuffer();
        out.write((value >>> 8) & 0xff);
        out.write((value >>> 0) & 0xff);
    }

    public void writeChar(int value) throws IOException {
        OutputStream out = this.out;

        flushBooleanBuffer();
        out.write((value >>> 8) & 0xff);
        out.write((value >>> 0) & 0xff);
    }

    /** This writes out specially packed ints.
      */
    public void writeCompactInt(int value) throws IOException {
        OutputStream out = this.out;

        flushBooleanBuffer();

        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                out.write(0x40);
                return;
            }

            value = -value;
            if (value < 0x40) {
                out.write(value | 0x40);
                return;
            } else {
                out.write((value & 0xff) | 0xc0);
            }
        } else {
            if (value < 0x40) {
                out.write(value);
                return;
            } else {
                out.write((value & 0x3f) | 0x80);
            }
        }

        value = value >>> 6;
        if (value < 0x80) {
            out.write(value);
        } else {
            out.write((value & 0xff) | 0x80);
            value = value >>> 7;
            if (value < 0x80) {
                out.write(value);
            } else {
                out.write((value & 0xff) | 0x80);
                value = value >>> 7;
                if (value < 0x80) {
                    out.write(value);
                } else {
                    out.write((value & 0xff) | 0x80);
                    value = value >>> 7;
                    if (value > 0)
                        out.write(value);
                }
            }
        }
    }

    public void writeInt(int value) throws IOException {
        OutputStream out = this.out;

        flushBooleanBuffer();
        out.write((value >>> 24) & 0xff);
        out.write((value >>> 16) & 0xff);
        out.write((value >>>  8) & 0xff);
        out.write((value >>>  0) & 0xff);
    }

    public void writeLong(long value) throws IOException {
        OutputStream out = this.out;

        flushBooleanBuffer();
        out.write((int)(value >>> 56) & 0xff);
        out.write((int)(value >>> 48) & 0xff);
        out.write((int)(value >>> 40) & 0xff);
        out.write((int)(value >>> 32) & 0xff);
        out.write((int)(value >>> 24) & 0xff);
        out.write((int)(value >>> 16) & 0xff);
        out.write((int)(value >>>  8) & 0xff);
        out.write((int)(value >>>  0) & 0xff);
    }

    public void writeFloat(float value) throws IOException {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeBytes(String s) throws IOException {
        int i, length;
        OutputStream out = this.out;

        flushBooleanBuffer();
        length = s.length();

        for (i = 0; i < length; i++) {
            out.write((byte)s.charAt(i));
        }
    }

    public void writeChars(String s) throws IOException {
        int i, length, value;
        OutputStream out = this.out;

        flushBooleanBuffer();
        length = s.length();

        for (i = 0; i < length; i++) {
            value = s.charAt(i);
            out.write((value >>> 8) & 0xff);
            out.write((value >>> 0) & 0xff);
        }
    }

    /** Unlike DataOutputStream, this can take a null String.
      */
    public void writeCompactUTF(String str) throws IOException {
        int id, length;

        // null and "" are handled specially.  They are assigned the
        // ids 0 and 1 respectively.

        if (str == null) {
            writeCompactInt(0);
            return;
        } else if (str.length() == 0) {
            writeCompactInt(1);
            return;
        }

        // If we have already written out this String, then just write out the
        // handle which is a positive int (not 0).

        id = stringTable.get(str);
        if (id == IdHashtable.NOT_FOUND) {
            id = stringCount++;
            stringTable.putKnownAbsent(str, id);

            // The id of a string is determined by the order it appears in the
            // stream.  They start at 2 and go up from there (null and "" are
            // handled specially above).  To indicate that we are not writing
            // an id, we write out the length as a negative number.

            length = utfLength(str);
            writeCompactInt(-length);
            writeUTFBytes(str);
        } else {
            writeCompactInt(id);
        }
    }

    public void writeUTF(String str) throws IOException {
        int utflen;
        OutputStream out = this.out;

        flushBooleanBuffer();

        if (str == null) {
            out.write(0xff);
            out.write(0xff);
            return;
        }

        utflen = utfLength(str);
        if (utflen >= 0xffff) {
            throw new IOException("string too long");
        }

        out.write((utflen >>> 8) & 0xff);
        out.write((utflen >>> 0) & 0xff);

        writeUTFBytes(str);
    }

    private final int utfLength(String str) {
        int i, c, strlen, utflen;

        strlen = str.length();
        utflen = 0;

        for (i = 0; i < strlen ; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007f)) {
                utflen++;
            } else if (c > 0x07ff) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        return utflen;
    }

    private final void writeUTFBytes(String str) throws IOException {
        int i, c, strlen;
        OutputStream out = this.out;

        strlen = str.length();

        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007f)) {
                out.write(c);
            } else if (c > 0x07ff) {
                out.write(0xe0 | ((c >> 12) & 0x0f));
                out.write(0x80 | ((c >>  6) & 0x3f));
                out.write(0x80 | ((c >>  0) & 0x3f));
            } else {
                out.write(0xc0 | ((c >>  6) & 0x1f));
                out.write(0x80 | ((c >>  0) & 0x3f));
            }
        }
    }
}
