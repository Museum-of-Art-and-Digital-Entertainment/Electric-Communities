package ec.app.net;

import ec.e.run.ELaunchable;
import ec.e.run.EEnvironment;
import ec.e.file.EStdio;
import ec.e.quake.TimeMachine;
import ec.e.net.DoCommit;
import ec.e.net.Registrar;
import ec.e.net.ProcessLocationServer;
import ec.e.net.ProcessLocationServerHelper;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.PLSControllerHelper;
import ec.e.net.PLSControllerServer;


    
public eclass BareProcessLocationServer implements ELaunchable {
    emethod go(EEnvironment env) {
        TimeMachine tm = null;

        try {
            tm = TimeMachine.summon(env);
            
            String registerWith = env.getProperty("RegisterWith");
            env.setProperty("RegisterWith", "");
        
            if (registerWith == null) {
                throw new Error("must specify RegisterWith=<file to put SturdyRef to Process Location Server in>");
            }
            String searchPath = env.getProperty("SearchPath");
            if (searchPath == null || searchPath.indexOf(";") >= 0) {
                throw new Error("must specify SearchPath=<fully.qualified.host.name>:<well known port>");
            }

            PLSCheckpointer committer = new PLSCheckpointer(tm);
            
            Registrar registrar = Registrar.summon(env);

            registrar.onTheAir();

            ProcessLocationServerHelper helper = new ProcessLocationServerHelper(env, searchPath, committer);
            ProcessLocationServer pls = new ProcessLocationServer(helper, searchPath);
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRef ref = refMaker.makeSturdyRef((EObject)pls);
            SturdyRefFileExporter exporter = registrar.getSturdyRefFileExporter(env);

            exporter.exportRef(ref, registerWith);
            registrar.setProcessLocationServerSet(registerWith, false);

            String controller = env.getProperty("Controller");
            if (controller != null) {
                PLSControllerHelper controllerHelper = new PLSControllerHelper(helper);
                PLSControllerServer server = new PLSControllerServer(controllerHelper);
                ref = refMaker.makeSturdyRef((EObject)server);
                exporter.exportRef(ref, controller);
            }
            
            tm <- hibernate(null, 0);
        }
        catch (Throwable t) {
            EStdio.reportException(t);
            tm <- suicide(1);
        }
    }
}

public eclass PLSCheckpointer implements DoCommit {
    TimeMachine myTimeMachine;
    
    public PLSCheckpointer(TimeMachine tm) {
        myTimeMachine = tm;
    }
    
    emethod commit() {
        myTimeMachine <- commit(null);
    }
}
