package ec.e.rep.steward;

import java.util.Vector;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import ec.util.ReadOnlyHashtable;
import ec.util.SortUtilities;

public class RuntimeExtensibleRepository extends Repository {

    /**
      
     * This is an intermediate class that contains functionality used
     * by both PublishRepository and CacheRepository.

     */

    public RuntimeExtensibleRepository(String[] files) throws IOException {
        super(files);
    }

    public File mkfileNamedAfter(File repositoryDir, String nameToFollow) throws IOException {
        String nextName = incrementName(nameToFollow);
        File result = new File(nextName);

        while (result == null || result.exists()) { // Make sure file does not exist already
            nextName = incrementName(nextName);
            result = new File(nextName);
        }

        // Return the File object. It does not exist yet - the File
        // must be opened in order to actually create the file.

        return result;
    }

    /**

     * Open a given Repository directory but open the last file for write.

     */

    public RuntimeExtensibleRepository(File repositoryDir, char prefix) 
         throws IOException {

             // LatestFile is the last file (in alphabetical order) in the directory.
             // We will append to it if it is not too large.
             // Otherwise we leave it alone and open another one
             // with a name that's even higher.

             File latestFile = null;
             String[] dirContents = repositoryDir.list(this);

             if ((dirContents != null) && (dirContents.length > 0)) {
                 SortUtilities.quickSortStringArray(dirContents, 0, dirContents.length - 1, true);
                 latestFile = new File(repositoryDir, dirContents[0]);

                 // Check latestFile size. If it's too large now, open another file.
                 if (latestFile.length() > myFileSizeLimit) {
                     latestFile = mkfileNamedAfter(repositoryDir, dirContents[0]);
                     new RandomAccessFile(latestFile, "rw").close(); // create a file
                 }
             }
             else {
                 if (prefix == 0) {
                     prefix = 'C'; // Prefix- 'W'= WebCache 'P'=Publish. Others?
                 }
                 String latestFileName = prefix + "0000000.REP";
                 latestFile = new File(repositoryDir, latestFileName);
                 new RandomAccessFile(latestFile, "rw").close(); // create a file
             }

             myRepositoryFiles = addRepFilesInOrder(repositoryDir, dirContents, latestFile);
             myFrontMostFile = (RepositoryFile)myRepositoryFiles.elementAt(0);

             // If we only have one file, don't keep the vector around.
                 
             if (myRepositoryFiles.size() <= 1) {
                 myRepositoryFiles = null;
             }
             myKeys = collectAllEntries();
    }
}
