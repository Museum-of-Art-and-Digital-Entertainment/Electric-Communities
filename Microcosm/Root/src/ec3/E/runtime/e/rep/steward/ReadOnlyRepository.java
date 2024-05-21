package ec.e.rep.steward;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class ReadOnlyRepository extends Repository {

    public ReadOnlyRepository(String[] files) throws IOException {
        super(files);
    }

    public ReadOnlyRepository(File repositoryDir, File firstFile) throws IOException {
        super(repositoryDir, firstFile);
    }

    public ReadOnlyRepository(File aFile) throws IOException {
        super(aFile);
    }

    public boolean isWriteable() {
        return false;
    }
}
