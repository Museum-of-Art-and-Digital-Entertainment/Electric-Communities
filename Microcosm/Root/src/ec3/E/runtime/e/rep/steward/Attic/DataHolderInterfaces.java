package ec.e.rep.steward;
import ec.cert.CryptoHash;
import java.io.IOException;
import ec.cert.Verifier;
import ec.cert.Certificate;

/**

 * DataHolder is the interface implemented by DataHolderStewards.  We
 * don't anticipate needing other classes besides DataHolderSteward
 * but these structures are so intertwined we need to break this one
 * out as an interface to break compiler dependency loops.

 */

public interface DataHolder {
    public byte certifiedBy(Verifier verifier);
    public void addCertificate(Certificate certificate);
    public Object held() throws RepositoryKeyNotFoundException, IOException;
    public void giveDataTo(DataRequestor requestor);
    void acceptData(Object data, DataHolder yourHolder);
    void handleFailure(Exception failure, DataHolder yourHolder);
    public CryptoHash getCryptohash();
}

/**

 * Anyone using DataHolders needs to define a callback object that
 * implements the DataRequestor interface. It will get called with the
 * data when the data becomes available. <p>

 * If there is a problem, including timeout, then the handleFailure
 * method gets called with an Exception object.

 * XXX yourHolder is really a DataHolder but I cannot declare them as
 * such since it causes a circular dependency. This should be fixed.

 */

public interface DataRequestor {
    void acceptData(Object data, DataHolder yourHolder);
    void handleFailure(Exception failure, DataHolder yourHolder);
}

