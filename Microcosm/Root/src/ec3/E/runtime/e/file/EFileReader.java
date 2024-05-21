package ec.e.file;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import ec.e.start.Tether;
import ec.e.start.Vat;

/**
  A capability class providing read-only access to a file. Wraps Java's
  RandomAccessFile class by only providing read access while withholding write
  access or the power to open a file. Also deals with quakes by holding onto
  the file via a tether.
*/
public class EFileReader implements DataInput {
    Tether myFileHolder;

    /**
    * Construct an EFileReader given a file. Only called within the package, by
    * EReadableFile.
    *
    * @param vat The vat we are running in.
    * @param file The file to open.
    * @throws IOException If an I/O error occurs.
    */
    EFileReader(Vat vat, File file) throws IOException {
        this(vat, file, "r");
    }

    /**
    * Internal constructor used by both EFileReader and EFileEditor.
    *
    * @param vat The vat we are running in.
    * @param file The file to open.
    * @param mode The open mode for RandomAccessFile, "r" or "rw".
    * @throws IOException If an I/O error occurs.
    */
    EFileReader(Vat vat, File file, String mode) throws IOException {
        myFileHolder = new Tether(vat, new RandomAccessFile(file, mode));
    }

    /*
      Public methods access the safe and useful subset of the public
      interface to RandomAccessFile which have to do with reading.

      XXX Arguably the interfaces of all the actual read methods should be
      modified to throw an EOFException on end of file rather than returning a
      flag value, but legacy code depends on the present behavior so for now
      I'm leaving it the way it is.
    */

    /**
    * Closes this file and releases any system resources associated with it,
    * by calling the file's close method.
    *
    * @throws IOException If an I/O error occurs.
    */
    public void close() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.close();
    }

    /**
    * Returns the current offset in this file, by calling the file's
    * getFilePointer method.
    *
    * @throws IOException If an I/O error occurs.
    */
    public long getFilePointer() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.getFilePointer());
    }

    /**
    * Returns the length of this file, by calling the file's length method.
    * @throws IOException If an I/O error occurs.
    */
    public long length() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.length());
    }

    /**
    * Reads a byte of data from this file, by calling the file's read() method.
    *
    * @return the next byte of data
    * @throws IOException If an I/O error occurs.
    */
    public int read() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.read());
    }

    /**
    * Reads up to buf.length bytes of data from this file into an array of
    * bytes, by calling the file's corresponding read(...) method.
    *
    * @param buf the buffer into which the data is read
    * @return the total number of bytes read into the buffer
    * @throws IOException If an I/O error occurs.
    */
    public int read(byte buf[]) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.read(buf));
    }

    /**
    * Reads up to length bytes of data from this file into an array of bytes,
    * by calling the file's corresponding read(...) method.
    *
    * @param buf the buffer into which the data is read
    * @param offset the start offset of the data
    * @param length the maximum number of bytes to read
    * @return the total number of bytes read into the buffer
    * @throws IOException If an I/O error occurs.
    */
    public int read(byte buf[], int offset, int length) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.read(buf, offset, length));
    }

    /**
    * Reads a boolean from this file, by calling the file's corresponding
    * readBoolean() method.
    *
    * @return the boolean value read.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public boolean readBoolean() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return file.readBoolean();
    }

    /**
    * Reads a signed 8-bit value from this file, by calling the file's
    * corresponding readByte() method.
    *
    * @return the byte value read.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public byte readByte() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readByte());
    }

    /**
    * Reads a Unicode character from this file, by calling the file's
    * corresponding readChar() method.
    *
    * @return the next two bytes of this file as a Unicode character.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public char readChar() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readChar());
    }

    /**
    * Reads a double from this file, by calling the file's corresponding
    * readDouble() method.
    *
    * @return the next eight bytes of this file, interpreted as a double.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public double readDouble() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readDouble());
    }

    /**
    * Reads a float from this file, by calling the file's corresponding
    * readFloat() method.
    *
    * @return the next four bytes of this file, interpreted as a float.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public float readFloat() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readFloat());
    }

    /**
    * Reads buf.length bytes from this file into the byte array, by calling the
    * file's corresponding readFully(...) method.
    *
    * @param buf the buffer into which the data is read
    * @throws EOFException If this file reaches the end before reading all the
    *   bytes.
    * @throws IOException If an I/O error occurs.
    */
    public void readFully(byte buf[]) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.readFully(buf);
    }

    /**
    * Reads exactly length bytes from this file into the byte array, by calling
    * the file's corresponding readFully(...) method.
    *
    * @param buf the buffer into which the data is read
    * @param offset the start offset of the data
    * @param length the maximum number of bytes to read
    * @throws EOFException If this file reaches the end before reading all the
    *   bytes.
    * @throws IOException If an I/O error occurs.
    */
    public void readFully(byte buf[], int offset, int length)
    throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.readFully(buf, offset, length);
    }

    /**
    * Reads a signed 32-bit integer from this file, by calling the file's
    * corresponding readInt() method.
    *
    * @returns the next four bytes of this file, interpreted as an int.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public int readInt() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readInt());
    }

    /**
    * Reads the next line of text from this file, by calling the file's
    * corresponding readLine() method.
    *
    * @return the next line of text from this file.
    * @throws IOException If an I/O error occurs.
    */
    public String readLine() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readLine());
    }

    /**
    * Reads a signed 64-bit integer from this file, by calling the file's
    * corresponding readLong() method.
    *
    * @return the next eight bytes of this file, interpreted as a long.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public long readLong() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readLong());
    }

    /**
    * Reads a signed 16-bit number from this file, by calling the file's
    * corresponding readShort() method.
    *
    * @return the next two bytes of this file, interpreted as a signed 16-bit
    *   number.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public short readShort() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readShort());
    }

    /**
    * Reads an unsigned 8-bit number from this file, by calling the file's
    * corresponding readUnsignedByte method.
    *
    * @return the byte value read.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public int readUnsignedByte() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readUnsignedByte());
    }

    /**
    * Reads an unsigned 16-bit number from this file, by calling the file's
    * corresponding readUnsignedShort() method.
    *
    * @return the next two bytes of this file, interpreted as an unsigned
    *   16-bit integer.
    * @throws EOFException If this file has reached the end.
    * @throws IOException If an I/O error occurs.
    */
    public int readUnsignedShort() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readUnsignedShort());
    }

    /**
    * Reads in a UTF-8 encoded string from this file, by calling the file's
    * corresponding readUTF() method.
    *
    * @return a Unicode string
    * @throws EOFException If this file has reached the end.
    * @throws UTFDataFormatException If the bytes do not represent a valid
    *   UTF-8 encoding of a Unicode string.
    * @throws IOException If an I/O error occurs.
    */
    public String readUTF() throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.readUTF());
    }

    /**
    * Sets the offset from the beginning of this file at which the next read
    * occurs, by calling the file's seek method.
    *
    * @param pos the absolute position.
    * @throws IOException If an I/O error occurs.
    */
    public void seek(long pos) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.seek(pos);
    }

    /**
    * Skips exactly n bytes of input, by calling the file's skipBytes method.
    *
    * @param n the number of bytes to be skipped.
    * @throws IOException If an I/O error occurs.
    */
    public int skipBytes(int n) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        return(file.skipBytes(n));
    }

    /**
    * Finalize by closing.
    */
    protected void finalize() throws IOException {
        close();    
    }
}
