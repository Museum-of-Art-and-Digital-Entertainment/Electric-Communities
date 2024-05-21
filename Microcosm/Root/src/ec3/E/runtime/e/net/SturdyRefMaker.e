package ec.e.net;

public class SturdyRefMaker {
    private static final Trace tr = new Trace("ec.e.net.SturdyRefMaker");
    private Registrar myRegistrar;

    private SturdyRefMaker() {}
    
    SturdyRefMaker(Registrar registrar) {
        myRegistrar = registrar;
    }

    public SturdyRef makeSturdyRef(Object obj) {
      if (obj instanceof Exportable) {
        Exportable toExport = (Exportable)obj;
        // XXX need some way to get the expiration date
        String searchPath[] = myRegistrar.searchPath();
        String regID = myRegistrar.registrarID();
        String objectID = myRegistrar.register(toExport);
        if (tr.debug) tr.debugm("creating new SturdyRef <e://" + searchPath + "/" + regID + "/" + objectID + "> for EObject: " + toExport);
        if (tr.debug && Trace.ON) tr.debugm("creating new SturdyRef <e://" + searchPath + "/" + regID + "/" + objectID + "> for EObject: " + obj);
        return new SturdyRef(myRegistrar, searchPath, regID, objectID);
      } else {
        // XXX Should I throw and exception here?
        return null;
      }
    }

    public RtForwardingSturdyRef makeForwardingSturdyRef(EObject obj) {
        return new RtForwardingSturdyRef(myRegistrar, obj);
    }
}
