package ec.e.boot;

import ec.trace.Trace;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Random;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;

import ec.e.run.HabiLoader;
import ec.e.run.Vat;
import ec.e.run.EEnvironment;
import ec.e.run.ELaunchable;

import ec.vcache.ClassCache;
import ec.util.EThreadGroup;

import ec.e.file.EStdio;
import ec.e.inspect.Inspector;
import ec.e.quake.StableStore;
import ec.e.quake.Revive;
import ec.e.run.OnceOnlyException;
import ec.trace.TraceController;
import ec.e.run.CrewCapabilities;
import ec.e.timer.Timer;
import ec.e.timer.ClockController;
import ec.e.util.PropUtil;
import ec.security.TimerJitterEntropy;
import ec.security.SecureRandomCrew;
import ec.util.Native;
import ec.e.run.NotificationManager;

/**
 * Usage: <pre>
 *     java ec.e.start.ELogin
 * </pre>
 *
 * The args are used to initialize the properties and realArgs within
 * the EEnvironment.  They are interpreted according to the format in
 * PropUtil.argsAndProps().
 *
 * @see ec.e.util.crew.PropUtil#argsAndProps
 */
public class ELogin {
    //
    // class variables
    //
    private static boolean OneOnly = true; // to suppress multiple launchings
    static Trace tr = new Trace("ec.e.boot.ELogin");
    static Trace tr_timer = new Trace("StartupTimer");
    //
    // instance variables
    //
    private HabiLoader      myHL = null;
    private Properties      myProperties = null;
    private String[]        myArgs = null;
    private String          myClassToLoad = null;
    private String          myPassphrase = null;
    private boolean         myWaiting = true;
    private Object          myRepository = null;
    private static ELogin   myLogin = null;

    /**
     * to suppress instantiation
     */
    private ELogin(HabiLoader hl) {myHL = hl;}

    /**
       Start a new EThreadGroup (for improved exception reporting),
       and run this.EMain(args) in a new EThread in the new
       EThreadGroup.
    */
    public static void main(String args[]) {
        EThreadGroup.callEMain("ec.e.boot.ELogin", args);
    }

