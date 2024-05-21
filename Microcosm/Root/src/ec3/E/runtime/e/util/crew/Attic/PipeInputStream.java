package ec.e.util.crew;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
   Copy data from an InputStream to an OutputStream.  The flow of data
   is driven from outside this class.  Create a PipeInputStream,
   giving it the size of the pipe buffer.  Then, call getOutputStream,
   which returns an OutputStream to place data into the pipe with.
   Data written into the OutputStream can be read from the
   PipeInputStream.

   @see ec.e.util.crew.PipeOutputStream
*/
public class PipeInputStream extends InputStream
{
    static private final Trace tr = new Trace("ec.e.util.crew.PipeInputStream");
    
    PipeInputStreamFeeder myFeeder;
    byte myBuf[];
    int myHead;
    int myTail;
    int myLen;
    int myEOF;
    boolean myClosed = false;

    private PipeInputStream() {}
    
    public PipeInputStream(int buflen) {
        myBuf = new byte[buflen];
        myHead = 0;
        myTail = 0;
        myLen = buflen;
        myEOF = -1;
        myFeeder = new PipeInputStreamFeeder(this);
    }

    public OutputStream getOutputStream() {
        return (OutputStream)myFeeder;
    }

    //////////////////////////////////////////////////////////
    // interface used by PipeInputStreamFeeder

    synchronized void write(byte b[], int off, int len) throws InterruptedIOException {
        while (len > 0) {
            if (myClosed) {
                throw new InterruptedIOException("broken pipe");
            }

            int space = 0;
            if (myHead < myTail) {
                space = myTail - myHead - 1;
            }
            else if (myTail == 0) {
                space = myLen - myHead - 1 ;
            }
            else {
                space = myLen - myHead;
            }
            
            if (space > 0) {
                if (space > len) {
                    space = len;
                }
                System.arraycopy(b, off, myBuf, myHead, space);
                len -= space;
                off += space;
                myHead += space;
                if (myHead >= myLen) myHead = 0 ;
                this.notify();
            }
            if (len > 0) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                    // can't hurt to just try again
                }
            }
        }
    }

    synchronized void eof() {
        myEOF = myHead;
        this.notify();
    }

    synchronized private boolean waitForData() {
        while (true) {
            if (myHead != myTail || myTail == myEOF) {
                if (tr.verbose) tr.verbosem("waitForData myHead=" + myHead + ", myTail=" + myTail + ", myEOF=" + myEOF);
                return myTail == myEOF;
            }
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                // can't hurt to just try again
            }
        }
    }
    
    //////////////////////////////////////////////////////////
    // InputStream interface

    /**
     * Reads a byte of data. This method will block if no input is 
     * available.
     * @return  the byte read, or -1 if the end of the
     *      stream is reached.
     * @exception IOException If an I/O error has occurred.
     */
    synchronized public int read() throws IOException {
        if (waitForData()) return -1;
        int ret = ((int)myBuf[myTail++]) & 0xff;
        if (myTail >= myLen) {
            myTail = 0 ;
        }
        this.notify();
        return ret;
    }

    /**
     * Reads into an array of bytes.  This method will
     * block until some input is available.
     * @param b the buffer into which the data is read
     * @return  the actual number of bytes read, -1 is
     *      returned when the end of the stream is reached.
     * @exception IOException If an I/O error has occurred.
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads into an array of bytes.  This method will
     * block until some input is available.
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return  the actual number of bytes read, -1 is
     *      returned when the end of the stream is reached.
     * @exception IOException If an I/O error has occurred.
     */
    synchronized public int read(byte b[], int off, int len) throws IOException {
        if (waitForData()) return -1;
        int dataAvailable;
        if (myHead < myTail) {
            dataAvailable = myLen - myTail ;
        }
        else {
            dataAvailable = myHead - myTail ;
        }
        if (len > dataAvailable) {
            len = dataAvailable;
        }
        System.arraycopy(myBuf, myTail, b, off, len);
        myTail += len;
        if (myTail >= myLen) {
            myTail = 0 ;
        }
        this.notify();
        return len;
    }

    /**
     * Returns the number of bytes that can be read
     * without blocking.
     * @return the number of available bytes.
     */
    synchronized public int available() throws IOException {
        if (myHead < myTail) {
            return myLen - myTail;
        }
        else {
            return myHead - myTail;
        }
    }

    public void close() throws IOException {
        myClosed = true;
    }
}



class PipeInputStreamFeeder extends OutputStream
{
    PipeInputStream myConsumer;
    
    PipeInputStreamFeeder(PipeInputStream pis) {
        myConsumer = pis;
    }

    // OutputStream interface
    
    /**
     * Writes a byte. This method will block until the byte is actually
     * written.
     * @param b the byte
     * @exception IOException If an I/O error has occurred.
     */
    public void write(int b) throws IOException {
        byte tmp[] = new byte[1];
        tmp[0] = (byte)b;
        myConsumer.write(tmp, 0, 1);
    }
    

    /**
     * Writes an array of bytes. This method will block until the bytes
     * are actually written.
     * @param b the data to be written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[]) throws IOException {
        myConsumer.write(b, 0, b.length);
    }

    /**
     * Writes a sub array of bytes. 
     * @param b the data to be written
     * @param off   the start offset in the data
     * @param len   the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[], int off, int len) throws IOException {
        myConsumer.write(b, off, len);
    }

    /**
     * Flushes the stream. This will write any buffered
     * output bytes.
     * @exception IOException If an I/O error has occurred.
     */
    public void flush() throws IOException {
    }

    /**
     * Closes the stream. This method must be called
     * to release any resources associated with the
     * stream.
     * @exception IOException If an I/O error has occurred.
     */
    public void close() throws IOException {
        myConsumer.eof();
    }
}
