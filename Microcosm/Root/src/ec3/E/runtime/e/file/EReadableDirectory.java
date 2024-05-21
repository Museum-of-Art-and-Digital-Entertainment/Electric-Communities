package ec.e.file;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import ec.e.start.Vat;
import ec.util.ReadOnlyHashtable;

/**
* A sturdy representation of a directory providing sensory access to its
* contents.
*/
public class EReadableDirectory extends EDirectoryBase {
    /**
    * Construct an EReadableDirectory given a file and a vat. Only called
    * within the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EReadableDirectory(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce an EReadableDirectory representation of this same directory.
    */
    public EReadableDirectory asReadableDirectory() {
        return(this);
    }

    /**
    * Produce a hashtable describing the contents of this directory.
    * @return A Hashtable mapping names of directories to EReadableDirectory
    *   objects and names of files to EReadableFile objects.
    */
    public ReadOnlyHashtable contents() {
        String names[] = myFile.list();
        Hashtable baseResult =
            new Hashtable(names.length == 0 ? 1 : names.length);
        for (int i=0; i<names.length; ++i) {
            File file = new File(myFile, names[i]);
            if (file.isDirectory())
                baseResult.put(names[i], new EReadableDirectory(myVat, file));
            else
                baseResult.put(names[i], new EReadableFile(myVat, file));
        }
        return(new ReadOnlyHashtable(baseResult));
    }

    /**
    * Lookup a specific directory pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EReadableDirectory corresonding to the path in question.
    */
    public EReadableDirectory lookupDirectory(String path) throws IOException {
        return(new EReadableDirectory(myVat, lookupDirectoryPath(path)));
    }

    /**
    * Lookup a specific file pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EReadableFile corresonding to the path in question.
    */
    public EReadableFile lookupFile(String path) throws IOException {
        return(new EReadableFile(myVat, lookupFilePath(path)));
    }
}
