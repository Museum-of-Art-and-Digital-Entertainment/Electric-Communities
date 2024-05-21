package ec.e.quake;

import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

import ec.security.Cipher;
import ec.security.CipherInputStream;
import ec.security.CipherOutputStream;

import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;

import ec.util.NestedException;
import ec.security.PassphraseKeyGenerator;
import ec.e.util.DiscreteEnumeration;
import ec.e.start.Tether;
import ec.e.openers.AwakeAfterRevival;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ClassRecipe;
import ec.e.openers.RootClassRecipe;
import ec.e.openers.FieldKnife;
import ec.e.openers.guest.AllowingClassRecipe;
import ec.e.serial.Serializer;
import ec.e.serial.Unserializer;
import ec.util.NativeSteward;

/**
 * Reliable storage for an object graph.  A restore() must revive the
 * last successful save().  If we crash in the middle of a save(),
 * restore() must revive from the previous one.
 *
 * This class is CREW!  The TimeMachine is its STEWARD.  Originally
 * StableStore was defined as a GUEST, until I tried to write the code
 * to revive from a checkpoint.  Then I realized there wasn't a Vat
 * for the StableStore to live in.  Since StableStore is useful for
 * more than just checkpointing, we should give it a more generic
 * Steward and make TimeMachine a GUEST.  <p>
 */
// XXX was package, needed to be public for CheckPointInspector -emm
public class StableStore {
    static private final Trace tr = new Trace("ec.e.quake.StableStore");

    /*
     * This first line is written both to identify the version of the
     * checkpoint format, and possibly to make the checkpoint file
     * directly launchable under Unix.  In an upcoming version of the
     * format, the last line should be replaced with the number of
     * registered objects.
     */
    static private final String HEADER_STRING = "#!EVat0x0.exe\n";
    static private final String FOOTER_STRING = "That's all folks!\n";
    // XXX only ecb mode is available in jce-1.1
    static private final String CIPHER_MODE   = "DES/ECB/PKCS#5";
    // tune as appropriate
    static private final int BUFFER_SIZE = 16 * 1024;

    private File myPlace;
    private File myTemp;
    private File myTrash;
    private File myBak;
    private OpenerRecipe myMaker;
    private int myNumObjects = 1000;
    private Cipher myCipher;
    private Key myKey;


    /**
     * Returns a StableStore for saving and restoring object graphs
     * to/from the designated filename.
     *
     * @param filename The filename which is the "true" name for the
     * checkpoint file.  StableStore will derive the other filenames
     * it needs from this one.  If 'filename' doesn't end in ".evat",
     * then ".evat" will be appended to it.
     *
     * @param maker Provides the StableStore with the ability to "peer
     * into" objects, and a "point-of-view" about their contents (eg,
     * do transient instance variables exist?  Should a Hashtable be
     * seen as consisting only of corresponding key & value objects, to
     * be rehashed on decoding?)
     *
     * @param passphrase nullOk The passphrase to use for encryption
     * and decryption.
     */
    public StableStore(String filename,
                       OpenerRecipe maker,
                       String passphrase) {

        if (tr.debug && Trace.ON) {
            tr.debugm("new StableStore(" + filename + ", " +
                      maker + ", " + passphrase + ")");
        }
        if (! filename.endsWith(".evat")) {
            filename += ".evat";
        }
        myPlace = new File(new File(filename).getAbsolutePath());
        String dir = myPlace.getParent();
        myTemp  = new File(dir, "#" + myPlace.getName() + "#");
        myTrash = new File(dir, "#" + myPlace.getName() + ".trash#");
        myBak   = new File(dir, myPlace.getName() + ".bak.000");
        myMaker = maker;
        setPassphrase(passphrase);
    }

    /**
       Defaults to no encryption.
    */
    public StableStore(String filename, OpenerRecipe maker) {
        this(filename, maker, null);
    }

    /**
     * Defaults to an OpenerRecipe suitable for persistence, and no encryption.
     */
    public StableStore(String filename) {
        this(filename, defaultRecipe(), null);
    }

