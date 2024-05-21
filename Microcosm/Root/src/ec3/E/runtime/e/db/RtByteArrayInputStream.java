package ec.e.db;
import java.io.*;

public class RtByteArrayInputStream extends ByteArrayInputStream {
    public RtByteArrayInputStream (byte bytes[]) {
        super(bytes);
    }
    
    public void seek (long position) {
        if (((int)position) <= buf.length) {
            pos = (int) position;
        } else {
            throw new RtSeekException();
        }
    }
    
    public long position () {
        return((long)pos);
    }

    public byte[] returnBytes() {
        return buf;
    }
}
