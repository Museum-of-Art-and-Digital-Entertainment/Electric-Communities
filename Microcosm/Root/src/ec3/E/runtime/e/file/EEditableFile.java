package ec.e.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import ec.e.start.Vat;
import ec.e.start.Tether;


/**
* A sturdy representation of a file which may be opened for editing (reading
* or writing, sequential or random-access).
*/
public class EEditableFile extends EDirectoryEntry implements EFileInfo {
    /**
    * Construct an EEditableFile given a file and a vat. Only called within
    * the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EEditableFile(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce a representation of this file which can only be opened for
    * appending.
    */
    public EAppendableFile asAppendableFile() {
        return(new EAppendableFile(myVat, myFile));
    }

    /**
    * Produce a representation of this file which can only be opened for
    * reading.
    */
    public EReadableFile asReadableFile() {
        return(new EReadableFile(myVat, myFile));
    }

    /**
    * Produce an instance of EFileEditor that will read and write this file.
    */
    public EFileEditor editor() throws IOException {
        return(new EFileEditor(myVat, myFile));
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
    * Determines the time that the file represented by this File object was
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
    * Produce an OutputStream that will write this file.
    */
    public OutputStream outputStream() throws IOException {
        return new EOutputStream(new Tether(myVat,
                                            new FileOutputStream(myFile)));
    }
}



