/*
  Agency.e -- The Agency and supporting classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

import ec.trace.Trace;
import ec.e.file.EDirectoryRootMaker;
import ec.e.file.EStdio;
import ec.e.net.Registrar;
import ec.e.quake.TimeMachine;
import ec.e.rep.ParimeterizedRepository;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Seismologist;
import ec.e.start.Syncologist;
import ec.e.start.TimeQuake;
import ec.util.EThreadGroup;
import ec.util.Native;
import ec.vcache.ClassCache;
import java.util.Vector;

/**
 * Sets up and manages the execution environment for E/Pluribus applications
 * such as Habi*.
 */
public eclass Agency implements ELaunchable, Seismologist {
    static Trace tr = new Trace("ec.pl.runtime.Agency");
    static Trace tr_timer = new Trace("StartupTimer");
    
    private AgentInfo myInfo = new AgentInfo();
    private boolean isStarted = false;
    private boolean isSoulTest;
    private EEnvironment myEnv;
    private long myTimeBegin;
    
    /**
     * Create a new agency.
     *
     * Constructor has no arguments because the only way these objects should
     * be getting created is by EBoot, which gets the name of the class to
     * launch from the command line, obtains the named Class object, and then
     * calls newInstance() on this (which implicitly invokes the zero-argument
     * constructor). This startup technique demands that we have a public,
     * zero-argument constructor here. However, nobody else should call it.
     */
    public Agency() {
    }
    
    /**
     * Start the agency. This method implements the ELaunchable interface
     * expected by EBoot.
     *
     * @param env  The E environment to execute inside
     *
     * There are two startup modes, depending on the setting of the boolean
     * environment property "makeTemplate". If "makeTemplate" is "true", the
     * program immediately hibernates, resulting in a new template file.
     * Otherwise, it goes ahead and starts up the application.
     */
    emethod go(EEnvironment env) {
        String checkpoint = env.getProperty("checkpoint");
        boolean makeTemplate =
            env.getProperty("makeTemplate", "false").equals("true");

        if (makeTemplate && checkpoint == null) {
            EStdio.err().println(
                "makeTemplate specified without checkpoint filename");
            System.exit(1);
        }
        if (!makeTemplate) {
            this <- initialStartup(env);
            return;
        }
        /* Eventually, we'll always make a template. When that time comes,
           remove everything above here. */

        myEnv = env;
        try {
            TimeMachine tm = TimeMachine.summon(env);
            tm <- hibernate(this, 0);
        } catch (Exception e) {
            EStdio.err().println("error summoning TimeMachine");
            EThreadGroup.printStackTrace(e);
            System.exit(1);
        }
    }
    
    /**
     * Notice a quake. This is part of the Seismologist interface.
     *
     * Actually, we don't really care about quakes here, so this method does
     * nothing. But it needs to be here in order for us to be a Seismologist,
     * and we need to be a Seismologist because we *are* interested in getting
     * the 'noticeCommit' message.
     */
    emethod noticeQuake(TimeQuake q) {
    }
    
    /**
     * Notice a commit. This is part of the Seismologist interfce.
     *
     * This is the event we are really interested in. Once we have committed
     * we should just start up the application.
     */
    emethod noticeCommit() {
        this <- initialStartup(myEnv);
    }
    
    /**
     * Actually start up the E/Pluribus application.
     *
     * @param env  The E environment to execute inside
     */
    emethod initialStartup(EEnvironment env) {
        try {
            /* If somehow we got "started" more than once, bail out now. */
            if (isStarted) {
                return;
            }
            isStarted = true;

            myEnv = env;
            
            /* Set up the agency info for everyone to use. */
            if (tr.debug && Trace.ON)
                tr.$("Establishing Agency's magic powers");
            myInfo.directoryMaker = (EDirectoryRootMaker)myEnv.magicPower(
                "ec.e.file.EDirectoryRootMakerMaker");
            try {
                myInfo.framework = (UIFramework)myEnv.magicPower(
                    "ec.pl.runtime.MagicUIPowerMaker");
            } catch (Exception e) {
                if (tr.debug && Trace.ON) {
                    tr.$("Ignored exception creating " +
                         "ec.pl.runtime.MagicUIPowerMaker:");
                    EStdio.reportException(e);
                }
            }
            
            myInfo.unumMaster = new UnumMaster();

            // XXX Avoid initial establishing of Registrar if doing SoulTest
            isSoulTest = env.getProperty("soultest", "false").equals("true");
            if (!isSoulTest) {
                EStdio.out().println("Agency Establishing registrar.");
                myInfo.registrar = Registrar.summon(myEnv);
                myInfo.registrar.onTheAir();
            } else {
                EStdio.out().println(
                    "Agency not estabishing registrar 'cuz it's a soul test");
            }
            
            /* Launch the agent, i.e., application, specified by the "Agent"
               environment property. */
            String agentClassName = myEnv.getProperty("Agent") + "_$_Impl";
            Class agentClass = ClassCache.forName(agentClassName);
            Agent agent = (Agent) agentClass.newInstance();
            if (tr_timer.debug && Trace.ON) {
                tr_timer.debugm("!!Starting Agency ... ");
                myTimeBegin = Native.queryTimer();
            }
            agent <- go(myEnv, myInfo);
            if (tr_timer.debug && Trace.ON)
                tr_timer.debugm("   !!time for Agency to complete startup:" +
                                Native.deltaTimerMSec(myTimeBegin) + " msec.");
        } catch (Throwable t) {
            EStdio.err().println("Pluribus Agency: fatal error in startup:");
            EStdio.reportException(t);
            EStdio.err().println("Aborting");
            try {
                /* Sleep so that people can see the above error output even
                   when running in impoverished GUI environments where the
                   error window disappears when the program exits! */
                Thread.currentThread().sleep(20000);
            } catch (InterruptedException e) {
                /* Since the sleep was basically cosmetic, and we are about to
                   die anyway, don't sweat any exceptions */
            }
            System.exit(1);
        }
    }
}

