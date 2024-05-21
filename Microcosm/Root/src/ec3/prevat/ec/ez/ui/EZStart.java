package ec.e.start;

import java.util.Properties;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.IOException;
import ec.util.EThreadGroup;
import ec.e.run.*;

import ec.e.run.OnceOnlyException;
import ec.e.run.TraceController;
import ec.e.util.crew.PropUtil;
import ec.e.file.EStdio;
import ec.e.timer.Timer;
import ec.e.timer.ClockController;
import ec.e.inspect.Inspector;
import ec.e.start.crew.CrewCapabilities;
import ec.security.crew.TimerJitterEntropy;


import java.util.Hashtable;

import ec.ez.ui.Listener;

import ec.e.net.ListenerInterest;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefImporter;
import ec.e.timer.Timer;
import ec.e.timer.ClockController;

/**
 * Usage: <pre>
 *     java ec.ez.start.EBoot <em>first-guest-class args...</em>
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
public class EZStart {

    /**
     * to suppress multiple launchings
     */
    private static boolean AreDone = false;

    public static void main(String args[])
         throws ClassNotFoundException, IllegalAccessException,
         InstantiationException, IOException, OnceOnlyException
    {
        if (AreDone) {
            throw new OnceOnlyException
                ("EZ.main() must only be called once");
        }
        AreDone = true;
        Properties props = new Properties();
        args = PropUtil.argsAndProps(args, props);

        ELaunchable starter;
/* Turned off
        if(args.length >= 1) {
        Object obj = Class.forName(args[0] + "_$_Impl").newInstance();

            try {
                starter = (ELaunchable)obj;
            } catch (ClassCastException e) {
            //make diagnostic more informative
            throw new ClassCastException("a " + args[0]
                                         + " isn't an ELaunchable");
            }
        } else {
            starter = new ezdummy();
        }
*/
        starter = new ezdummy();

        if ("true".equals(props.getProperty("ECtraceProperties", "false"))) {
            System.out.println("[root properties at startup]");
            props.list(System.err);
        }

        Vat vat = new Vat(props);
        ClassLoader sysLoader = new Object().getClass().getClassLoader();
        EEnvironment env = new EEnvironment(args, props, vat, sysLoader);
        Timer.makeTheTimers(env);
        ClockController.makeTheClockControllers(env);

        EStdio.initialize(vat);

        // XXX need a way to make this capability secure.
        // Unfortunately, Trace needs to be compiled quite early.
        // BEM: the new (September) version of Trace is supposed
        // to be capability secure.
        // XXX JAY - made conditional on property to turn off spam.
        if ("true".equals(props.getProperty("ECTraceOK"))) {
            TraceController.start(env.props());
        }

        vat.setEEnvironment(env);
        starter <- go(env);

        //if main was called from the top of the world, at this point
        //the main thread falls off the end of the world.
    }
}


eclass ezdummy implements ELaunchable {

    ezdummy() {}

    emethod go(EEnvironment env) {
        Hashtable hash = new Hashtable();

// Capture as much stuff as possible from the environment and put in into the POV
// table. (XXX JAY - check with a security guru on this)

        hash.put("args", env.args());
        hash.put("props", env.props());
        hash.put("vat", env.vat());
        hash.put("environment", env);
        Enumeration en = env.propertyNames();
        while (en.hasMoreElements()) {
            String theKey = (String) en.nextElement();
            String theValue = env.getProperty(theKey);
            hash.put(theKey, theValue);
        }

     if( (env.getProperty("registerWith") != null) ||
            (env.getProperty("RegisterWithURLs") != null) ) {
// If requested, fire up the registrar

        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");

        Registrar registrar = new Registrar(env);
        SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
        hash.put("refmaker", refMaker);

        try {
           registrar.onTheAir();
   // turn this line on, the previous off, when not using PLS.
   //           registrar.onTheAir(env.getProperty("SearchPath"));
            hash.put("registrar", registrar);
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }

        if (reg != null) {
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            hash.put("exporter", refExporter);
        }

        if (lookup != null) {
             SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
             hash.put("importer", importer);
        }

     } // end check for registerWith defined.
        try {
            Listener.launchEZ(hash);
            }
        catch (Exception e) {
        }

    }
}
