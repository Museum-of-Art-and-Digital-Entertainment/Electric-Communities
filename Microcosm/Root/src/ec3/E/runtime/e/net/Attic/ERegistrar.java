package ec.e.net;

import ec.e.start.EEnvironment;
import ec.e.start.MagicPowerMaker;

public class ERegistrarMaker implements MagicPowerMaker {
    public ERegistrarMaker() {
    }

    public Object make(EEnvironment env) {
        return (Object) new ERegistrar();
    }
}


public class ERegistrar {
    ERegistration register(EObject obj) throws ERegistrationException {
        throw new RuntimeException("unimplemented");
    }

    void lookup(String url, EDistributor resultDist) {
        throw new RuntimeException("unimplemented");
    }
}

public class ERegistration {
    public void unRegister() throws ERegistrationException {
        throw new RuntimeException("unimplemented");
    }

    public String getURL() {
        throw new RuntimeException("unimplemented");
    }
}

public class ERegistrationException extends Exception {
    public ERegistrationException(String message) { super(message); }
}
