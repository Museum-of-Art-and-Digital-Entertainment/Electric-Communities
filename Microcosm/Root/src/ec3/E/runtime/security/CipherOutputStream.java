package ec.security;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
//import java.security.Cipher;
import ec.security.Cipher;

/* this implementation calls crypt() on arbitrary boundries */
public class CipherOutputStream extends FilterOutputStream 
{
    private Cipher myCipher;
    private byte myBuf[] = new byte[1];
    
    public CipherOutputStream(OutputStream outs, Cipher c) {
        super(outs);
        myCipher = c;
        int state = myCipher.getState();
        if (state != Cipher.ENCRYPT && state != Cipher.DECRYPT) {
            throw new IllegalArgumentException("Cipher state not initialized");
        }
    }

    public synchronized void write(int b) throws IOException {
        myBuf[0] = (byte)b;
        write(myBuf, 0, 1);
    }

    public synchronized void write(byte b[], int off, int len) throws IOException {
        byte outbuf[] = new byte[len];
        myCipher.crypt(b, off, len, outbuf, 0);
        out.write(outbuf, 0, len);
    }

    private void doFlush() throws IOException {
    }

    public synchronized void flush() throws IOException {
    }

    public void close() throws IOException {
        out.close();
    }
}

/* this implementation only calls crypt() on blocksize boundries
public class CipherOutputStream extends FilterOutputStream 
{
    private Cipher myCipher;
    private boolean myEncrypt; // don't really need to know unless doing padding right
    private int myBlockSize;
    private int myBufLen;
    private byte myBuf[];
    private int myHead;
    
    public CipherOutputStream(OutputStream outs, Cipher c) {
        super(outs);
        myCipher = c;
        int state = myCipher.getState();
        if (state == Cipher.ENCRYPT) {
            myEncrypt = true;
        }
        else if (state == Cipher.DECRYPT) {
            myEncrypt = false;
        }
        else {
            throw new IllegalArgumentException("Cipher state not initialized");
        }
        myBlockSize = myCipher.blockSize();
        myBufLen = myBlockSize * 512 ;
        myBuf = new byte[myBufLen];
        myHead = 0 ;
    }

    public synchronized void write(int b) throws IOException {
        myBuf[myHead++] = (byte)b;
        if (myHead >= myBlockSize) {
            doFlush();
        }
    }

    public synchronized void write(byte b[], int off, int len) throws IOException {
        while (len > 0) {
            int avail = myBufLen - myHead ;
            if (avail > len) avail = len;
            System.arraycopy(b, off, myBuf, myHead, avail);
            off += avail;
            len -= avail;
            myHead += avail;
            if (myHead >= myBlockSize) {
                doFlush();
            }
        }
    }

    private void doFlush() throws IOException {
        int excess = (myHead % myBlockSize);
        int len = myHead - excess;
        myCipher.crypt(myBuf, 0, len, myBuf, 0);
        out.write(myBuf, 0, len);
        if (excess > 0) {
            System.arraycopy(myBuf, len, myBuf, 0, excess);
        }
        myHead = excess;
    }

    public synchronized void flush() throws IOException {
        if (myHead > 0) {
            while (myHead < myBlockSize) {
                myBuf[myHead++] = (byte)0;
            }
            doFlush();
        }
    }

    public void close() throws IOException {
        flush();
        out.close();
    }
}
*/
