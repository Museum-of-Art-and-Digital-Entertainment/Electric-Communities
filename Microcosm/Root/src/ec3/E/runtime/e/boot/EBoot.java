package ec.e.boot;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;

import ec.e.run.ELaunchable;
import ec.e.run.Vat;
import ec.e.run.EEnvironment;
import ec.util.EThreadGroup;
import ec.e.run.OnceOnlyException;
import ec.trace.TraceController;
import ec.e.util.PropUtil;
import ec.e.file.EStdio;
import ec.e.timer.Timer;
import ec.e.timer.ClockController;
import ec.e.inspect.Inspector;
import ec.e.run.CrewCapabilities;
import ec.security.TimerJitterEntropy;

/**
 * Usage: <pre>
 *     java ec.e.start.EBoot <em>first-guest-class args...</em>
 * </pre>
 * 'first-guest-class' must implement ELaunchable.  It is launched by
 * being sent the emessage 'go(anEEnvironment)'. <p>
 *
 * The args are used to initialize the properties and realArgs within
 * the EEnvironment.  They are interpreted according to the format in
 * PropUtil.argsAndProps(). 
 *
 * @see ec.e.util.crew.PropUtil#argsAndProps
 */
public class EBoot {

    /**
     * to suppress multiple launchings
     */
    private static boolean AreDone = false;

    /**
     * to suppress instantiation
     */
    private EBoot() {}

    /**
       Start a new EThreadGroup (for improved exception reporting),
       and run this.EMain(args) in a new EThread in the new
       EThreadGroup.
    */
    public static void main(String args[]) {
        EThreadGroup.callEMain("ec.e.boot.EBoot", args);
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
        if (AreDone) {
            throw new OnceOnlyException
                ("EBoot.main() must only be called once");
        }
        AreDone = true;
        Properties props = new Properties();
        args = PropUtil.argsAndProps(args, props);

        if (args.length < 1) {
            throw new Error
                ("EBoot needs class name to launch: " + args);
        }

        /*
         * In lieu of a GuestLoader, it would be good to at least
         * ensure this is a guest class
         */
        Object obj = Class.forName(args[0] + "_$_Impl").newInstance();

        ELaunchable starter;
        try {
            starter = (ELaunchable)obj;
        } catch (ClassCastException e) {
            //make diagnostic more informative
            throw new ClassCastException("a " + args[0]
                                         + " isn't an ELaunchable");
        }
        if ("true".equals(props.getProperty("ECtraceProperties", "false"))) {
            System.out.println("[root properties at startup]");
            props.list(System.err);
        }

        String newArgs[] = new String[args.length-1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        Vat vat = new Vat(props);
        ClassLoader sysLoader = new Object().getClass().getClassLoader();
        EEnvironment env = new EEnvironment(newArgs, props, vat, sysLoader);
        Timer.makeTheTimers(env);
        ClockController.makeTheClockControllers(env);

        EStdio.initialize(vat);

        // XXX need a way to make this capability secure.
        // Unfortunately, Trace needs to be compiled quite early.
        // BEM: the new (September) version of Trace is supposed
        // to be capability secure.
        TraceController.start(env.props());

        TimerJitterEntropy.start();     // Start collecting entropy

        vat.setEEnvironment(env);

        CrewCapabilities.establishCrewCapabilities(env);

        Inspector.checkForAndStartInspector(env);
        starter <- go(env);

        //if main was called from the top of the world, at this point
        //the main thread falls off the end of the world.
    }
}
