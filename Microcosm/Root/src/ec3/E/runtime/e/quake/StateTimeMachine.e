package ec.e.quake;

import ec.e.file.EStdio;
import ec.util.NestedException;
import ec.e.run.OnceOnlyException;
import ec.e.run.Vat;
import ec.e.run.EEnvironment;
import ec.e.run.TimeQuake;
import ec.e.run.Seismologist;
import java.io.IOException;

/**
 * Version of TimeMachine for State Bundles.  This time machine
 * creates a StateSerializer for serializing the state bundles
 * for a given vat.
 */
public eclass StateTimeMachine extends TimeMachine {
 
    /**
     * The StateStableStore instance.
     */
    private StateStableStore myStateStableStore;
    // Reference to EEnvironment object.  Used to get at 
    // superbundle for state bundles serialization
    private EEnvironment myEnv;
         
    /**
     * Asks the EEnviroment to summon a StateTimeMachineMaker, and returns
     * the TimeMachine it conjures up (an instance of this class).
     */
    static public TimeMachine summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException
    {
        return (TimeMachine)eEnv.magicPower
            ("ec.e.quake.StateTimeMachineMaker");
    }
  
    /**
     * Called once per vat by the StateTimeMachineMaker.
     */
    public StateTimeMachine(EEnvironment eEnv)
         throws OnceOnlyException, IOException {
        super(eEnv);
        myEnv = eEnv;
        String filename = eEnv.getProperty("checkpoint");
        if (filename == null) {
            //this vat is ephemeral
            return;
        }

        myStateStableStore = new StateStableStore(myEnv, filename);
        if (myStateStableStore.exists()) {
            throw new OnceOnlyException
                ("cannot overwrite a checkpoint " + filename);
        }
    }

    /**
       Set the passphrase associated with this vat.  Once set, this
       passphrase will have to be provided in order to revive the vat.

       @param passphrase nullOK the passphrase to use, or null for no
       encryption.
    */
    emethod setPassphrase(String passphrase) {
        try {
            myStateStableStore.rewriteWithNewPassphrase(passphrase);
        }
        catch (IOException e) {
            ethrow new NestedException("Can't set passphrase", e);
        }
    }

    /**
     * Asks the TimeMachine to commit the current state of the vat.
     * Once the Seismologist is told to notice a commit, the vat is
     * guaranteed to not regress to a state earlier than that from
     * which the commit was requested.  An ephemeral (non-persistent)
     * vat cannot regress, it can only die.  Therefore, it commits
     * trivially. <p>
     *
     * XXX Currently does a checkpoint synchronously when the commit
     * request is received. <p>
     *
     * XXX This could be made smarter in order to gather commit
     * requests and satisfy them together.  This may only make sense
     * when doing background commits--while waiting for one to
     * complete, others should simply accumulate. <p>
     */
    emethod commit(Seismologist waiter) {
        try {
            internalCommit(waiter);
        } catch(IOException ex) {
            ethrow new NonCommittal("Won't commit " + ex);
        }
    }

    private void internalCommit(Seismologist waiter) throws IOException {
        if (myStateStableStore != null) {
            /*
             * The saved checkpoint contains no Java stack frames, so
             * the restored computation will not perform the remainder
             * of this method after the save.  Therefore, we store the
             * waiter away around the save, so Vat.revive() can
             * proceed to tell the waiter that the commit succeeded.
             */
            setWaiter(waiter);
            myStateStableStore.save(myEnv.getSuperBundle());
            setWaiter(null);
        }
        if (waiter != null) {
            waiter <- noticeCommit();
        }
    }

    /**
     * The persistent process is stopped in its tracks, presumably to
     * be revived again later, and the process incarnation exits with
     * the supplied exitCode. <p>
     */
    emethod hibernate(Seismologist waiter, int exitCode) {
        try {
            if (myStateStableStore == null) {
                ethrow new NonCommittal("This vat is ephemeral");
                return;
            }
            internalCommit(waiter);
            System.exit(exitCode);

        } catch(IOException ex) {
            ethrow new NonCommittal("Too scared to sleep " + ex);
        }
    }

    /**
     * Exits the persistent process as well as the process incarnation.
     */
    emethod suicide(int exitCode) {
        try {
            if (myStateStableStore != null) {
                myStateStableStore.delete();
            }
            System.exit(exitCode);

        } catch(IOException ex) {
            ethrow new NonCommittal
                ("Can't stop myself from coming back " + ex);
        }
    }
     
}
