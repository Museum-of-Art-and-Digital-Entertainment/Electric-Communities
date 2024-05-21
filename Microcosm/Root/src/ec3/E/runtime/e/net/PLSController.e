package ec.app.net;

import ec.trace.Trace;
import ec.e.run.ELaunchable;
import ec.e.run.EEnvironment;
import ec.e.file.EStdio;
import ec.e.io.EInputHandler;
import ec.e.lang.EString;
import ec.e.quake.TimeMachine;
import ec.e.io.EConsoleMaker;
import ec.e.net.PLSControllerServer;
import ec.e.net.Registrar;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRef;


public eclass PLSController implements ELaunchable, EInputHandler {
    static private final Trace tr = new Trace("ec.app.net.PLSController");
    private PLSControllerServer pls = (PLSControllerServer) EUniChannel.construct(PLSControllerServer.class);
    private EUniDistributor pls_dist = EUniChannel.getDistributor(pls);
    private TimeMachine tm = null;
    private Registrar myRegistrar;
    
    emethod go(EEnvironment env) {
        try {
            EString reply = (EString) EUniChannel.construct(EString.class);
            EUniDistributor reply_dist = EUniChannel.getDistributor(reply);
            
            tm = TimeMachine.summon(env);

            String location = env.getProperty("PLS") ;

            env.setProperty("ShutdownPLSConnections", "false");
            myRegistrar = Registrar.summon(env);
            myRegistrar.onTheAir();

            SturdyRefFileImporter importer = myRegistrar.getSturdyRefFileImporter(env);
            SturdyRef plsref = importer.importRef(location);
            plsref.followRef(pls_dist);

            EConsoleMaker consoleMaker = EConsoleMaker.summon(env);
            consoleMaker.makeConsole(this, EStdio.in(), null);
            pls <- command("connect", reply_dist);
            this <- printReply(reply);
        }
        catch (Throwable t) {
            EStdio.reportException(t);
            tm <- suicide(1);
        }
    }
    
    emethod handleInput (String line) {
        EString reply = (EString) EUniChannel.construct(EString.class);
        EUniDistributor reply_dist = EUniChannel.getDistributor(reply);
        
        if (line == null) {
            EStdio.out().println("Exiting...");
            tm <- suicide(0);
        }
        
        else {
            if (tr.verbose && Trace.ON) tr.$("You said: '" + line + "'");
            if (line.startsWith("quit")) {
                tm <- suicide(0);
            }
            else {
                pls <- command(line, reply_dist);
                this <- printReply(reply);
            }
        }
    }

    emethod printReply(EString reply) {
        ewhenever reply (String str) {
            EStdio.out().println(str);
        }
    }
}

