/**
 *  This is a totally bogus file to get Kari's circular shit working...
 */
package ec.e.hold;

import ec.cert.Certificate;
import ec.cert.CryptoHash;
import ec.cert.Verifier;
import ec.e.file.EStdio;
import ec.e.rep.ParimeterizedRepository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class Fulfiller implements DataRequestor {
    public Fulfiller() {
        EStdio.out().println("\nWarning Fulfiller class is coming from a dummy class\n");
    }

    public Fulfiller(ParimeterizedRepository repository, Object urlFetcher, Vector hints) {
        EStdio.out().println("\nWarning Fulfiller class is coming from a dummy class\n");
    }

    public void acceptData(Object data, DataRequestor yourRequestor){}
    public void acceptByteData(byte[] data, DataRequestor yourRequestor){}
    public void handleFailure(Exception failure,
                              DataRequestor yourRequestor){}
}

public class DataHolderSteward implements DataHolder, DataRequestor {
    public DataHolderSteward(CryptoHash cryptoHash, Fulfiller fulfiller, Hashtable certs) {
        EStdio.out().println("\nWarning DataHolderSteward class is coming from a dummy class\n");
    }
    public byte certifiedBy(Verifier verifier){return 0;}
    public void addCertificate(Certificate certificate){}
    public Object held() throws RepositoryKeyNotFoundException, IOException{return null;}
    public void giveDataTo(DataRequestor requestor){}
    public void acceptData(Object data, DataRequestor yourRequestor){}
    public void acceptByteData(byte[] data, DataRequestor yourRequestor){}
    public void handleFailure(Exception failure, DataRequestor yourRequestor){}
    public CryptoHash getCryptohash() {return null;}
}
