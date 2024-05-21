package ec.tests.hab;

import ec.ifc.app.ECApplication;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.e.file.EDirectoryRootMaker;
import ec.e.file.EEditableDirectory;
import ec.e.start.Tether;
import ec.e.io.crew.RtConsole;

// To run this testcase, execute:
// java ec.tests.hab.HaberTest

/**

 * This is our launcher. It is given the all-powerful environment and
 * doles out capabilities piecemeal to the parts that need them.

 */

public eclass HaberTest implements ELaunchable {
    private ECApplication application;
    private EEnvironment myEnv = null;
    private Vat vat = null;

    emethod go (EEnvironment env) {
        myEnv = env;
        vat = env.vat();

        ec.ui.IFCInspectorUI.start("full"); // Start an Inspector in its own thread
        ec.e.inspect.Inspector.gather(env, "Vat", "Environment");
        ec.e.inspect.Inspector.gather(vat, "Vat", "Vat");

        try {
            EStdio.initialize(vat);

            // Summon the capabilities we'll need here and now
            // Then hand them out piecemeal to the entities that will use them.

            EDirectoryRootMaker rootMaker = new EDirectoryRootMaker(vat);
            EEditableDirectory rootDir = rootMaker.makeDirectoryRoot("./testbed");
            ec.e.inspect.Inspector.gather(rootMaker, "Capabilities", "RootMaker");

            // Create a console.

            Tether handlerHolder = new Tether(vat, new Eshiell(System.out, rootDir));
            RtConsole.setupConsoleReader(handlerHolder, System.in, null);
            ec.e.inspect.Inspector.gather(handlerHolder, "Vat", "Tether to Eshiell");
        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw");
            e.printStackTrace(EStdio.err());
        }
    }

    // Entry point. Call eboot with ourselves as the first argument,
    // followed by all given arguments.
    // XXX Hm. This does not work. Might be important to figure out why!

    public static void main(String inArgs[]) {
        String args[] = new String[inArgs.length + 1];
        args[0] = "ec.tests.hab.HaberTest";
        java.lang.System.arraycopy((Object)inArgs,0,(Object)args,1,inArgs.length);
        try {
            ec.e.start.EBoot.main(args);
        } catch (Throwable t) {
            System.out.println("Exception caught in main():");
              t.printStackTrace();
        }
    }
}
