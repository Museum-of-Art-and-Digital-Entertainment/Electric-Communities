package ec.e.rep;

import ec.e.openers.AllowingClassRecipe;
import ec.e.openers.*;
import ec.e.rep.RepositoryKeyNotFoundException;
import ec.e.serial.Serializer;
import ec.e.serial.Unserializer;
import ec.e.serial.ParamSerializer;
import ec.e.serial.ParamUnserializer;
import ec.e.util.DiscreteEnumeration;
import ec.tables.SimTable;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import ec.e.hold.DataHolderRecipe;
import ec.e.hold.ReposableMarker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/* REPTIMESTART */
import ec.util.NativeSteward;
/* REPTIMEEND */

    /**

     * RepositoryHandle instances are used in repository key
     * SimTables. They contain the RepositoryFile object associated
     * with the file that contains the data, and the file position in
     * that file. <p>

     */

class RepositoryHandle {

    private static OpenerRecipe myMaker = defaultRecipe();
    long myDataPos;             // Start of my Data
    int myDataLength;           // Length of my Data
    RepositoryFile myRepositoryFile; // File where the data is
    private static final int VOIDED_RECORD = -1; // Pseudo file position for deleted records.

    RepositoryHandle() {
        myDataPos = -1;
        myDataLength = 0;
        myRepositoryFile =null;
    }

    /**

     * Constructor. Only used by RepositoryFile.put()

     */

    // XXX Make this a factory and hand one to RepositoryFile

    RepositoryHandle(RepositoryFile repositoryFile, long filePos, int dataLength) {
        myDataPos = filePos;
        myDataLength = dataLength;
        myRepositoryFile = repositoryFile;
    }

    static private OpenerRecipe defaultRecipe() {
        ClassRecipe classRecipe
            = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                       "ec.e.hold.ReposableMarker");

