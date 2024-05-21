package ec.e.file;

import java.io.InputStream;
import java.io.IOException;
import ec.e.start.Tether;

/**
  An input stream based on the E file classes rather than the Java file
  classes. The idea is to emulate the function of the java InputStream class,
  but do so safely in the Vat environment. Thus we open from an E file object
  and hold onto the open stream with a tether.
*/
class EInputStream extends InputStream {
    private Tether myStreamHolder;

    /**
    * Construct an EInputStream given a Tether holding onto a primitive Java
    * InputStream. Note that the constructor is not public: the only way to get
    * an EInputStream is from one of the E file classes which knows how to
    * call this constructor within the package.
    *
    * @param streamHolder A leaf Tether holding the stream to input from.
    * @throws IOException If an I/O error occurs.
    */
    EInputStream(Tether streamHolder) throws IOException {
        myStreamHolder = streamHolder;
    }

    /*
      Public methods replicate the public interface to InputStream.

      XXX Arguably the interfaces of all the actual read methods should be
      modified to throw an EOFException on end of file rather than returning a
      flag value, but legacy code depends on the present behavior so for now
      I'm leaving it the way it is.
    */

    /**
    * Return the number of bytes that can be read from this stream without
    * blocking.
    *
    * @throws IOException If an I/O error occurs.
    */
    public int available() throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        return(stream.available());
    }

    /**
    * Closes this stream and releases any system resources associated with it,
    * by calling the stream's close method.
    *
    * @throws IOException If an I/O error occurs.
    */
    public void close() throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        stream.close();
    }

    /**
    * Reads a byte of data from this stream, by calling the stream's read()
    * method.
    *
    * @return the next byte of data
    * @throws IOException If an I/O error occurs.
    */
    public int read() throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        return(stream.read());
    }

    /**
    * Reads up to buf.length bytes of data from this stream into an array of
    * bytes, by calling the stream's corresponding read(...) method.
    *
    * @param buf the buffer into which the data is read
    * @return the total number of bytes read into the buffer
    * @throws IOException If an I/O error occurs.
    */
    public int read(byte buf[]) throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        return(stream.read(buf));
    }

    /**
    * Reads up to length bytes of data from this stream into an array of bytes,
    * by calling the stream's corresponding read(...) method.
    *
    * @param buf the buffer into which the data is read
    * @param offset the start offset of the data
    * @param length the maximum number of bytes to read
    * @return the total number of bytes read into the buffer
    * @throws IOException If an I/O error occurs.
    */
    public int read(byte buf[], int offset, int length) throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        return(stream.read(buf, offset, length));
    }

    /**
    * Skips over and discards n bytes of data from the input stream, by calling
    * the stream's corresponding skip() method. The skip method may, for a
    * variety of reasons, end up skipping over some smaller number of bytes,
    * possibly zero. The actual number of bytes skipped is returned.
    *
    * @param n the number of bytes to be skipped.
    * @returns the actual number of bytes skipped
    * @throws IOException If an I/O error occurs.
    */
    public long skip(long n) throws IOException {
        InputStream stream = (InputStream) myStreamHolder.held();
        return(stream.skip(n));
    }

    /**
    * Finalize by closing.
    */
    protected void finalize() throws IOException {
        close();    
    }
}
