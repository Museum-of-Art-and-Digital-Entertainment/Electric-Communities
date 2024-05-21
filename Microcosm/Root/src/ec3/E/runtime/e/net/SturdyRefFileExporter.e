package ec.e.net;

import ec.e.file.EEditableFile;
import ec.e.file.EEditableDirectory;
import ec.e.file.EDirectoryRootMaker;
import ec.e.start.EEnvironment;
import java.io.IOException;
import java.io.DataOutputStream;

public final class SturdyRefFileExporter {
    static private final Trace tr = new Trace("ec.e.net.SturdyRefFileExporter");
    
    private EEnvironment myEnv;
    private EDirectoryRootMaker myRootFileMaker;

    private SturdyRefFileExporter() {}
    
    SturdyRefFileExporter(Registrar registrar, EEnvironment env) {
        if (registrar == null || env == null) {
            throw new SecurityException("Need real Registrar/EEnvironment to make SturdyRefFileExporter");
        }
        myEnv = env;
    }

    public void exportRef(SturdyRef ref, EEditableFile file) throws IOException {
        DataOutputStream os = new DataOutputStream(file.outputStream());
        String url = (new EARL(ref.myRemoteSearchPath, ref.myRemoteRID, ref.myRemoteObjectID, null)).url();
        os.writeUTF(url);
        os.close();
        if (tr.debug && Trace.ON) tr.debugm("exported url=" + url);
    }

    public void exportRef(SturdyRef ref, String filename) throws IOException {
        if (myRootFileMaker == null) {
            try {
                myRootFileMaker = EDirectoryRootMaker.summon(myEnv);
            }
            catch (Exception e) {
                throw new IOException("can't make root directory object: " + e);
            }
        }
        EEditableFile file = myRootFileMaker.makeEditableFile(filename);
        if (tr.debug && Trace.ON) tr.debugm("exporting to file=" + filename);
        exportRef(ref, file);
    }
}
