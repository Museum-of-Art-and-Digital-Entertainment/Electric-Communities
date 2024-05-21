package ec.e.file;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import ec.e.quake.FragileLeaf;
import ec.e.quake.Vat;

/**
  A capability class providing write-only (i.e., append) access to a file.
  Wraps Java's RandomAccessFile class by only providing write access while
  withholding read or seek access or the power to open a file. Also deals with
  quakes by holding onto the file via a tether.
*/
public class EFileAppender implements DataOutput {
    private FragileLeaf myFileHolder;

    /**
    * Construct an EFileAppender given a file. Only called within the package,
    * by EAppendableFile.
    *
    * @param vat The vat we are running in.
    * @param filename The name of the file to open.
    * @throws IOException If an I/O error occurs.
    */
    EFileAppender(Vat vat, File file) throws IOException {
        RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
        randomFile.seek(randomFile.length());
        myFileHolder = vat.makeFragileLeaf((Object) randomFile);
    }

    /*
      Public methods access the safe and useful subset of the public
      interface to RandomAccessFile which have to do with writing.
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
    * Writes the specified byte to this file.
    *
    * @param b The byte to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(int b) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.write(b);
    }

    /**
    * Writes buf.length bytes from the specified byte array to this file.
    *
    * @param buf The bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[]) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.write(buf);
    }

    /**
    * Writes length bytes from the specified byte array starting at offset to
    * this file.
    *
    * @param buf The bytes to be written.
    * @param offset The start offset (in buf) of the data.
    * @param length The number of bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[], int offset, int length) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.write(buf, offset, length);
    }

    /**
    * Writes a boolean to the file as a one-byte value, by calling the file's
    * corresponding writeBoolean() method.
    *
    * @param v A boolean value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeBoolean(boolean v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeBoolean(v);
    }

    /**
    * Writes out a byte to the file as a one-byte value, by calling the file's
    * corresponding writeByte() method.
    *
    * @param v A byte value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeByte(int v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeByte(v);
    }

    /**
    * Writes out the string to the file as a sequence of bytes, by calling the
    * file's corresponding writeBytes() method.
    *
    * @param s A string of bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeBytes(String s) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeBytes(s);
    }

    /**
    * Writes out a char to the file as a two-byte value, by calling the file's
    * corresponding writeChar() method.
    *
    * @param v A char value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeChar(int v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeChar(v);
    }

    /**
    * Writes a string to the file as a sequence of characters, by calling the
    * file's corresponding writeChars() method.
    *
    * @param s A string of chars to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeChars(String s) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeChars(s);
    }

    /**
    * Writes out a double to the file as an eight-byte value, by calling the
    * file's corresponding writeDouble() method.
    *
    * @param v A double value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeDouble(double v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeDouble(v);
    }

    /**
    * Writes out a float to the file as a four-byte value, by calling the
    * file's corresponding writeFloat() method.
    *
    * @param v A float value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeFloat(float v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeFloat(v);
    }

    /**
    * Writes out an int to the file as a four-byte value, by calling the file's
    * correspoding writeInt() method.
    *
    * @param v An int value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeInt(int v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeInt(v);
    }

    /**
    * Writes out a long to the file as an eight-byte value, by calling the
    * file's corresponding writeLong() method.
    *
    * @param v A long value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeLong(long v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeLong(v);
    }

    /**
    * Writes out a short to the file as a two-byte value, by calling the file's
    * corresponding writeShort() method.
    *
    * @param v A short value to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeShort(int v) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeShort(v);
    }

    /**
    * Writes out a string to the file using UTF-8 encoding, by calling the
    * file's corresponding writeUTF() method.
    *
    * @param str A string to be written.
    * @throws IOException If an I/O error occurs.
    */
    public final void writeUTF(String str) throws IOException {
        RandomAccessFile file = (RandomAccessFile) myFileHolder.held();
        file.writeUTF(str);
    }

    /**
    * Finalize by closing.
    */
    protected void finalize() throws IOException {
        close();
    }
}
