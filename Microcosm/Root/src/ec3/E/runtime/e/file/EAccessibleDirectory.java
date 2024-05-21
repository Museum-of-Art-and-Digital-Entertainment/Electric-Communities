package ec.e.file;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import ec.e.start.Vat;
import ec.util.ReadOnlyHashtable;

/**
* A sturdy representation of a directory providing read-only access to its
* directory subtree and arbitrary access to the files its subtree contains.
*/
public class EAccessibleDirectory extends EDirectoryBase {
    /**
    * Construct an EAccessibleDirectory given a file and a vat. Only called
    * within the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EAccessibleDirectory(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce an EReadableDirectory representation of this same directory.
    */
    public EReadableDirectory asReadableDirectory() {
        return(new EReadableDirectory(myVat, myFile));
    }

    /**
    * Produce a hashtable describing the contents of this directory.
    * @return A Hashtable mapping names of directories to EAccessibleDirectory
    *   objects and names of files to EEditableFile objects.
    */
    public ReadOnlyHashtable contents() {
        String names[] = myFile.list();
        Hashtable baseResult =
            new Hashtable(names.length == 0 ? 1 : names.length);
        for (int i=0; i<names.length; ++i) {
            File file = new File(myFile, names[i]);
            if (file.isDirectory())
                baseResult.put(names[i], new EAccessibleDirectory(myVat,file));
            else
                baseResult.put(names[i], new EEditableFile(myVat, file));
        }
        return(new ReadOnlyHashtable(baseResult));
    }

    /**
    * Lookup a specific directory pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EAccessibleDirectory corresonding to the path in question.
    */
    public EAccessibleDirectory lookupDirectory(String path)
    throws IOException {
        return(new EAccessibleDirectory(myVat, lookupDirectoryPath(path)));
    }

    /**
    * Lookup a specific file pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EEditableFile corresponding to the path in question.
    */
    public EEditableFile lookupFile(String path) throws IOException {
        return(new EEditableFile(myVat, lookupFilePath(path)));
    }
}
