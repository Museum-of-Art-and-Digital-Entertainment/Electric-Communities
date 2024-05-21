package ec.e.quake;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.io.IOException;

import ec.util.EThreadGroup;
import ec.e.util.PropUtil;
import ec.e.run.Vat;
import ec.e.file.EStdio;
import ec.e.openers.VarOpener;
import ec.e.run.OnceOnlyException;
import ec.security.TimerJitterEntropy;
import ec.e.inspect.Inspector;
import ec.trace.TraceController;

/**
 * Used to revive a Vat from an earlier checkpoint
 */
public class Revive {

    /**
       Start a new EThreadGroup (for improved exception reporting),
       and run this.EMain(args) in a new EThread in the new
       EThreadGroup.
    */
    public static void main(String args[]) {
        EThreadGroup.callEMain("ec.e.quake.Revive", args);
    }

    /**
     * To restart a Vat, do "java ec.e.quake.Revive filename",
     * where filename is the same name that was provided in
     * a "checkpoint=filename" property of an earlier EBoot command. <p>
     *
     * If a filename is provided, it is also used as the new
     * "checkpoint=filename" property.  If no filename is provided,
     * then the filename bound to this property is used.
     */
    static public void EMain(String[] args)
         throws IOException, OnceOnlyException {

        try {
            VarOpener.staticSelfTest();
        } catch (ExceptionInInitializerError eiie) {
            Throwable t = eiie.getException();
            System.err.println("oops " + t);
            t.printStackTrace();
            throw eiie;
        }
        Properties props = new Properties();
        String passphrase = null;

        args = PropUtil.argsAndProps(args, props);
        if (args.length >= 1) {
            //XXX should we complain rather than overwrite an existing
            //binding?
            props.put("checkpoint", args[0]);
        }
        if (args.length >= 2) {
            passphrase = args[1];
            args[1] = "Passphrase" ; // try to hide passphrase
        }
        else {
            passphrase = props.getProperty("Passphrase");
        }
        props.put("Passphrase", ""); // try to hide passphrase

        String filename = props.getProperty("checkpoint");
        if (filename == null) {
            throw new IllegalArgumentException
                ("usage: java ec.e.quake.Revive filename [passphrase]");
        }
        TraceController.start(props);

        TimerJitterEntropy.start();     // Start collecting entropy

        doRevival(filename, passphrase, args, props);
    }

    // this method is in Dummies.java as well!!!!!!!!!
    static public void doRevival(String filename,
                                 String passphrase,
                                 String args[],
                                 Properties props)
         throws IOException, OnceOnlyException
    {
        System.out.println("restoring from " + filename +
                           " with passphrase <<" + passphrase + ">>");

        StableStore checkpoint = new StableStore(filename, passphrase);
        Vat vat = (Vat)checkpoint.restore();

        EStdio.initialize(vat);
        vat.revive(args, props, checkpoint);
        // If we specify "Inspector=stop" or "=full" in props, then we get an Inspector.
        // We can also specify inspector UI class name. Default is "IFCInspectorUI".
        Inspector.checkForAndStartInspector(props.getProperty("Inspector"),
                                            props.getProperty("InspectorClass"));
    }
}
