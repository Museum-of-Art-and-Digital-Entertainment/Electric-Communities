package ec.e.start;

import java.util.NoSuchElementException;
import java.io.IOException;
import ec.e.file.EWritableDirectory;
import ec.e.file.RtFileTrace;
import ec.e.net.ENet;
import ec.e.net.ERegistrar;
import ec.e.net.RtNetTrace;
import ec.e.net.RtConnection;
import ec.e.cap.EEnvironment;
// import ec.e.db.DBTracing;
import ec.e.run.RtRun;
import ec.e.run.EObjectExport;
import ec.e.run.EProxy_$_Impl;
import ec.e.quake.Vat;
import ec.e.quake.TimeMachine;
import ec.e.quake.StableStore;


final class RtRootCapabilities {

    /**
     *
     */
    static void CreateSturdyRoots(Vat vat) 
         throws IOException, OnceOnlyException {

        EEnvironment env = vat.eEnv();
        RtRun eRunner;
        synchronized(vat.vatLock()) {
            eRunner = new RtRun(vat.vatLock());
            String causalityId = (env.getProperty("CausalityId"));
            if (causalityId != null) {
                RtRun.setCausalityId(causalityId);
            }
            RtRun.setCausalityTracing
                (env.getPropertyAsBoolean("CausalityTracing"));
            
            boolean dgcTracing = env.getPropertyAsBoolean("ECtraceDGC");
            EObjectExport.tr.traceMode(dgcTracing);
            EProxy_$_Impl.tr.traceMode(dgcTracing);
            
            RtNetTrace.TraceMode(env);
            //XXX RtFileTrace.TraceMode(env);
            // DBTracing has been temporarily removed
            //XXX DBTracing.TraceMode(env);
        }
        vat.makeSturdyRoot("eRunner", eRunner);
        //XXX
        //vat.makeSturdyRoot("file.root",
        //                   EWritableDirectory.createRootCapability());
        //vat.makeSturdyRoot("net.root",
        //                   ENet.createRootCapability
        //                   (env, env.getPropertyAsInt("DefaultPort", 0)));
        //vat.makeSturdyRoot("registrar.root",
        //                   ERegistrar.createRootCapability(env));

        vat.init(env.getProperty("checkpoint"));
    }
}
