package ec.e.quake;

import java.io.IOException;

import ec.e.file.EStdio;
import ec.util.NestedException;
import ec.e.run.OnceOnlyException;
import ec.e.run.Vat;
import ec.e.run.Tether;
import ec.e.run.SettableTether;
import ec.e.run.EEnvironment;
import ec.e.run.TimeQuake;
import ec.e.run.Seismologist;


/**
 * The TimeMachine is a magic power, made by the TimeMachineMaker.
 * It is a sturdy steward wrapping a StableStore crew object.  Since
 * it is also an EObject, it knows its emethods can only be invoked
 * between other e-events, and so can be considered brace points.  The
 * TimeMachine's sturdiness derives from the sturdiness of the Tether
 * it uses to hold onto the StableStore, and this sturdiness is by
 * special arrangement between Revive, Vat, and EEnvironment.
 */
public eclass TimeMachine {
    private Vat myVat;
    private SettableTether myCheckpointTether;

    /**
     * Asks the EEnviroment to summon a TimeMachineMaker, and returns
     * the TimeMachine it conjures up.
     */
    static public TimeMachine summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException
    {
        String TimeMachineMakerClass = eEnv.getProperty("TimeMachineMakerClass");
        if (TimeMachineMakerClass == null) {
            TimeMachineMakerClass = "ec.e.quake.StateTimeMachineMaker" ;
        }
        return (TimeMachine)eEnv.magicPower(TimeMachineMakerClass);
    }

    /**
     * Called once per vat by the TimeMachineMaker.
     */
    public TimeMachine(EEnvironment eEnv)
         throws OnceOnlyException, IOException {

        myVat = eEnv.vat();
        try {
            myCheckpointTether
                = (SettableTether)eEnv.magicPower("CheckpointTether");
        } catch(Exception ex) {
            throw new Error("shouldn't happen");
        }

        if (myCheckpointTether.held() != null) {
            System.out.println("checkpoint tether is non null...throwing");
            throw new OnceOnlyException("CheckpointTether already set");
        }
        String filename = eEnv.getProperty("checkpoint");
        if (filename == null) {
            //this vat is ephemeral
            return;
        }

        // StableStore starts out unencrypted.  StableStore.setCipher
        // should be called later to begin encrypting.  -emm
        StableStore checkpoint = new StableStore(filename);
        if (checkpoint.exists()) {
            throw new OnceOnlyException
                ("cannot overwrite a checkpoint " + filename);
        }
        myCheckpointTether.set(checkpoint);
    }

    /**
       Set the passphrase associated with this vat.  Once set, this
       passphrase will have to be provided in order to revive the vat.

       @param passphrase nullOK the passphrase to use, or null for no
       encryption.
    */
    emethod setPassphrase(String passphrase) {
        try {
            StableStore checkpoint = (StableStore)myCheckpointTether.held();
            checkpoint.rewriteWithNewPassphrase(passphrase);
            // checkpoint.setPassphrase(passphrase);
            // this <- commit(null); // make sure the checkpoint uses the new passphrase.
        }
        catch (IOException e) {
            ethrow new NestedException("Can't set passphrase", e);
        }
    }

    /**
     * Tells the watcher to notice the last quake.  If 'watcher' is
     * null, does nothing.
     */
    emethod lastQuake(Seismologist watcher) {
        if (watcher != null) {
            watcher <- noticeQuake(myVat.lastQuake());
        }
    }

    /**
     * Tells the watcher to notice the next quake, once it happens.
     * If 'watcher' is null, does nothing.
     */
    emethod nextQuake(Seismologist watcher) {
        myVat.lastQuake().waitForNext(watcher);
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

    protected void setWaiter(Seismologist waiter)  {
        myVat.setWaiter(waiter);
    }

    private void internalCommit(Seismologist waiter) throws IOException {
        StableStore checkpoint = (StableStore)myCheckpointTether.held();
        if (checkpoint != null) {
            /*
             * The saved checkpoint contains no Java stack frames, so
             * the restored computation will not perform the remainder
             * of this method after the save.  Therefore, we store the
             * waiter away around the save, so Vat.revive() can
             * proceed to tell the waiter that the commit succeeded.
             */
            setWaiter(waiter);
            checkpoint.save(myVat);
            setWaiter(null);
        }
        if (waiter != null) {
            waiter <- noticeCommit();
        }
    }

    /**
     * Cause a TimeQuake without saving/restoring.  Used for code to
     * practice quake recovery logic.
     */
    emethod quakeDrill(Seismologist watcher) {
        myVat.report(TimeQuake.QUAKE_DRILL);
        lastQuake(watcher);
    }

    /**
     * The persistent process is stopped in its tracks, presumably to
     * be revived again later, and the process incarnation exits with
     * the supplied exitCode. <p>
     */
    emethod hibernate(Seismologist waiter, int exitCode) {
        try {
            if (myCheckpointTether.held() == null) {
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
     * The persistent process is stopped in its tracks, presumably to
     * be revived again later, and the process incarnation exits with
     * the supplied exitCode.  If there is no StableStore, exits
     * anyway without commiting.
    emethod quit(Seismologist waiter, int exitCode) {
        try {
            if (myCheckpointTether.held() != null) {
                internalCommit(waiter);
            }
        } catch(IOException ex) {
            EStdio.reportException(ex);
            try {
                synchronized (this) {
                    wait(1000); // time to let error be flushed...
                }
            } catch (InterruptedException e) {
            }
        } finally {
            System.exit(exitCode);
        }
    }
    */

    /**
     * Exits the process incarnation without first saving.
     */
    emethod crash(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * Exits the persistent process as well as the process incarnation.
     */
    emethod suicide(int exitCode) {
        try {
            StableStore checkpoint = (StableStore)myCheckpointTether.held();
            if (checkpoint != null) {
                checkpoint.delete();
            }
            System.exit(exitCode);

        } catch(IOException ex) {
            ethrow new NonCommittal
                ("Can't stop myself from coming back " + ex);
        }
    }
}

/**
 * ethrown when there's some problem committing state to stable
 * storage
 */
public class NonCommittal extends RuntimeException {
    public NonCommittal(String s) { super(s); }
}
