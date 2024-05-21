package ec.e.rep;

import java.util.Vector;
import java.io.IOException;
import ec.e.file.*;

public class ReadOnlyRepository extends Repository {

    public ReadOnlyRepository(Vector files) throws IOException {
        super(files);
    }

    public ReadOnlyRepository(EDirectoryBase repositoryDir, EEditableFile firstFile) 
         throws IOException {
        super(repositoryDir, firstFile);
    }

    public ReadOnlyRepository(EReadableFile aFile) throws IOException {
        super(aFile);
    }

    public ReadOnlyRepository(EFileReader aFile) throws IOException {
             super(aFile);
    }

    public ReadOnlyRepository(EFileEditor aFile)  throws IOException {
        super(aFile);
    }

    public boolean isWriteable() {
        return false;
    }
}
