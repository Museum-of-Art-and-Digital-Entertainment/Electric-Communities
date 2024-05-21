package ec.e.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import ec.e.start.Vat;
import ec.e.start.Tether;


/**
* A sturdy representation of a file which may be opened for appending.
*/
public class EAppendableFile extends EDirectoryEntry {
    /**
    * Construct an EAppendableFile given a file and a vat. Only called within
    * the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EAppendableFile(Vat vat, File file) {
        super(vat, file);
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
    * Produce an OutputStream that will write this file.
    */
    public OutputStream outputStream() throws IOException {
        return new EOutputStream(new Tether(myVat, 
                                            new FileOutputStream(myFile)));
    }
}
