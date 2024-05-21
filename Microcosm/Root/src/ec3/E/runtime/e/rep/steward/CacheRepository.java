package ec.e.rep.steward;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;

import ec.e.openers.*;
import ec.e.serial.*;

import ec.cert.CryptoHash;

import ec.util.ReadOnlyHashtable;
import ec.util.SortUtilities;

/* REPTIMESTART */
import ec.util.NativeSteward;
/* REPTIMEEND */

public class CacheRepository extends RuntimeExtensibleRepository {

    // Each entry in this table is an old file.
    // About half of our files are old files.
    // Whenever we read an object from an old file
    // we copy it to the frontmost file.

    // This way we can eventually delete the oldest file(s) and not lose
    // much that we'd like to keep.

    private Hashtable oldRepositoryFiles = null;

    public static CacheRepository makeCacheRepository(File repositoryDir, long maxBytes)
         throws IOException {

             // pruning method computes the oldRepositoryFiles table
             // as a bonus.

             Hashtable oldRepositoryFiles = pruneToSize(repositoryDir, maxBytes);
             return new CacheRepository(repositoryDir, oldRepositoryFiles);
    }

    /**

     * Open a given Repository directory as usual but open one file
     * for write. This is either the file with the highest name (in
     * alphabetical order) or a new file if the previous file is too
     * large to be appended to. The writable file becomes the
     * "firstFile" for this Repository.

     */

    private  CacheRepository(File repositoryDir, Hashtable oldFiles) throws IOException {
             super(repositoryDir,'W'); // W means this stores objects from the Web etc.
             oldRepositoryFiles = oldFiles;
    }

    /**

     *  Wrap RepositoryHandle.getObject calls so that we can update
     *  our LRU info as needed - Anytime we fetch an object, that
     *  object becomes less likely to fall out of the cache.

     */

    public Object getObject(RepositoryHandle handle, Object key, Hashtable parimeterArguments) 
         throws IOException {
             if (Trace.repository.debug && Trace.ON)
                 Trace.repository.debugm("Requesting " + key + " from Cache Repository");

             if (oldRepositoryFiles.get(handle.myRepositoryFile) == null) {
                 if (Trace.repository.debug && Trace.ON)
                     Trace.repository.debugm("No LRU info update needed");
                 return handle.getObject(key, parimeterArguments); // Not an old file.
             }

             /* REPTIMESTART */
             long startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */

             byte[] bytes = handle.getDataBytes(key);

             /* REPTIMESTART */
             Repository.diskTimer += NativeSteward.deltaTimerUSec(startTime);
             startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */

             if (Trace.repository.debug && Trace.ON)
                 Trace.repository.debugm("Updating CacheRepository LRU info");

             CryptoHash newKey = putHashBytes(bytes, null); // Save bytes again in frontmost file.

             // Make a gratuituous check.

             if (! newKey.equals(key)) {
                 // If this ever happens then we ar in deep trouble.
                 throw new Error("Refresh of Cache repository yielded different key");
             }

             /* REPTIMESTART */
             startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */

             // Decode here, since we already have the bytes,
             // rather than calling handle.getObject() again.

             ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             Object result = (ParamUnserializer.make
                              (byteStream,myMaker,parimeterArguments)).decodeGraph();

             /* REPTIMESTART */
             long deltaTime = NativeSteward.deltaTimerUSec(startTime);
             Repository.unsTimer += deltaTime;
             Repository.gatherDecodeStatistics(result,deltaTime);
             /* REPTIMEEND */

             return result;
    }

    /**

     * Using the sizes of all files in this dir, determine how many of
     * the oldest files that need to be deleted to get under the given
     * limit, then delete them.

     */

    public static Hashtable pruneToSize(File repositoryDir, long maxBytes) throws IOException {
        Hashtable oldRepositoryFiles = new Hashtable(30);
        String[] dirContents = repositoryDir.list(new Repository());

        if ((dirContents != null) && (dirContents.length > 0)) {
            SortUtilities.quickSortStringArray(dirContents, 0, dirContents.length - 1, true);

            int nrElements = dirContents.length;

            if (Trace.repository.debug && Trace.ON) {
                Trace.repository.debugm("Number of Cache files found initially: " + nrElements);
            }

            // dirContents is now a (backwards) sorted array of file names.

            int lastFileNumber = 0;
            long totalSoFar = 0;

            for (int i = 0; i < nrElements; i++) {
                File candidate = new File(dirContents[i]);
                totalSoFar += candidate.length();

                if (totalSoFar > maxBytes) {
                    if (Trace.repository.debug && Trace.ON) {
                        Trace.repository.debugm("Deleting old Cache file " + candidate);
                    }
                    if (candidate.canWrite()) {
                        candidate.delete();
                    }
                    else {
                        throw new IOException
                            ("Cache repository not writable - Cannot prune Cache file " +
                             candidate);
                    }
                }
                else {
                    lastFileNumber = i;
                }
            }

            int firstOldFile = lastFileNumber / 2;

            if (lastFileNumber > firstOldFile) {
                if (Trace.repository.debug && Trace.ON) {
                    Trace.repository.debugm
                        ("Cache files with numbers" + firstOldFile + " - " +
                         lastFileNumber + " will be copied forward when accessed");
                }
            }

            for (int j = firstOldFile; j < lastFileNumber; j++) {
                oldRepositoryFiles.put(dirContents[j],
                                       dirContents[j]); // Use data as value too.
                ec.e.inspect.Inspector.gather(oldRepositoryFiles,
                                              "Repository",
                                              "Old cache files");
            }
        }
                 
        return oldRepositoryFiles;
    }
}