    /**
     * Defaults to an OpenerRecipe suitable for persistence.
     */
    public StableStore(String filename, String passphrase) {
        this(filename, defaultRecipe(), passphrase);
    }

    /**
     * An OpenerRecipe suitable for persistence
     */
    static private OpenerRecipe defaultRecipe() {
        ClassRecipe classRecipe
            = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                       "ec.e.quake.Checkpointable");

        return new OpenerRecipe(Checkpointable.DECODER_MAKERS,
                                Checkpointable.ENCODER_MAKERS,
                                classRecipe);
    }

    /**
     * Is there already a checkpoint at this place?
     */
    // this method is in Dummies.java as well!!!!!!!!!
    public static boolean exists(String filename) throws IOException {
        StableStore ss = new StableStore(filename, null, null);
        return ss.exists();
    }

    /**
     * Is there already a checkpoint at this place?
     */
    public boolean exists() throws IOException {
        return myPlace.exists() || myBak.exists();
    }


    /**
     * Save the object graph rooted in 'root' to the checkpoint.
     * Don't return until it is reliably committed.  If there is
     * already a valid checkpoint there, all failure conditions that
     * preserve the following properties should leave a valid
     * checkpoint behind (either the original or the new one). <p>
     *
     * XXX Assumptions made in the current implementation:
     * <pre>
     *     1) flush() or close() reliably commits to disk.
     *     2) renameTo() is atomic and reliable, and so doesn't copy
     *     3) no one else is concurrently using these files
     *     4) closes & renames happen in the order issued
     * </pre>
     *
     * Properties #1 may already be implemented by java.io, we need to
     * find out.  Property #3 we know to not be assured by any
     * software, and doing so in a portable fashion looks hard, even
     * among cooperative parties. <p>
     *
     * I expect #4 is given by most operating systems as well as
     * Microsoft Windows. <p>
     *
     * We used to say: <p>
     *
     * "The code assume that a rename to an existing filename will
     * wipe out the destination file as part of the one atomic
     * action.  Under no conditions will it wipe out the destination
     * but not rename the source." <p>
     *
     * But this isn't the case for Windows95 (or presumably, NT).
     * Instead, a rename on those systems fails if the destination
     * filename is already occupied.  Arguably, this is a better
     * semantics, but Java should have decided on one semantics to
     * implement on both platforms.  For this code to work on both
     * systems, it is hereby changed to ensure that the destination
     * name is unoccupied, rather than count on either an error or a
     * lack of error otherwise.
     */
    public void save(Object root) throws IOException {
        long start =  NativeSteward.queryTimer();

        /* This used to do the following but that triggered a bug in some JITs
           and the above call adds only microseconds to a several second process.

        long start = 0;
        if (tr.debug && Trace.ON) {
            start = NativeSteward.queryTimer();
        }
        */

        OutputStream outs = getOutputStream();

        //Pass it myNumObjects as a predictor of how many
        //objects there'll be this time.
        Serializer ser = Serializer.make(outs, myMaker, myNumObjects);

        ser.writeBytes(HEADER_STRING);
        ser.encodeGraph(root);
        myNumObjects = ser.numObjects();
        ser.writeInt(myNumObjects);
        ser.writeBytes(FOOTER_STRING);

        ser.flush(); //can we instead assume that close() flush()es?
        ser.close();

        moveToMyPlace();
        /*
         * The file known as myTemp is now named myPlace.  We will
         * restore from it, so we are now committed and may return
         * normally.
         */

        if (tr.debug && Trace.ON) {
            long stop = NativeSteward.queryTimer();
            double secs = (stop - start) / 1000000.0;
            double rate = myNumObjects / secs;
            tr.debugm(myNumObjects + " objects in " +
                      secs + " seconds = " +
                      rate + " objects saved per second");
            }
    }

    private InputStream getInputStream() throws IOException {
        File lastGood = myPlace;
        if (! lastGood.exists()) {
            /*
             * If myBak doesn't exist either, we'll fail trying to
             * open it.
             */
            lastGood = myBak;
        }
        InputStream ins = new FileInputStream(lastGood);

        if (myCipher != null && myKey != null) {
            try {
                myCipher.initDecrypt(myKey);
                ins = new CipherInputStream(ins, myCipher);

            } catch (KeyException e) {
                throw new NestedException
                    ("can't init cipher to decrypt checkpoint", e);
            }
        }

        //ins = new InflaterInputStream(ins);
        ins = new BufferedInputStream(ins, BUFFER_SIZE);
        return ins;
    }


    private OutputStream getOutputStream() throws IOException {
        //MSM: verified that this truncates if temp exists
        OutputStream outs = new FileOutputStream(myTemp);

        if (myCipher != null && myKey != null) {
            try {
                myCipher.initEncrypt(myKey);
                outs = new CipherOutputStream(outs, myCipher);

            } catch (KeyException e) {
                throw new NestedException
                    ("can't init cipher to encrypt checkpoint", e);
            }
        }

        //outs = new DeflaterOutputStream(outs);
        outs = new BufferedOutputStream(outs, BUFFER_SIZE);
        return outs;
    }

    private void moveToMyPlace() throws IOException {
        /*
         * since we arrived here, we must have successfully written
         * out root to myTemp.  Now we must rename this to myPlace,
         * but without losing what was the most recent valid
         * checkpoint if we crash.  To succeed at this under Windows,
         * we first rename myPlace to myBak.  To succeed at this, we
         * first delete myBak.
         */
        if (myPlace.exists()) {
            if (! myBak.delete()) {
                /*
                 * We don't care if this fails.
                 */
                tr.debugm("Could not delete backup file " + myBak.getName());
            }
            if (! myPlace.renameTo(myBak)) {
                /*
                 * The purpose of this rename is to safely enable the
                 * following rename to succeed, on platforms (like
                 * Windows95) which can't rename to an occupied file name.
                 * We don't care if this fails, as the following rename
                 * will either succeed or fail, and we're safe either
                 * way.
                 */
                tr.debugm("Could not rename to backup file " + myPlace.getName() + " to " + myBak.getName());
            } else {
                /*
                 * The file known as myPlace is now named myBak.  If we crash
                 * here, we will restore from it, so we're still safe.
                 */
            }
        }
        if (! myTemp.renameTo(myPlace)) {
            /*
             * XXX What the hell shape are we guaranteed to be in if
             * renameTo returns false?  I think we're actually ok: the
             * original myPlace should be unaffected.
             */
            throw new IOException("couldn't rename " + myTemp
                                  + " to " + myPlace);
        }
    }


    /**
     * Read in the last successfully saved checkpoint, and return the
     * resulting object graph.
     */
    public Object restore() throws IOException {
        long start = 0;
        if (tr.debug && Trace.ON) {
            start = NativeSteward.queryTimer();
        }

        InputStream ins = getInputStream();

        Unserializer uns = Unserializer.make(ins, myMaker, myNumObjects);
        Object result = null;
        try {
            String format = uns.readLine();
            if (! HEADER_STRING.equals(format + "\n")) {
                throw new IOException("Unrecognized format " + format);
            }
            result = uns.decodeGraph();

            // XXX should read numObjects and verify the footer before
            // reading the object graph, not afterwards.  This would
            // let us tell the Unserializer to preallocate a Vector of
            // the right size.
            myNumObjects = uns.readInt();
            String footer = uns.readLine();
            if (! FOOTER_STRING.equals(footer + "\n")) {
                throw new IOException("Unrecognized footer " + footer);
            }

            // Let all interesed objects awakeAfterRevival().  Do this
            // in reverse order so that acyclic relationships can
            // become valid in bottom to top order.  I.e., if you know
            // you're not in a cycle, then by the time you're woken up
            // everything you can talk to is already awake.
            Vector objects = uns.done();

            if (myNumObjects != objects.size()) {
                throw new IOException("myNumObjects confused " +
                                      myNumObjects + " vs. " + objects.size());
            }
            for (int i = myNumObjects-1; i >= 0; i--) {
                Object obj = objects.elementAt(i);
                if (obj != null && (obj instanceof AwakeAfterRevival)) {
                    ((AwakeAfterRevival)obj).awakeAfterRevival();
                }
            }
        } finally {
            uns.close();
        }

        if (tr.debug && Trace.ON) {
            long stop = NativeSteward.queryTimer();
            double secs = (stop - start) / 1000000.0;
            double rate = myNumObjects / secs;
            tr.debugm(myNumObjects + " objects in " +
                      secs + " seconds = " +
                      rate + " objects revived per second");
        }
        return result;
    }

    /**
       Verify a passphrase on this .evat file.
    */
    // this method is in Dummies.java as well!!!!!!!!!
    public static boolean checkPassphrase(String filename, String passphrase) {
        // can't get away with null OpenerRecipe, as it's used to make the Unserializer.
        StableStore ss = new StableStore(filename, defaultRecipe(), passphrase);
        try {
            return ss.checkPassphrase();
        }
        catch (IOException e) {
            if (tr.debug && Trace.ON) tr.debugm("problem checking passphrase", e);
            return false;
        }
    }

    /**
       Verify a passphrase on this .evat file.  Reads the format line
       using the provided decryption key (if any).  If the line
       matches, the key is almost certainly good.  If the line does
       not match, there may be version skew or an incorrect passphrase
       -- we can't tell which.  Other problems could result in an
       IOException.
    */
    public boolean checkPassphrase() throws IOException {
        InputStream ins = getInputStream();
        Unserializer uns = Unserializer.make(ins, myMaker);
        String format = uns.readLine();
        if (tr.verbose && Trace.ON) tr.verbosem("checkpassphrase, format line is:" + format);
        uns.close();
        if (! HEADER_STRING.equals(format + "\n")) {
            return false;
        }
        return true;
    }

    /**
       Change the passphrase for this .evat file.  Should be followed
       immediately by a save() so the current state uses the new key.
    */
    public void setPassphrase(String passphrase) {
        if (passphrase == null || passphrase.length() <= 0) {
            myCipher = null;
            myKey = null;
        }
        else {
            try {
                myCipher = Cipher.getInstance(CIPHER_MODE);
                myKey = PassphraseKeyGenerator.generateKey(passphrase,
                                                           CIPHER_MODE);
            }
            catch (NoSuchAlgorithmException e) {
                throw new NestedException("can't load encryption algorythm " +
                                          CIPHER_MODE, e);
            }
        }
    }

    private void copyTo(StableStore to) throws IOException {
        InputStream ins = getInputStream();
        OutputStream outs = to.getOutputStream();
        byte buf[] = new byte[BUFFER_SIZE];
        int len;

        while ((len=ins.read(buf, 0, BUFFER_SIZE)) > 0) {
            outs.write(buf, 0, len);
        }
        outs.flush();
        outs.close();
        ins.close();
        to.moveToMyPlace();
    }

    public void rewriteWithNewPassphrase(String newPassphrase) throws IOException {
        if (myPlace.exists()) {
            StableStore to = new StableStore(myPlace.getAbsolutePath(), newPassphrase);
            copyTo(to);
        }
        setPassphrase(newPassphrase);
    }

    public static void copy(String fromFile, String fromPassPhrase, String toFile, String toPassPhrase) throws IOException {
        StableStore from = new StableStore(fromFile, null, fromPassPhrase);
        StableStore to = new StableStore(toFile, null, toPassPhrase);
        from.copyTo(to);
    }

    /**
     * Cause there to no longer be a valid checkpoint.  After this
     * operation completes, restore()s will fail unless there is
     * another save().
     */
    public void delete() throws IOException {
        if (! myPlace.renameTo(myTrash)) {
            throw new IOException("It's too persistent " + myPlace);
        }
    }
}

