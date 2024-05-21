package ec.e.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import ec.e.start.Vat;
import ec.e.start.Tether;


/**
* A sturdy representation of a file which may be opened for (sequential or
* random-access) reading.
*/
public class EReadableFile extends EDirectoryEntry implements EFileInfo {
    /**
    * Construct an EReadableFile given a file and a vat. Only called within
    * the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EReadableFile(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce a representation of this file which can only be opened for
    * reading. At this level this is a no-op.
    */
    public EReadableFile asReadableFile() {
        return this;
    }

    /**
    * Produce an InputStream that will read this file.
    */
    public InputStream inputStream() throws IOException {
        return new EInputStream(new Tether(myVat, 
                                           new FileInputStream(myFile)));
    }

    /**
    * Test if this entry describes a directory (it doesn't).
    *
    * @return false, since this is a file
    */
    public boolean isDirectory() {
        return(false);
    }

    /**
    * Length in bytes of this file.
    */
    public long length() {
        return(myFile.length());
    }

    /**
    * Determines the time that the file represented by this object was
    * last modified. As per the Java class library spec, the return value is
    * system-dependent and should only be used to compare with other values
    * returned by lastModified(). It should not be interpreted as an absolute
    * time.
    *
    * @return the "time" the file specified by this object was last modified
    */
    public long lastModified() {
        return(myFile.lastModified());
    }

    /**
    * Produce an instance of EFileReader that will read this file.
    */
    public EFileReader reader() throws IOException {
        return(new EFileReader(myVat, myFile));
    }
}

