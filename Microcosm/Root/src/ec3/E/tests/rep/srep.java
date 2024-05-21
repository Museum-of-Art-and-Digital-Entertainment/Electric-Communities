package ec.tests.rep;

import ec.cert.CryptoHash;
import ec.ifc.app.*;
import java.io.IOException;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.file.EStdio;
import ec.e.rep.StandardRepository;
import ec.e.rep.steward.SuperRepository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.run.Trace;
import ec.e.start.crew.CrewCapabilities;

/* To run this test, type to a shell:
   java ec.e.start.EBoot ec.tests.rep.srep */

eclass srep implements ELaunchable {
    emethod go(EEnvironment env) {
        StandardRepositoryTester tester = new StandardRepositoryTester(env);
        tester <- go();
    }
}

eclass StandardRepositoryTester {
    private EEnvironment myEnv;

    public StandardRepositoryTester(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {
        try {
            EStdio.err().println("Calling summon");
            StandardRepository.summon(myEnv); 

            SuperRepository theRep = 
                (SuperRepository)CrewCapabilities.getTheSuperRepository();
            Object cryptoKey = theRep.getCryptoHash("test.bmp");
            if (cryptoKey == null) {
                throw new RepositoryKeyNotFoundException("System object named '" +
                                                         "test.bmp" + "' not in symbol table");
            }

            EStdio.err().println("Calling get");
            Object bits = theRep.get((CryptoHash)cryptoKey);       // Get the data
            EStdio.err().println("done");

            if (bits == null) EStdio.err().println("[FAILURE] BITSNUL - Bytes are null");
            else EStdio.err().println("[SUCCESS] BITSOK - bytes retrieved");
        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}