    /**
     * When EBoot is the first class on the 'java' command line,
     * 'main' gets called with the remaining command line arguments.
     * 'main' can only be called once.  'args[0]' is the above
     * first-guest-class, and is typically the third command line
     * argument (after 'java' and 'ec.e.start.EBoot'). <p>
     *
     * @exception ClassNotFoundException when 'args[0]' does not
     * correspond to a class on the CLASSPATH. <p>
     *
     * @exception IllegalAccessException when 'args[0]' names a
     * non-permitted class, or a class whose zero-argument
     * constructor is not permitted. <p>
     *
     * @exception InstantiationException when 'args[0]' names an
     * uninstantiable class, such as an interface or an abstract
     * class. <p>
     *
     * @exception IOException when a file from which one is supposed
     * to read further properties does not exist or cannot be read.
     * However, if "-ECNoDefaults" is not provided, but any of the
     * default properties files does not exist, they are silently
     * skipped rather than throwing an exception.  But if they do
     * exist and aren't readable, they throw an exception like any
     * other properties file. <p>
     *
     * @exception OnceOnlyException if called more than once. <p>
     */
    public static void EMain(String args[])
         throws ClassNotFoundException, IllegalAccessException,
         InstantiationException, IOException, OnceOnlyException
    {
        if (!OneOnly) {
            throw new OnceOnlyException
                ("ELogin.main() must only be called once");
        }

        OneOnly = false;

        cafeLoadHack(); // preload class(es) that are giving Cafe trouble.

        Properties props = new Properties();
        String[] theArgs = PropUtil.argsAndProps(args, props);
        String startupList = props.getProperty("StartupList");
        String startupPreload = props.getProperty("StartupClassPreload");

        //Preload classes if desired.
        HabiLoader theHL = null;
        if (startupPreload != null) {
          if ("preload".equals(startupPreload)) {
            if (tr.debug && Trace.ON) {
              tr.debugm("Pre-loading classes.");
            } else if (tr_timer.debug && Trace.ON) {
              tr_timer.debugm("Pre-loading classes.");
            }
            theHL = HabiLoader.oneTimeInstance(startupList);
            theHL.run();
          } else {
          }
        }


        myLogin = new ELogin(theHL);

        // XXX need a way to make this capability secure.
        // Unfortunately, Trace needs to be compiled quite early.
        // BEM: the new (September) version of Trace is supposed
        // to be capability secure.
        // XXX should avatarProps be passed to Trace?
        // Not at this point, but after we've picked the one we're
        // going to launch it would be nice to mix them in.  -emm
        TraceController.start(props);

        // XX create SuperRepository in CrewCapabilities and store it here as
        // an interim
        myLogin.myRepository = CrewCapabilities.createCrewRepository(props);

        TimerJitterEntropy.start(); // Start collecting entropy
        if (props.getProperty("Developer") != null) {
            tr.errorm("Developer mode set.  Seeding the entropy gatherer " +
                      "with unique but not completely unguessable bits");
            // some pseudo-randomness
            byte[] fakeSeed = new byte[20];
            (new Random(System.currentTimeMillis())).nextBytes(fakeSeed);
            SecureRandomCrew.provideEntropy(fakeSeed, 160);
            // and some (hopefully) unique bits
            SecureRandomCrew.provideEntropy(
                System.getProperty("user.name").getBytes(), 1);
            SecureRandomCrew.provideEntropy(
                System.getProperty("user.home").getBytes(), 1);
            SecureRandomCrew.provideEntropy(
                System.getProperty("user.dir").getBytes(), 1);
        }
        long begin = 0;
        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Creating login class... ");
            begin = Native.queryTimer();
        }
        Class loginClass = ClassCache.forName(props.getProperty("LoginUI"));
        Object loginUI = loginClass.newInstance();
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done creating login class: "
                + Native.deltaTimerMSec(begin) + " msec.");

        myLogin.myClassToLoad = args[0] + "_$_Impl";
        myLogin.myProperties = props;

        myLogin.myArgs = new String[args.length-1];
        System.arraycopy(args, 1, myLogin.myArgs, 0, myLogin.myArgs.length);

        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Initing login... ");
            begin = Native.queryTimer();
        }

        ((ELoginPresenter)loginUI).init(myLogin,
                                        props,
                                        new CheckpointPassphraseVerifier());
        if(tr_timer.debug && Trace.ON)
          tr_timer.debugm("   !!Done initing login: " +
                          Native.deltaTimerMSec(begin) + " msec.");

        myLogin.continueRunning(args);
    }

