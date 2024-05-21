package ec.e.net;

/**
 * An E language, lookup-only interface to the registrar.
 */
public eclass SturdyRefFollower implements SturdyRefFollowerKludge {
    private Registrar myRegistrar;

    private SturdyRefFollower() {}

    /**
     * Wrap a SturdyRefFollower object around a Registrar object.
     *
     * @param registrar The Registrar being wrapped.
     */
    SturdyRefFollower(Registrar registrar) {
        myRegistrar = registrar;
    }

    /**
     * Lookup a local EObject by its object ID.
     *
     * @param objectID The object ID of the object desired.
     * @param resultDist A distributor which will be forwarded to the
     *  registered object, if such an object exists.
     */
    emethod lookupObjectID(String objectID, EResult resultDist) {
        myRegistrar.lookupObjectID(objectID, resultDist);
    }
    
    /**
     * Send an envelope to a local EObject by its object ID.
     * XXX PESSIMISM This would not be necessary if channels were properly
     * optimistic.
     * @param objectID The object ID of the object desired.
     * @param env An envelope which will be delivered to the registered
     *  object, if such an object exists.
     */
    emethod sendToObjectID(String objectID, RtEnvelope env) {
        myRegistrar.sendToObjectID(objectID, env);
    }

    /**
       Return a holder from which another member of this package can
       extract our Registrar.  You shouldn't be able to call this,
       much less use it, if you're outside the ec.e.net package.
    */
    local RegistrarHolder getRegistrarHolder() {
        return new RegistrarHolder(myRegistrar);
    }
}

public interface SturdyRefFollowerKludge {
    RegistrarHolder getRegistrarHolder();
}

class RegistrarHolder {
    private Registrar myRegistrar;

    private RegistrarHolder() {}
    
    RegistrarHolder(Registrar registrar) {
        myRegistrar = registrar;
    }

    /* package */ Registrar held() {
        return myRegistrar;
    }
}

/**
 * E exception for lookup problems in the Registrar.
 */
public class RegistrarLookupEException extends RuntimeException {
    public RegistrarLookupEException(String message) { super(message); }
}
