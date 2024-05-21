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
import ec.e.start.EEnvironment;

import ec.security.Cipher;
import ec.security.CipherInputStream;
import ec.security.CipherOutputStream;

import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;

import ec.util.NestedException;
import ec.security.PassphraseKeyGenerator;
import ec.e.util.DiscreteEnumeration;
import ec.e.serialstate.StateSerializer;
import ec.e.serialstate.StateUnserializer;
import ec.util.NativeSteward;

import ec.e.serialstate.StateOutputStream;
import ec.e.serialstate.StateInputStream;

/**
 * Reliable storage for an object graph.  A restore() must revive the
 * last successful save().  If we crash in the middle of a save(),
 * restore() must revive from the previous one.
 *
 * This class is CREW!  The StateTimeMachine is its STEWARD.  
 */
public class StateStableStore {
    static private final Trace tr = new Trace("ec.e.quake.StateStableStore");

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
    private Cipher myCipher;
    private Key myKey;
    private EEnvironment myEnv;

    /**
     * Returns a StateStableStore for saving and restoring object graphs
     * to/from the designated filename.
     *
     * @param filename The filename which is the "true" name for the
     * checkpoint file.  StateStableStore will derive the other filenames
     * it needs from this one.  If 'filename' doesn't end in ".evat",
     * then ".evat" will be appended to it.
     *
     * @param passphrase nullOk The passphrase to use for encryption
     * and decryption.
     */
    public StateStableStore(EEnvironment env, String filename,
                       String passphrase) {

        myEnv = env;
                 
        if (tr.debug && Trace.ON) {
            tr.debugm("new StateStableStore(" +env+", "+ filename + ", "+ passphrase + ")");
        }
        if (! filename.endsWith(".evat")) {
            filename += ".evat";
        }
        myPlace = new File(new File(filename).getAbsolutePath());
        String dir = myPlace.getParent();
        myTemp  = new File(dir, "#" + myPlace.getName() + "#");
        myTrash = new File(dir, "#" + myPlace.getName() + ".trash#");
        myBak   = new File(dir, myPlace.getName() + ".bak.000");
        setPassphrase(passphrase);
    }
         
    public StateStableStore(String filename, String passphrase)  {
      this(null, filename, passphrase);
    }
         
    public StateStableStore(EEnvironment env, String filename)  {
      this(env, filename, null);
    }
         
    /**
     * Constructor that defaults to no encryption
     */
    public StateStableStore(String filename) {
        this(filename, null);
    }

    /**
     * Is there already a checkpoint at this place?
     * 
     * @param filename file to check for existance
     * @exception IOException thrown if some problem doing this check
     */
    public static boolean exists(String filename) throws IOException {
        return new StateStableStore(filename).exists();
    }

    /**
     * Is there already a checkpoint at this place?
     *
     * @return true if stable store file already exists, or the backup exists
     * @exception IOException thrown if some problem doing check
     */
    public boolean exists() throws IOException {
        return myPlace.exists() || myBak.exists();
    }

    /**
     * Save the entire object graph rooted at root
     *
     * @param root the root of the object graph to encode
     * @exception IOException thrown if some problem writing object graph
     */
    public void save(Object root) throws IOException {
         
      if (tr.debug && Trace.ON) tr.debugm("StateStableStore.save with object "+root);
               
      long start =  NativeSteward.queryTimer();

      StateSerializer ser = StateSerializer.make(getOutputStream());

      ser.writeUTF(HEADER_STRING);
           if (tr.debug && Trace.ON) tr.debugm(" wrote header");
      int numObjects = ser.encodeGraph(root);
           if (tr.debug && Trace.ON) tr.debugm(" wrote "+numObjects+" objects");
               
      ser.writeUTF(FOOTER_STRING);
           if (tr.debug && Trace.ON) tr.debugm(" wrote footer");

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
          double rate = numObjects / secs;
          tr.debugm(numObjects + " objects in " +
                    secs + " seconds = " +
                    rate + " objects saved per second");
      }
    }

    /**
     * Get StateInputStream for this stable store
     *
     * @return StateInputStream for this StateStableStore
     * @exception IOException thrown if some problem getting stream
     */
    private StateInputStream getInputStream() throws IOException {
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
        return new StateInputStream(myEnv, (InputStream) new BufferedInputStream(ins, BUFFER_SIZE));
    }

    /**
     * Get StateOutputStream for this stable store
     *
     * @return StateOutputStream for this StateStableStore
     * @exception IOException thrown if some problem getting stream
     */
    private StateOutputStream getOutputStream() throws IOException {
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

        return new StateOutputStream((OutputStream) new BufferedOutputStream(outs, BUFFER_SIZE));
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
     * Verify a passphrase on this .evat file
     *
     * @param filename the file to check
     * @param passphrase the String passphrase to check
     * @return true if passphrase ok, false otherwise
     */
    // this method is in Dummies.java as well!!!!!!!!!
    public static boolean checkPassphrase(String filename, String passphrase) {
        // can't get away with null OpenerRecipe, as it's used to make the Unserializer.
        try {
            return (new StateStableStore(filename, passphrase)).checkPassphrase();
        }
        catch (IOException e) {
            if (tr.debug && Trace.ON) tr.debugm("problem checking passphrase", e);
            return false;
        }
    }

   /**
    *  Verify a passphrase on this .evat file.  Reads the format line
    *   using the provided decryption key (if any).  If the line
    *   matches, the key is almost certainly good.  If the line does
    *   not match, there may be version skew or an incorrect passphrase
    *   -- we can't tell which.  Other problems could result in an
    *   IOException.
    *
    * @return true if passphrase for this StateStableStore is ok, false otherwise
    * @exception IOException thrown if some problem doing check
    */
    public boolean checkPassphrase() throws IOException {
        StateInputStream ins = getInputStream();
        StateUnserializer uns = StateUnserializer.make(ins);
        String format = uns.readLine();
        if (tr.verbose && Trace.ON) tr.verbosem("checkpassphrase, format line is:" + format);
        uns.close();
        if (! HEADER_STRING.equals(format + "\n")) {
            return false;
        }
        return true;
    }

   /**
    *    Change the passphrase for this .evat file.  Should be followed
    *   immediately by a save() so the current state uses the new key.
    *
    * @param passphrase the String new passphrase
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
 

    /**
     * Copy this StateStableStore to the file represented by the given StateStableStore
     *
     * @param to the StateStableStore to copy ourselves to
     * @exception IOException thrown if some problem copying ourselves
     */
    private void copyTo(StateStableStore to) throws IOException {
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
            StateStableStore to = new StateStableStore(myPlace.getAbsolutePath(), newPassphrase);
            copyTo(to);
        }
        setPassphrase(newPassphrase);
    }

    public static void copy(String fromFile, String fromPassPhrase, String toFile, String toPassPhrase) throws IOException {
        StateStableStore from = new StateStableStore(fromFile, fromPassPhrase);
        StateStableStore to = new StateStableStore(toFile, toPassPhrase);
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

