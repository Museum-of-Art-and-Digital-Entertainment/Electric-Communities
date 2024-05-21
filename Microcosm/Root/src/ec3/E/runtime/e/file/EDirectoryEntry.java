package ec.e.file;

import java.io.File;
import java.io.IOException;
import ec.e.start.Vat;

/**
 * Common interface for all things located in directories, namely directories
 * and files.
 */
abstract public class EDirectoryEntry {
    Vat myVat;
    File myFile;

    /**
     * Base constructor for all sturdy file objects.
     */
    EDirectoryEntry(Vat vat, File file) {
        myVat = vat;
        myFile = file;
    }

    /**
     * Test if the file or directory represented by this entry actually exists.
     */
    public boolean exists() {
        return myFile.exists();
    }

    /**
     * Test if this entry is in a particular directory.
     */
    public boolean inDirectory(EDirectoryBase dir) {
        File testFile = new File(dir.myFile, myFile.getName());
        return testFile.equals(myFile);
    }

    /**
     * Test if this entry describes a directory.
     *
     * @returns true=>directory, false=>file
     */
    abstract public boolean isDirectory();

    /**
     * Delete the file or directory represented by this entry.
     */
    void deleteSelf() throws IOException {
        if (!myFile.delete())
            throw new IOException("deleteSelf: delete failed");
    }

    /**
     * Move the file or directory represented by this entry to another
     * location.
     */
    void moveSelf(File newFile) throws IOException {
        if (!myFile.renameTo(newFile))
            throw new IOException("moveSelf: rename failed");
    }
}
