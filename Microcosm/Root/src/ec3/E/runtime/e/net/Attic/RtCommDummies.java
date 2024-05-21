package ec.e.net;

import java.io.OutputStream;
import java.io.IOException;

public class EConnection {
    public void dgcSuspectTrash(long id, int referenceCount) {}
    public void dgcWRemoveMe(long id) {}    
    public void sendEnvelope(long told, RtEnvelope env) {}
    public ESender sender() { return null; }
}

public class ESender {}
