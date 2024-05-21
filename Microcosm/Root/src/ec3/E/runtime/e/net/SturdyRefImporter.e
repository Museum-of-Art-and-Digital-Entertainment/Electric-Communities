package ec.e.net;

public final class SturdyRefImporter {
    private Registrar myRegistrar;

    private SturdyRefImporter() {}
    
    SturdyRefImporter(Registrar registrar) {
        if (registrar == null) {
            throw new SecurityException("Need real registrar to make SturdyRefImporter");
        }
        myRegistrar = registrar;
    }

    public SturdyRef importRef(String url) throws InvalidURLException {
        EARL earl = new EARL(url);
        return new SturdyRef(myRegistrar, earl.searchPath(), earl.registrarID(), earl.objectID());
    }
}
