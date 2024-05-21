package ec.e.util.crew;

import ec.util.EThreadGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.EOFException;

/**
   Copy data from an InputStream to an OutputStream.  The flow of data
   is driven from inside this class.  Create a PipeOuputStream, giving
   it the size of the pipe buffer, an InputStream, and an
   OutputStream.  Then, call start() to initiate reading from the
   InputStream.  Data read from the InputStream will be written in
   chunks the size of the pipe buffer onto the OutputStream.

   @see ec.e.util.crew.PipeIntputStream
*/
public class PipeOutputStream extends Thread
{
    private byte myBuf[];
    private int myLen;
    private int myOffset;
    private InputStream myInput;
    private OutputStream myOutput;
    
    private PipeOutputStream() {}

    public PipeOutputStream(int bufsiz, InputStream ins, OutputStream outs) {
        myInput = ins;
        myOutput = outs;
        myLen = bufsiz;
        myOffset = 0;
        myBuf = new byte[myLen];
    }
    
    public void run() {
        while (true) {
            int len = myLen - myOffset;
            int nread;
            try {
                nread = myInput.read(myBuf, myOffset, len);
            }
            catch (EOFException e) {
                nread = -1 ;
            }
            catch (IOException e) {
                EThreadGroup.reportException(e);
                nread = -1 ;
            }
            
            if (nread > 0) {
                myOffset += nread ;
                if (myOffset < myLen) continue;
            }
            if (myOffset > 0) {
                try {
                    myOutput.write(myBuf, 0, myOffset);
                }
                catch (InterruptedIOException e) {
                    // This should be broken pipe exception if we get here
                    // It means the input stream was closed, which is a normal ending
                    break;
            }
                catch (IOException e) {
                    EThreadGroup.reportException(e);
                    break;
                }
                myOffset = 0;
            }
            if (nread <= 0) {
                break;
            }
        }
        try {
            myInput.close();
        }
        catch (IOException e) {
            EThreadGroup.reportException(e);
        }
        try {
            myOutput.flush();
        }
        catch (IOException e) {
            EThreadGroup.reportException(e);
        }
        try {
            myOutput.close();
        }
        catch (IOException e) {
            EThreadGroup.reportException(e);
        }
    }
}





