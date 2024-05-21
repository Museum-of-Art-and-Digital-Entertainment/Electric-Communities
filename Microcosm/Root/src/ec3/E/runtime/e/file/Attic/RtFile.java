package ec.e.file;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.net.InetAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InternalError;
import java.io.RandomAccessFile;
import java.io.File;

import ec.e.dgc.*;
import ec.e.run.*;
import ec.e.db.*;
import ec.e.stream.*;
import ec.eload.*;
import ec.e.cap.ECapability;
import ec.e.cap.ERestrictionException;
import ec.e.cap.ERestrictedException;

final public class RtFile implements ECapability, Cloneable {
	static Trace tr = new Trace(false, "[RtFile]");

	private long expiration; // milliseconds since epoch
	private boolean canWrite;
	private boolean canRead;
	private boolean canAppend;
	private long sizeLimit;
	private long sizeCurrent;
	private long maxduration; // milliseconds

	private RandomAccessFile file;

//
// Constructors
//

	// only called by RtDirectory
	RtFile(String filename, boolean read, boolean write, boolean append,
				long sizelimit) throws IOException { 
		if (tr.tracing) tr.$("creating new RtFile: " + filename);
		String mode;
		
		canWrite = write;
		canRead = read;
		canAppend = append;
		sizeLimit = sizelimit; 

		expiration = Long.MAX_VALUE ; // durations useful for directory objects?
		maxduration = Long.MAX_VALUE ;

		mode = "";
		if (read) mode = mode + "r";
		if (write || append) mode = mode + "w";
		file = new RandomAccessFile(filename, mode);
	
		if (append && !read && !write) {
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

	public void restrict(String kind, String restriction) throws ERestrictionException {
		if (kind == "expiration") {
			long newexp = RtUtil.parseExpirationDate(restriction, false);
			if (newexp < expiration) {
				expiration = newexp ;
			}
		}
		else if (kind == "access") {
			if      (restriction == "NoWrite") canWrite = false ;
			else if (restriction == "NoRead") canRead = false ;
			else if (restriction == "NoAppend") canAppend = false ;
			else throw new ERestrictionException(kind + ":" + restriction);
		}
		else if (kind == "size") {
			long newsize = Long.parseLong(restriction);
			if (newsize >= 0 && newsize < sizeLimit) {
				sizeLimit = newsize ;
			}  
		}
		else if (kind == "maxduration") {
			long newduration = RtUtil.parseExpirationDate(restriction, true);
			if (newduration >= 0 && newduration < maxduration) {
				maxduration = newduration ;
			}
		}
		else {
			throw new ERestrictionException(kind + ":" + restriction);
		}
	}

	public void close() throws IOException {
		file.close();
	}
	
//
// Public methods
//

// file information and seeking 
	 
	public long getFilePointer() throws ERestrictedException, IOException {
		if (!canRead && !canWrite) { // append does not allow seek 
			throw new ERestrictedException("RtFile CanRead");
		} else return file.getFilePointer();
	}

	public void seek(long pos) throws ERestrictedException, IOException {
		if (!canRead && !canWrite) { // append does not allow seek
			throw new ERestrictedException("RtFile CanRead");
		} else file.seek(pos);
	}

	public int skipBytes(int n) throws ERestrictedException, IOException { 
		if (!canRead && !canWrite) { // append does not allow seek
			throw new ERestrictedException("RtFile CanRead");
		} else return file.skipBytes(n);
	}

	public long length() throws ERestrictedException, IOException {
		if (canRead || canWrite) {
			return file.length();
		} else throw new ERestrictedException("RtFile CanRead");
	}

// read functions

	public int read() throws ERestrictedException, IOException {
		if (canRead) {	
			return file.read();
		} else throw new ERestrictedException("RtFile CanRead");
	} 

	public int read(byte b[], int off, int len) throws ERestrictedException, IOException {
		if (canRead) {
			return file.read(b, off, len);
		} else throw new ERestrictedException("RtFile CanRead");
	} 

	public int read(byte b[]) throws ERestrictedException, IOException {
		if (canRead) {
			return file.read(b);
		} else throw new ERestrictedException("RtFile CanRead");
	} 

	public final void readFully(byte b[]) throws ERestrictedException, IOException {
		if (canRead) {
			file.readFully(b);
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final void readFully(byte b[], int off, int len) throws ERestrictedException, IOException {
		if (canRead) {
			file.readFully(b, off, len);
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final boolean readBoolean() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readBoolean();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final byte readByte() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readByte();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final int readUnsignedByte() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readUnsignedByte();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final short readShort() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readShort();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final int readUnsignedShort() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readUnsignedShort();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final char readChar() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readChar();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final int readInt() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readInt();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final long readLong() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readLong();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final float readFloat() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readFloat();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final double readDouble() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readDouble();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final String readLine() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readLine();
		} else throw new ERestrictedException("RtFile CanRead");
	}

	public final String readUTF() throws ERestrictedException, IOException {
		if (canRead) {
			return file.readUTF();
		} else throw new ERestrictedException("RtFile CanRead");
	}

// write functions
	
	public void write(byte b[]) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			if ((sizeCurrent + b.length) > sizeLimit) {
				throw new ERestrictedException("RtFile SizeLimit");	
			} else {
				sizeCurrent += b.length;	
				file.write(b);
			}
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public void write(byte b[], int off, int len) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			if ((sizeCurrent + len) > sizeLimit) {
				throw new ERestrictedException("RtFile SizeLimit");	
			} else {
				sizeCurrent += len;	
				file.write(b, off, len);
			}
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeBoolean(boolean v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeBoolean(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeByte(int v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeByte(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeShort(int v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeShort(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeChar(int v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeChar(v);
		} else throw new ERestrictedException("RtFile CanWrite"); 
	}

	public final void writeInt(int v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeInt(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeLong(long v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeLong(v);
		} else throw new ERestrictedException("RtFile CanWrite"); 
	}

	public final void writeFloat(float v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeFloat(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeDouble(double v) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeDouble(v);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeBytes(String s) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeBytes(s);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeChars(String s) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeChars(s);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

	public final void writeUTF(String str) throws ERestrictedException, IOException {
		if (canWrite || canAppend) {
			file.writeUTF(str);
		} else throw new ERestrictedException("RtFile CanWrite");
	}

}

//
// Exceptions
//

public class RtFileException extends Exception {
	public RtFileException(String message) {
		super(message);
	}
}
