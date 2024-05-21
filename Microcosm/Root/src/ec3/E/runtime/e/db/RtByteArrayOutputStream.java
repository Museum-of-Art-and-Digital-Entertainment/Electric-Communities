package ec.e.db;
import java.io.*;

public class RtByteArrayOutputStream extends ByteArrayOutputStream {
    protected int pos;

    public RtByteArrayOutputStream(int size) {
        super(size);
    }
    
    public void seek (long position) {
        pos = (int)position;
        if (pos >= buf.length)
            increaseBufferSize(pos);
        if (pos > count)
            count = pos;
    }
    
    public long position () {
        return((long)pos);
    }
    
    public synchronized void write(int b) {
        if (pos >= buf.length)
            increaseBufferSize(pos+1);
        buf[pos++] = (byte)b;
        if (pos > count)
            count = pos;
    }
    
    public synchronized void write(byte b[], int off, int len) {
        int newpos = pos + len;
        if (newpos > buf.length) {
            increaseBufferSize(newpos);
        }
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
        if (pos > count)
            count = pos;
    }
    
    public synchronized void reset() {
        count = 0;
        pos = 0;
    }
    
    private void increaseBufferSize (int newcount) {
        byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
    }
}
