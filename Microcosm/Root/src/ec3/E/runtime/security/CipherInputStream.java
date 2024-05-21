package ec.security;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import ec.security.Cipher;

/* this implementation calls crypt() on arbitrary boundries */
public class CipherInputStream extends FilterInputStream 
{
    private Cipher myCipher;
    private byte myBuf[] = new byte[1];
    
    public CipherInputStream(InputStream ins, Cipher c) {
        super(ins);
        myCipher = c;
        int state = myCipher.getState();
        if (state != Cipher.ENCRYPT && state != Cipher.DECRYPT) {
            throw new IllegalArgumentException("Cipher state not initialized");
        }
    }

    public int read() throws IOException {
        if (read(myBuf, 0, 1) <= 0)
            return -1;
        else
            return myBuf[0];
    }

    public int read(byte b[], int off, int len) throws IOException {
        byte inbuf[] = new byte[len];
        int mylen = in.read(inbuf, 0, len);
        if (mylen > 0)
            myCipher.crypt(inbuf, 0, mylen, b, off);
        return mylen;
    }
}
