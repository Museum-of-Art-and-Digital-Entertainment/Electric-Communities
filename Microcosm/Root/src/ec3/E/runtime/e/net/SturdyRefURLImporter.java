package ec.e.net;

import ec.util.NestedException;
import ec.e.file.URLStreamMaker;
import ec.e.start.EEnvironment;
import java.io.IOException;
import java.io.DataInputStream;

public final class SturdyRefURLImporter {
    static private final Trace tr = new Trace("ec.e.net.SturdyRefURLImporter");
    
    private Registrar myRegistrar;
    private EEnvironment myEnv;
    private URLStreamMaker myURLStreamMaker;

    private SturdyRefURLImporter() {}
    
    SturdyRefURLImporter(Registrar registrar, EEnvironment env) {
        if (registrar == null || env == null) {
            throw new SecurityException("Need real Registrar/EEnvironment to make SturdyRefFileImporter");
        }
        myRegistrar = registrar;
        myEnv = env;
    }

    public SturdyRef importRef(String url) throws IOException, InvalidURLException {
        if (tr.debug && Trace.ON) tr.debugm("importing from url=" + url);
        if (myURLStreamMaker == null) {
            try {
                myURLStreamMaker = URLStreamMaker.summon(myEnv);
            }
            catch (Exception e) {
                throw new NestedException("can't make URLStreamMaker object", e);
            }
        }
        try {
            DataInputStream is = new DataInputStream(myURLStreamMaker.getInputStream(url));
            String newurl = is.readUTF();
            is.close();
            if (tr.debug && Trace.ON) tr.debugm("imported earl=" + newurl);
            EARL earl = new EARL(newurl);
            return new SturdyRef(myRegistrar, earl.searchPath(), earl.registrarID(), earl.objectID());
        }
        catch (IOException e) {
            throw e;
        }
        // should be java.net.MalformedURLException, but we can't see that in the Vat.
        catch (Exception e) {
            throw new InvalidURLException(url, e);
        }
    }
}


