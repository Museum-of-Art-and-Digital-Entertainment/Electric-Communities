/**
 * LittleEndianInputStream.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Tony Grant
 * October 7 1997
 *
 * Java presumes a big-endian ordering in its input streams.
 * BMP file are little-endian.
 * This class allows for simple reading of ints and shorts with little-endian ordering.
 * 
 * This inherits from FilterInputStream and simply swaps the ordering of the bytes in the methods it overrides.
 * <covering butt>
 * Dima siad he was "90% sure" that the methods not overwritten don't rely on any endian ordering
 * and thus don't need to be overridden.
 * </covering butt>
 */

package ec.misc;

import java.io.*;

public class LittleEndianInputStream extends FilterInputStream {
    // Public Constructor

    public LittleEndianInputStream(InputStream in)  {
        super(in);
    }
    
    // Builds an int by reading in 4 bytes and packing them in the
    // opposite order of the Java default 
    public final int readInt() throws IOException {
        InputStream in = this.in;
        int ch4 = in.read();
        int ch3 = in.read();
        int ch2 = in.read();
        int ch1 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)  {
             throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    
    // Builds an short by reading in 2 bytes and packing them in the
    // opposite order of the Java default 
    public final short readShort() throws IOException {
        InputStream in = this.in;
        int ch2 = in.read();
        int ch1 = in.read();
        if ((ch1 | ch2) < 0)  {
             throw new EOFException();
        }
        return (short)((ch1 << 8) + (ch2 << 0));
    }
    
    // Returns a byte   
    public final byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    public final int readUnsignedByte() throws IOException {
    int ch = in.read();
    if (ch < 0)
        throw new EOFException();
    return ch;
    }
    
    // Skips a number of bytes
    public final int skipBytes(int n) throws IOException {
        InputStream in = this.in;
        for (int i = 0 ; i < n ; i += (int)in.skip(n - i));
        return n;
    }
}