        return new OpenerRecipe(ReposableMarker.DECODER_MAKERS,
                                ReposableMarker.ENCODER_MAKERS,
                                classRecipe);
    }

    /**

     * Returns the data in this record as a byte array.
     * This is a package-scope help method.

     */

    byte[] getDataBytes(Object key)
         throws IOException, RepositoryKeyNotFoundException {
             return myRepositoryFile.getDataBytes(key, myDataPos, myDataLength);
    }

    /**

     * Get the size of the object (in the encoded form) that this
     * RepositoryHandle object stands for.

     */

    public int size() {
        return myDataLength;
    }

    /**

     * After reading in the key table, update repository file in all
     * entries to reference the current file instance.

     */

    void updateFile(RepositoryFile newFile) {
        myRepositoryFile = newFile;
    }

    /**

     * Delete the object that this RepositoryHandle object stands for
     * We are given the length of the key as an argument.  We already
     * know the offset in the file to the start of data.  We can now
     * compute the start of the record and delete the record in the
     * file based on the file position of the start of the record.

     */

    public void delete(int keyLength) throws IOException, RepositoryKeyNotFoundException {
        myRepositoryFile.deleteByFilePosition
          (myDataPos - keyLength - (2 * RepositoryFile.SIZEOF_INT));
        myDataPos = VOIDED_RECORD; // Void this record (Shouldn't be necessary)
    }

    /**

     * Retrieve the data as a byte array and create an object by decoding this.
     * Also note that the key is only there to improve error messages.

     * <b>NOTE</b> This code is currently heavily instrumented to
     * attempt to catch the <b>Gray RepositoryBlob</b> in the
     * act. This extra code slows us down some so this code should be
     * cleaned up before flight. When doing so, update this
     * documentation.<p>

     * @param parimeters nullOK untrusted - A Hashtable. Note that all
     * put() and encode() methods use PEHashtables, but all get(0
     * methods use regular Hashtables since this is the semantics we
     * want.

     */

    public Object getObject(Object key, Hashtable parimeters) 
         throws IOException, RepositoryKeyNotFoundException {
             byte[] bytes = null;
             Object result = null;

             try {              // getDataBytes does its own timing of disk read time.
                 bytes = myRepositoryFile.getDataBytes(key, myDataPos, myDataLength);
             } catch (Throwable t) {

                 // The item read errored? Try again, just for fun.
                 // If it throws a second time, fine.
                 // If it works, complain very loudly about nondeterminism.

                 bytes = myRepositoryFile.getDataBytes(key, myDataPos, myDataLength);
                 if (bytes != null) {
                     t.printStackTrace();
                     String msg = "GRAY BLOB TRAP: Repository is nondeterministic" +
                        " (1) in RepositoryHandle.getObject()" +
                        " - First exception was " + t +
                       " with the message " + t.getMessage() +
                       " and key was " + key;
                     System.out.println(msg);
                     throw new Error(msg);
                 }
                 throw (IOException)t; // Re-throw the exception if we ever get here.
             }

             if (bytes == null) {

                 // The item read returned null? Try again, just for fun.
                 // If it throws this time, fine.
                 // If it works, complain very loudly about nondeterminism.

                 bytes = myRepositoryFile.getDataBytes(key, myDataPos, myDataLength);
                 if (bytes != null) {
                     String msg = "GRAY BLOB TRAP: Repository is nondeterministic" +
                       " (2) in RepositoryHandle.getObject(), key was " + key;
                     System.out.println(msg);
                     IOException iox = new IOException(msg);
                     iox.printStackTrace();
                 }
                 else throw new IOException("getObject() for key " + key +
                                            " returned null without throwing an error!");
             }

             /* REPTIMESTART */
             long startTime = NativeSteward.queryTimer();
             /* REPTIMEEND */

             // lay another trap for nondeterminism in the decode phase.
             // XXX If this never triggers, then this trap can also be removed

             try {
                 ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                 result = (ParamUnserializer.make
                                  (byteStream,myMaker,parimeters)).decodeGraph();
             } catch (Throwable t) {

                 // Attempt to do it over, including the read.
                 // if it works this time, complain about nondeterminism

                 bytes = myRepositoryFile.getDataBytes(key, myDataPos, myDataLength);
                 ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                 result = (ParamUnserializer.make
                           (byteStream,myMaker,parimeters)).decodeGraph();

                 if (result != null) { // If second decode succeeded
                     t.printStackTrace();
                     String msg = "GRAY BLOB TRAP: Repository is nondeterministic" +
                        " (3) in RepositoryHandle.getObject()" +
                        " - First exception was " + t +
                       " with the message " + t.getMessage() +
                       " and key was " + key;
                     System.out.println(msg);
                     throw new Error(msg);
                 }
                 throw (IOException)t; // Re-throw the exception if we ever get here.
             }

             /* REPTIMESTART */
             long deltaTime = NativeSteward.deltaTimerUSec(startTime);
             Repository.unsTimer += deltaTime;
             Repository.gatherDecodeStatistics(result,deltaTime);
             /* REPTIMEEND */

             return result;
    }
}

/**

 * Small subclass to handle tricky data such as %SymbolTable%. They
 * are created and placed as values in the key Dictionary of a
 * Repository at initilization of that Repository so that when someone
 * asks the Repository for %SymbolTable% (etc) then the data is
 * immediately returned without accessing the disk and/or recomputing
 * it. If this is useful, then we could add API to create these from
 * the user level, to use for temporary objects that need to be stored
 * under a key without writing them to a Repository file.

 */

class RepositoryDirectDataHandle extends RepositoryHandle {

    private Object myData;

    public RepositoryDirectDataHandle() {
        myData = null;
    }

    public RepositoryDirectDataHandle(Object data) {
        myData = data;
    }

    public Object getObject(Object key, Hashtable parimeters) 
         throws IOException, RepositoryKeyNotFoundException {
             return myData;
    }

    public void delete(int keyLength) {
        myData = null;
    }
}
