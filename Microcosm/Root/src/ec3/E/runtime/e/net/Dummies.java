package ec.e.net;

import java.io.IOException;
import ec.e.net.steward.Proxy;

public class PktConnection
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy PktConnection class");
    }
}

public class Registrar {
    static {
        throw new ExceptionInInitializerError("Loaded dummy Registrar class");
    }

    public String statistics() {
        throw new RuntimeException("Dummy class should be overwritten");
    }
}

public class ProxyInterest {
    static {
        throw new ExceptionInInitializerError("Loaded dummy ProxyInterest class");
    }

    public ProxyInterest(Proxy proxy, ProxyDeathHandler handler, Object data) {
    }
}

public interface ProxyDeathHandler {
}

public class EConnection {
    static {
        throw new ExceptionInInitializerError("Loaded dummy EConnection class");
    }
    public Object vatLock() { return null; }
    public void dgcSuspectTrash(long id, int referenceCount) {}
    public void dgcWRemoveMe(long id) {}    
    public ESender sender() { return null; }
    public void registerInterestInProxy (ProxyInterest interest, long identity) {}
    public void unregisterInterestInProxy (ProxyInterest interest) {}
}


public class ESender {
    static {
        throw new ExceptionInInitializerError("Loaded dummy ESender class");
    }
    public void sendEnvelope(Object dest, long told, RtEnvelope env) throws IOException {}
}