/*  This method is used to work around a bug in Cafe class loading
    that causes class loading to fail under certain circumstances.
    While the precise nature of the bug is uncertain, it appears
    to involve static initializers and class loading taking place
    relatively late in the Habi* initialization sequence.

    We may eventually move this hack into another class that is
    itself dynamically loaded. Presently, this seems unneccesary
    since ec3 and Cafe will both be revised in several weeks...

    JAY 3/6/98
*/
    static void cafeLoadHack() {
      try {
        // Attempt to load the Recipe3D class that was giving us trouble.
        Class recipeClass = Class.forName("ec.cosm.gui.appearance.Recipe3D");
      } catch (ClassNotFoundException ex) {
        // If the class is not there - ignore that completely.
      }
    }

    public void afterLogin(Properties props) {
        if (props != null) {
            if ((myHL != null)&&
                (myHL.isAlive())){
              myHL.setPriority(6);
            }
            myPassphrase = props.getProperty("Passphrase");
            if (myPassphrase != null && myPassphrase.length() == 0) {
                myPassphrase = null;
            }
            Enumeration en = props.propertyNames();
            while (en.hasMoreElements()) {
                String pname = (String)en.nextElement();
                if (!pname.equals("Passphrase")) {
                    // this might be a good place to inform TraceController.
                    myProperties.put(pname, props.getProperty(pname));
                }
            }
        }
        else {
            System.out.println("User login cancelled or failed.");
            try {
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) {}
            System.exit(0);
        }

        synchronized (this) {
            myWaiting = false;
            notifyAll();
        }
    }

    void continueRunning(String[] args)
         throws ClassNotFoundException, IllegalAccessException,
         InstantiationException, IOException, OnceOnlyException
    {
        Properties props = new Properties();
        String[] theArgs = PropUtil.argsAndProps(args, props);
        String startupList = props.getProperty("StartupList");
        String startupPreload = props.getProperty("StartupClassPreload");

        //Preload classes if desired.
        if (startupPreload != null) {
          if("background".equals(startupPreload)) {
            if (tr.debug && Trace.ON) {
              tr.debugm("Loading classes in the background.");
            } else if (tr_timer.debug && Trace.ON) {
              tr_timer.debugm("Loading classes in the background.");
            }
            myHL = HabiLoader.oneTimeInstance(startupList);
            myHL.start();
          } else {
          }
        }

        while (myWaiting) {
            synchronized (this) {
                try {
                    wait(5000);
                }
                catch (InterruptedException e) {
                }
            }
        }

        /**
         * Kill orthogonal persistence once and for all

        String checkpoint = myProperties.getProperty("checkpoint");
        if (checkpoint != null) {
            if (StableStore.exists(checkpoint)) {
                long begin = 0;
                if(tr_timer.debug && Trace.ON) {
                  tr_timer.debugm("!!Reviving from checkpoint... ");
                  begin = Native.queryTimer();
                }
                Revive.doRevival(checkpoint, myPassphrase, myArgs, myProperties);
                if(tr_timer.debug && Trace.ON)
                    tr_timer.debugm("   !!Done reviving: " +
                                    Native.deltaTimerMSec(begin) + " msec.");
                CrewCapabilities.setCrewRepository(myLogin.myRepository);
                myPassphrase = null;
                return;
            }
        }
        */

        long begin = 0;
        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Creating vat... ");
            begin = Native.queryTimer();
        }
        Vat vat = new Vat(myProperties);
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done creating vat: " +
                            Native.deltaTimerMSec(begin) + " msec.");
        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Creating classloader... ");
            begin = Native.queryTimer();
        }
        ClassLoader sysLoader = new Object().getClass().getClassLoader();
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done creating classloader: " +
                            Native.deltaTimerMSec(begin) + " msec.");
        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Creating EEnvironment... ");
            begin = Native.queryTimer();
        }
        EEnvironment env = new EEnvironment(myArgs, myProperties, vat, sysLoader);
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done creating EEnvironment: " +
                            Native.deltaTimerMSec(begin) + " msec.");

        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Initing timer and clocks... ");
            begin = Native.queryTimer();
        }
        Timer.makeTheTimers(env);
        ClockController.makeTheClockControllers(env);
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done initing timers and clocks: " +
                            Native.deltaTimerMSec(begin) + " msec.");

        EStdio.initialize(vat);
        vat.setEEnvironment(env);

        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Extablishing crew capabilities... ");
            begin = Native.queryTimer();
        }
        CrewCapabilities.establishCrewCapabilities(env, myLogin.myRepository);
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("   !!Done establishing crew capabilities: " +
                            Native.deltaTimerMSec(begin) + " msec.");

        if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm("!!Starting inspector... ");
            begin = Native.queryTimer();
        }
        Inspector.checkForAndStartInspector(env);
        if(tr_timer.debug && Trace.ON)
            tr_timer.debugm("  !!Done starting inspector: " +
                            Native.deltaTimerMSec(begin) + " msec.");

        Object obj = ClassCache.forName(myClassToLoad).newInstance();
        ELaunchable nextThingToDo = (ELaunchable)obj;
        nextThingToDo <- go(env);
    }

    /**
     * Returns the SuperRepository that is being stored as an interim here
     */
    public Object repository() {
        return myRepository;
    }
}

public interface ELoginPresenter {
    public void init(ELogin loginObject, Properties avatarProps, PassphraseVerifier verifier);
}

public interface PassphraseVerifier
{
    public boolean verify(Properties props, String passphrase);
}

public class CheckpointPassphraseVerifier implements PassphraseVerifier
{
    public CheckpointPassphraseVerifier() { }

    public boolean verify(Properties props, String passphrase) {

        // XXX Dima: Scott, fix it back after you are done!!!!!
        if (true) {
            return true;
        }
        else {
            if (passphrase != null && passphrase.length() == 0) {
                passphrase = null;
            }
            String checkpoint = props.getProperty("checkpoint");
            if (checkpoint == null) {
                return true;
            }
            try {
                if (!StableStore.exists(checkpoint)) {
                    return true;
                }
            }
            catch (IOException e) {
                EStdio.reportException(e);
                return false;
            }

            return StableStore.checkPassphrase(checkpoint, passphrase);
        }
    }
}


public interface RepositoryInitializer {
    public void initializeRepository();
}
