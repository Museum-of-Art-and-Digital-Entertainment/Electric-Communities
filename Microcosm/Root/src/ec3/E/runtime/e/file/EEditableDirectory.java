package ec.e.file;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import ec.e.start.Vat;
import ec.util.ReadOnlyHashtable;

/**
* A sturdy representation of a directory providing arbitrary access to its
* contents.
*/
public class EEditableDirectory extends EDirectoryBase {
    /**
    * Construct an EEditableDirectory given a file and a vat. Only called
    * within the package.
    *
    * @param vat The vat we are running in.
    * @param file The file we represent.
    */
    EEditableDirectory(Vat vat, File file) {
        super(vat, file);
    }

    /**
    * Produce an EAccessibleDirectory representation of this same directory.
    */
    public EAccessibleDirectory asAccessibleDirectory() {
        return(new EAccessibleDirectory(myVat, myFile));
    }

    /**
    * Produce an EReadableDirectory representation of this same directory.
    */
    public EReadableDirectory asReadableDirectory() {
        return(new EReadableDirectory(myVat, myFile));
    }

    /**
    * Move a file or directory into this directory. Can also be used to rename
    * a file or directory that is already in this directory.
    *
    * @param sourceDirectory An editable directory where the indicated
    *   directory entry is currently to be found. If this is 'this', the
    *   contain operation becomes a rename operation.
    * @param entry An EDirectory entry describing the file or directory that
    *   is to be moved into this directory.
    * @param name The name under which this entry should be known here.
    */
    public void contain(EEditableDirectory sourceDirectory,
                        EDirectoryEntry entry, String name) throws IOException{
        if (!entry.inDirectory(sourceDirectory))
            throw new IOException("contain: " + name + " is not in source directory");
        File newFile = new File(myFile, name);
        entry.moveSelf(newFile);
    }

    /**
    * Produce a hashtable describing the contents of this directory.
    * @return A Hashtable mapping names of directories to EEditableDirectory
    *   objects and names of files to EEditableFile objects.
    */
    public ReadOnlyHashtable contents() {
        String names[] = myFile.list();
        Hashtable baseResult =
            new Hashtable(names.length == 0 ? 1 : names.length);
        for (int i=0; i<names.length; ++i) {
            File file = new File(myFile, names[i]);
            if (file.isDirectory())
                baseResult.put(names[i], new EEditableDirectory(myVat, file));
            else
                baseResult.put(names[i], new EEditableFile(myVat, file));
        }
        return(new ReadOnlyHashtable(baseResult));
    }

    /**
    * Remove a file or directory from this directory.
    *
    * @param entry An EDirectoryEntry describing the entry to be removed.
    */
    public void delete(EDirectoryEntry entry) throws IOException {
        if (entry.inDirectory(this))
            entry.deleteSelf();
        else
            throw new IOException("delete: not in directory");
    }

    /**
    * Lookup a specific directory pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EEditableDirectory corresonding to the path in question.
    */
    public EEditableDirectory lookupDirectory(String path) throws IOException {
        return(new EEditableDirectory(myVat, lookupDirectoryPath(path)));
    }

    /**
    * Lookup a specific file pathname relative to this directory.
    *
    * @param path A pathname (in local format) for the file desired.
    * @return An EEditableFile corresonding to the path in question.
    */
    public EEditableFile lookupFile(String path) throws IOException {
        return(new EEditableFile(myVat, lookupFilePath(path)));
    }

    /**
    * Create a new subdirectory under this directory.
    *
    * @param name The name for the new subdirectory.
    * @return An editable directory object describing the new directory.
    */
    public EEditableDirectory mkdir(String name) throws IOException {
        if (name.indexOf(File.separator) != -1 || name.equals(".."))
            throw new IOException("mkdir: improper name '" + name + "'");
        File newFile = new File(myFile, name);
        if (!newFile.mkdir())
            throw new IOException("mkdir: mkdir of " + name + " failed");
        return(new EEditableDirectory(myVat, newFile));
    }

    /**
    * Create a new (empty) file in this directory. Actually, the file is not
    * really created until it is opened; this method merely creates the
    * possibility that the file might exist.
    *
    * @param name The name for the new file.
    * @return An editable file object describing the new file.
    */
    public EEditableFile mkfile(String name) throws IOException {
        if (name.indexOf(File.separator) != -1 || name.equals(".."))
            throw new IOException("mkfile: improper name '" + name + "'");
        File newFile = new File(myFile, name);
        return(new EEditableFile(myVat, newFile));
    }
}
