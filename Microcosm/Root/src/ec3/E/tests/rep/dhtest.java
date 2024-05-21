package ec.tests.rep;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.tests.rep.Feline;
import ec.e.file.EStdio;
import ec.tests.rep.Canine;
import ec.e.rep.steward.Repository;
import ec.e.hold.DataHolder;
import ec.e.hold.DataHolderSteward;
import ec.cert.CryptoHash;
import ec.e.hold.DataRequestor;
import ec.e.rep.StandardRepository;
import ec.e.rep.steward.SimpleRepository;
import ec.e.rep.ParimeterizedRepository;

import ec.e.hold.Fulfiller;
import ec.e.rep.steward.RepositoryKeyNotFoundException;

/* 
   To run this test, type

   java ec.e.start.EBoot ec.tests.rep.dhtest

   to a Windows shell of some kind.

   */
 

eclass dhtest implements ELaunchable {
    emethod go(EEnvironment env) {
        DataHolderTester tester = new DataHolderTester(env);
        tester <- go();
    }
}

eclass DataHolderTester implements DataRequestor {
    private EEnvironment myEnv;
    private Fulfiller theFulfiller;
    
    public DataHolderTester(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {
        Vat vat = myEnv.vat();
        try {
            
            ParimeterizedRepository rep = (ParimeterizedRepository)ParimeterizedRepository.summon(myEnv);
            Vat testvat = null;

            Fulfiller theFulfiller = new Fulfiller(rep,null,null);

            // First retrieve a known object from the StandardRepository

            Hashtable symbols = (Hashtable)rep.get("%SymbolTable%");
            CryptoHash hash = (CryptoHash)symbols.get("test.bmp");

            byte[] bytes = (byte[]) theFulfiller.getFromRepository(hash);

            // Create an empty DataHolder to test asynchronous repository retrieval

            CryptoHash byteHash = Repository.computeCryptoHash(bytes);
            if (! byteHash.equals(hash)) 
                EStdio.err().println("[FAILURE] CRHMISM - Cryptohashes don't match!");

            DataHolder edh = new DataHolderSteward(byteHash,theFulfiller,null);

            byte[] bytes2 = (byte[])edh.held();            

            // Request the data bytes to be retrieved asynchronously if need be.

            edh.giveDataTo(this);

        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }

    local void handleFailure(Exception exception, DataHolder holder) {
        System.out.println("HandleFailure called:" + exception);
    }

    local void acceptData(Object object, DataHolder holder) {
        System.out.println("acceptData called with " + object);
    }

    local void acceptByteData(byte[] data, DataHolder holder) {
        System.out.println("acceptByteData called with " + data);
    }
}
