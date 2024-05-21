package ec.e.file;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.net.InetAddress;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InternalError;
import java.io.RandomAccessFile;
import java.io.File;

import ec.e.run.*;
import ec.e.db.*;
import ec.e.stream.*;
import ec.eload.*;
import ec.e.cap.ECapability;
import ec.e.cap.ERestrictionException;
import ec.e.cap.ERestrictedException;

final public class EWritableFile extends EReadableFile implements DataInput, DataOutput {
	static Trace tr = new Trace(false, "[EWritableFile]");

	private long expiration; // milliseconds since epoch
	private boolean canWrite;
	private boolean canAppend;
	private long sizeLimit;
	private long sizeCurrent;

//
// Constructors
//

	// only called by EWritableDirectory
	EWritableFile(String filename, boolean write, boolean append) throws IOException { 

		fileName = filename;
		if (tr.tracing) tr.$("EWritableFile opening: " + filename);
		
		canWrite = write;
		canAppend = append;

		expiration = Long.MAX_VALUE ; // durations useful for directory objects?
		maxduration = Long.MAX_VALUE ;

		String mode = "rw";
		if (tr.tracing) tr.$("RandomAccessFile contructor arguments: " + fileName + " " + mode);
		file = new RandomAccessFile(fileName, mode);
	
		if (append) {
			file.seek(file.length());
		} // and from here on, append capability does not allow seeking
	}

//
// ECapability methods
//

	public ECapability copy() {
		try {
			return (ECapability)this.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

//
// Restriction Methods
//

	// not currently in use
	public EWritableFile changeExpiration(long newExpiration)
	{
		if (newExpiration < expiration)
		{
			EWritableFile restrictedFile = (EWritableFile)this.copy();
			restrictedFile.expiration = newExpiration ;
			return restrictedFile;
		}
		else return null; // throw restrictionException?
	}
	
	public EWritableFile restrictWrite()
	{
		EWritableFile restrictedFile = (EWritableFile)this.copy();
		restrictedFile.canWrite = false ;
		return restrictedFile;
	}
	
	public EWritableFile restrictAppend()
	{
		EWritableFile restrictedFile = (EWritableFile)this.copy();
		restrictedFile.canAppend = false ;
		return restrictedFile;
	}
	
	// turns an EWritableFile in read/write mode into an EReadableFile
	public EReadableFile restrictToReadOnly() throws IOException
	{
		if (canWrite) {
			return new EReadableFile(fileName);
		} else throw new IOException("EWritableFile in append mode");
	}

	public void close() throws IOException {
		file.close();
	}
	
//
// Public methods
//

// file information and seeking 
	 
	public long getFilePointer() throws IOException {
		if (canWrite) { // append does not allow seek 
			return file.getFilePointer();
		} else throw new IOException("EWritableFile in append mode");
	}

	public void seek(long pos) throws IOException {
		if (canWrite) { // append does not allow seek
			file.seek(pos);
		} else throw new IOException("EWritableFile in append mode");
	}

	public int skipBytes(int n) throws IOException { 
		if (canWrite) { // append does not allow seek
			return file.skipBytes(n);
		} else throw new IOException("EWritableFile in append mode");
	}

	public long length() throws IOException {
		if (canWrite) { // append does not allow seek
			return file.length();
		} else throw new IOException("EWritableFile in append mode");
	}

// read functions
// these override the EReadableFile methods in order to 
// make sure the user is not in append mode

	public int read() throws IOException {
		if (canWrite) {	
			return file.read();
		} else throw new IOException("EWritableFile in append mode");
	} 

	public int read(byte b[], int off, int len) throws IOException {
		if (canWrite) {
			return file.read(b, off, len);
		} else throw new IOException("EWritableFile in append mode");
	} 

	public int read(byte b[]) throws IOException {
		if (canWrite) {
			return file.read(b);
		} else throw new IOException("EWritableFile in append mode");
	} 

	public final void readFully(byte b[]) throws IOException {
		if (canWrite) {
			file.readFully(b);
		} else throw new IOException("EWritableFile in append mode");
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (canWrite) {
			file.readFully(b, off, len);
		} else throw new IOException("EWritableFile in append mode");
	}

	public final boolean readBoolean() throws IOException {
		if (canWrite) {
			return file.readBoolean();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final byte readByte() throws IOException {
		if (canWrite) {
			return file.readByte();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final int readUnsignedByte() throws IOException {
		if (canWrite) {
			return file.readUnsignedByte();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final short readShort() throws IOException {
		if (canWrite) {
			return file.readShort();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final int readUnsignedShort() throws IOException {
		if (canWrite) {
			return file.readUnsignedShort();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final char readChar() throws IOException {
		if (canWrite) {
			return file.readChar();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final int readInt() throws IOException {
		if (canWrite) {
			return file.readInt();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final long readLong() throws IOException {
		if (canWrite) {
			return file.readLong();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final float readFloat() throws IOException {
		if (canWrite) {
			return file.readFloat();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final double readDouble() throws IOException {
		if (canWrite) {
			return file.readDouble();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final String readLine() throws IOException {
		if (canWrite) {
			return file.readLine();
		} else throw new IOException("EWritableFile in append mode");
	}

	public final String readUTF() throws IOException {
		if (canWrite) {
			return file.readUTF();
		} else throw new IOException("EWritableFile in append mode");
	}

// write functions
	
	public void write(int b) throws IOException {
		file.write(b);
	}

	public void write(byte b[]) throws IOException {
		file.write(b);
	}

	public void write(byte b[], int off, int len) throws IOException {
		file.write(b, off, len);
	}

	public final void writeBoolean(boolean v) throws IOException {
		file.writeBoolean(v);
	}

	public final void writeByte(int v) throws IOException {
		file.writeByte(v);
	}

	public final void writeShort(int v) throws IOException {
		file.writeShort(v);
	}

	public final void writeChar(int v) throws IOException {
		file.writeChar(v);
	}

	public final void writeInt(int v) throws IOException {
		file.writeInt(v);
	}

	public final void writeLong(long v) throws IOException {
		file.writeLong(v);
	}

	public final void writeFloat(float v) throws IOException {
		file.writeFloat(v);
	}

	public final void writeDouble(double v) throws IOException {
		file.writeDouble(v);
	}

	public final void writeBytes(String s) throws IOException {
		file.writeBytes(s);
	}

	public final void writeChars(String s) throws IOException {
		file.writeChars(s);
	}

	public final void writeUTF(String str) throws IOException {
		file.writeUTF(str);
	}

//
// Finalize
//

	protected void finalize() throws IOException {
		close();	
	}

}
