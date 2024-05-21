package ec.e.net;

import ec.e.file.EReadableFile;
import ec.e.file.EEditableFile;
import ec.e.file.EEditableDirectory;
import ec.e.file.EDirectoryRootMaker;
import ec.e.start.EEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

public final class SturdyRefFileImporter {
    static private final Trace tr = new Trace("ec.e.net.SturdyRefFileImporter");
    
    private Registrar myRegistrar;
    private EEnvironment myEnv;
    private EDirectoryRootMaker myRootFileMaker;

    private SturdyRefFileImporter() {}
    
    SturdyRefFileImporter(Registrar registrar, EEnvironment env) {
        if (registrar == null || env == null) {
            throw new SecurityException("Need real Registrar/EEnvironment to make SturdyRefFileImporter");
        }
        myRegistrar = registrar;
        myEnv = env;
    }

    public SturdyRef importRef(InputStream instream) throws IOException, InvalidURLException {
        DataInputStream is = new DataInputStream(instream);
        String newurl = is.readUTF();
        if (tr.debug && Trace.ON) tr.debugm("imported url=" + newurl);
        EARL earl = new EARL(newurl);
        return new SturdyRef(myRegistrar, earl.searchPath(), earl.registrarID(), earl.objectID());
    }

    public SturdyRef importRef(EReadableFile file) throws IOException, InvalidURLException {
        InputStream instream = file.inputStream();
        SturdyRef ref = null;
        
        try {
            ref = importRef(instream);
        }
        finally {
            instream.close();
        }
        return ref;
    }

    public SturdyRef importRef(String filename) throws IOException, InvalidURLException {
        if (myRootFileMaker == null) {
            try {
                myRootFileMaker = EDirectoryRootMaker.summon(myEnv);
            }
            catch (Exception e) {
                throw new IOException("can't make root directory object: " + e);
            }
        }
        EEditableFile file = myRootFileMaker.makeEditableFile(filename);
        if (tr.debug && Trace.ON) tr.debugm("importing from file=" + filename);
        return importRef(file.asReadableFile());
    }
}

