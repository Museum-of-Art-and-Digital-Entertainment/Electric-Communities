package ec.e.net;

/**
 * An E language, lookup-only interface to the registrar.
 */
public eclass EDirectoryServer {
    private Registrar myRegistrar;

    /**
     * Wrap an EDirectoryServer object around a Registrar object.
     *
     * @param registrar The Registrar being wrapped.
     */
    EDirectoryServer(Registrar registrar) {
        myRegistrar = registrar;
    }

    /**
     * Lookup a local EObject by its object ID.
     *
     * @param objectID The object ID of the object desired.
     * @param resultDist A distributor which will be forwarded to the
     *  registered object, if such an object exists.
     */
    emethod lookupObjectID(String objectID, EDistributor resultDist) {
        myRegistrar.lookupObjectID(objectID, resultDist);
    }

    /**
     * Lookup an EObject by URL.
     *
     * @param url The URL of the object desired.
     * @param resultDist A distributor which will be forwarded to the object,
     *  if such an object is found.
     */
    emethod lookupURL(String url, EDistributor resultDist) {
        myRegistrar.lookupURL(url, resultDist);
    }
}

/**
 * E exception for lookup problems in the Registrar.
 */
public class RegistrarLookupEException extends RtEException {
    public RegistrarLookupEException(String message) { super(message); }
}
