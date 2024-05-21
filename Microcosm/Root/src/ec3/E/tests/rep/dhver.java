package ec.tests.rep;

import ec.ifc.app.*;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.tests.rep.Feline;
import ec.tests.rep.Canine;
import ec.e.file.*;
import ec.e.rep.*;
import ec.e.rep.steward.*;
import ec.e.rep.steward.CryptoHash;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import ec.cert.Certificate;
import ec.cert.Verifier;

/* 
   To run this test, type

   java ec.e.start.EBoot ec.tests.rep.dhver

   to a Windows bash shell.
   You could remove all inspector code if you wanted.

   */
 

eclass dhver implements ELaunchable {
    emethod go(EEnvironment env) {
        DataHolderVerificationTester tester = new DataHolderVerificationTester(env);
        tester <- go();
    }
}

eclass DataHolderVerificationTester implements DataRequestor {
    private EEnvironment myEnv;
    private StandardFulfiller theFulfiller;
    ECApplication app = null;

    public DataHolderVerificationTester(EEnvironment env) {
        myEnv = env;
    }

    private EDirectoryBase ensureDirectory(EDirectoryRootMaker rootMaker,
                                           EEditableDirectory homeDir,
                                           String path) 
      throws IOException {
        try {
            EDirectoryBase dir = homeDir.lookupDirectory(path);
            ec.e.file.EStdio.err().println("Found directory " + path);
            return dir;
        } catch (IOException iox) {
            ec.e.file.EStdio.err().println("Caught IOException - Attempting to create directory " + path);
            return homeDir.mkdir(path);
        }
    }

    emethod go() {
        Vat vat = myEnv.vat();
        DataHolder edh;

        // To run without inspector, comment out next two lines.

        app = new ECApplication(); // Create the IFC app
        ec.ui.IFCInspectorUI.initialize("full"); // Use IFC for Inspector UI

        try {
            EStdio.initialize(vat);
            EDirectoryRootMaker rootMaker = new EDirectoryRootMaker(vat);
            
            EEditableDirectory startUpDir = rootMaker.makeDirectoryRoot(".");
            EEditableDirectory ecHomeDir  = (EEditableDirectory)ensureDirectory
              (rootMaker, startUpDir, "ECHome");
            EDirectoryBase     cacheDir   = ensureDirectory(rootMaker, ecHomeDir, "Cache");
            EDirectoryBase     extrasDir  = ensureDirectory(rootMaker, ecHomeDir, "Extras");
            EDirectoryBase     distDir    = ensureDirectory(rootMaker, ecHomeDir, "Dist");

            EEditableFile anEditFile = ((EEditableDirectory)distDir).mkfile("Repository");

            System.out.println("Opening repository");
            Repository rep = new Repository(anEditFile);

            theFulfiller = new StandardFulfiller(myEnv,rootMaker);
            ec.e.inspect.Inspector.gather(theFulfiller, "Fulfiller");

            Hashtable symbolTable = (Hashtable)theFulfiller.getFromRepository("%SymbolTable%");
            ec.e.inspect.Inspector.gather(symbolTable, "Symbol Table");

            Hashtable certificateTable = (Hashtable)theFulfiller.getFromRepository("certificates");
            ec.e.inspect.Inspector.gather(certificateTable, "Certificates");

            Vector simulatedRegion = new Vector(100);

            Enumeration e = symbolTable.keys();

            while (e.hasMoreElements()) {
                Object symbol = e.nextElement();
                Object cryptoHash = symbolTable.get(symbol);
                Object certificates = certificateTable.get(cryptoHash);
                if (certificates != null) {
                    edh = new DataHolder(vat,null,(CryptoHash)cryptoHash,
                                                                    theFulfiller,(Hashtable)certificates);
                    simulatedRegion.addElement(edh);
                }
            }

            Verifier verifier = null;

            e = certificateTable.elements();
            while (e.hasMoreElements()) {
                Hashtable oCerts = (Hashtable)e.nextElement();
                Enumeration e2 = oCerts.keys();
                while (e2.hasMoreElements()) {
                    Object o = e2.nextElement();
                    if (o instanceof Verifier) {
                        verifier = (Verifier)o;
                        break;
                    }
                }
            }

            System.out.println("Verifier is " + verifier);

            DataHolder x1 = (DataHolder)simulatedRegion.elementAt(0);

            e = simulatedRegion.elements();

            while (e.hasMoreElements()) {
                edh = (DataHolder)e.nextElement();
                if (edh.certifiedBy(verifier)) {
                    System.out.println(edh + " is certified by " + verifier);
                } else {
                    System.out.println(edh + " is not certified by " + verifier);
                }
            }

            ec.e.inspect.Inspector.gather(simulatedRegion,"Simulated Region Vector");

            //      edh.giveDataTo(this);

            ec.e.inspect.Inspector.inspect(null,null); // Force inspector window to appear

            e = simulatedRegion.elements();

            System.out.println("Start timer!");
            long startTime = System.currentTimeMillis();

            while (e.hasMoreElements()) {
                edh = (DataHolder)e.nextElement();
                Object dummy = edh.held();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Stop timer");

            int totalBytes = 0;

            e = simulatedRegion.elements();

            while (e.hasMoreElements()) {
                edh = (DataHolder)e.nextElement();
                totalBytes += ((byte[])(edh.held())).length;
            }

            System.out.println("We read in " + simulatedRegion.size() +
                               " items, a total of " + totalBytes + " bytes, in " +
                               (endTime - startTime) + " ms");

            if (app != null) app.run();

        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }

    local void handleFailure(Exception exception, DataHolder holder) {
        ec.e.inspect.Inspector.gather(exception, "handlefailure exception");
        System.out.println("HandleFailure called:" + exception);
    }

    local void acceptData(Object object, DataHolder holder) {
        ec.e.inspect.Inspector.gather(object, "acceptdata result");
        System.out.println("acceptData called with " + object);
    }
}
