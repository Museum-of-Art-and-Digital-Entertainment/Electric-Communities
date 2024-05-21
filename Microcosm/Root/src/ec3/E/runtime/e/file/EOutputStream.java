package ec.e.file;

import java.io.OutputStream;
import java.io.IOException;
import ec.e.start.Tether;

/**
  An output stream based on the E file classes rather than the Java file
  classes. The idea is to emulate the function of the java OutputStream class,
  but do so safely in the Vat environment. Thus we open from an E file object
  and hold onto the open stream with a leaf.
*/
class EOutputStream extends OutputStream {
    private Tether myStreamHolder;

    /**
    * Construct an EOutputStream given a Tether holding onto a primitive Java
    * OutputStream. Note that the constructor is not public: the only way to
    * get an EOutputStream is from one of the E file classes which knows how to
    * call this constructor within the package.
    *
    * @param streamHolder A leaf tether holding the stream to input from.
    * @throws IOException If an I/O error occurs.
    */
    EOutputStream(Tether streamHolder) throws IOException {
        myStreamHolder = streamHolder;
    }

    /*
      Public replicate the public interface to OutputStream.
    */

    /**
    * Closes this stream and releases any system resources associated with it,
    * by calling the stream's close method.
    *
    * @throws IOException If an I/O error occurs.
    */
    public void close() throws IOException {
        OutputStream stream = (OutputStream) myStreamHolder.held();
        stream.close();
    }

    /**
    * Writes the specified byte to this stream.
    *
    * @param b The byte to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(int b) throws IOException {
        OutputStream stream = (OutputStream) myStreamHolder.held();
        stream.write(b);
    }

    /**
    * Writes buf.length bytes from the specified byte array to this stream.
    *
    * @param buf The bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[]) throws IOException {
        OutputStream stream = (OutputStream) myStreamHolder.held();
        stream.write(buf);
    }

    /**
    * Writes length bytes from the specified byte array starting at offset to
    * this stream.
    *
    * @param buf The bytes to be written.
    * @param offset The start offset (in buf) of the data.
    * @param length The number of bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[], int offset, int length) throws IOException {
        OutputStream stream = (OutputStream) myStreamHolder.held();
        stream.write(buf, offset, length);
    }

    /**
    * Finalize by closing.
    */
    protected void finalize() throws IOException {
        close();    
    }
}