/**
 * The launch interface for an E/Pluribus application. Something that wants to
 * be run by the agency should implement this.
 */
public einterface Agent
{
    /**
     * Start up the agent.
     *
     * @param env  The E environment to execute inside
     * @param info  Global resources all Pluribus agents need
     */
    go(EEnvironment env, AgentInfo info);
}

/**
 * This class is simply a struct full of information needed by the agent at
 * runtime.
 *
 * XXX CLEANUP: The content of this struct is entirely ad hoc, being driven
 * by the discovered needs of the RealmText subsystem. Indeed, the only
 * entities which reference this class are in the RealmText classes; it is
 * not even used by the rest of the Habi* application! This is inappropriate
 * for a mechanism that is intended to be generic. More properly, it makes
 * sense for us to revisit what capabilities are being passed via this
 * mechanism and see if we can find a more application-embedded means to do
 * this and rid ourselves of this class entirely (as well as removing it from
 * the agent API).
 */
public class AgentInfo
{
    public Registrar registrar;
    public EDirectoryRootMaker directoryMaker;
    public UIFramework framework;
    public UnumMaster unumMaster;
    /* XXX This should be a properly fleshed out SoulTracker */
    public Vector soulStateVector;
}

/**
 * Encapsulates special powers over an unum: getting the router or soul, or
 * killing the unum.
 *
 * XXX CLEANUP: given that all operations require a key object anyway, it is
 * not clear that these functions need to be protected by their own object. It
 * may be possible to move these to the Ingredient base class, for example.
 *
 * XXX CLEANUP: all these operations take a parameter typed to be an Unum, but
 * it is assumed that what we really have is a deflector to an unum. This may
 * in fact be true in practice but the soundness of this assumption should be
 * investigated. And if it's always a deflector, shouldn't the parameter be
 * so declared?
 */
public class UnumMaster
{
    /**
     * Package protected constructor (so only Pluribus runtime can hand these
     * out).
     */
    UnumMaster() {
    }
    
    /**
     * Return the UnumRouter associated with a particular unum.
     *
     * @param unum  The unum whose router is desired.
     * @param key  Key object to prove you are allowed to do this
     */
    public UnumRouter getUnumRouter(Unum unum, Object key) {
        UnumRouter unumRouter =
            (UnumRouter) RtDeflector.getTarget((RtDeflector)unum, key);
        return unumRouter;
    }

    /**
     * Return the UnumSoul associated with a particular unum.
     *
     * @param unum  The unum whose soul is desired.
     * @param key  Key object to prove you are allowed to do this
     */
    public UnumSoul getUnumSoul(Unum unum, Object key) {
        UnumRouter unumRouter = getUnumRouter(unum, key);
        return unumRouter.getUnumSoul();
    }

    /**
     * Kill a particular unum.
     *
     * @param unum  The unum you want to kill.
     * @param key  Key object to prove you are allowed to do this
     */
    public void killUnum(Unum unum, Object key) {
        UnumRouter unumRouter = getUnumRouter(unum, key);
        unumRouter.killUnum();
    }
}
