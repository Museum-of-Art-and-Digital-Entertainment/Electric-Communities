package ec.e.rep;

import java.util.Vector;
import java.io.IOException;
import ec.e.file.*;

public class CacheRepository extends Repository {

    public CacheRepository(Vector files) throws IOException {
        super(files);
    }

    public CacheRepository(EDirectoryBase repositoryDir, EEditableFile firstFile)
         throws IOException {
        super(repositoryDir, firstFile);
    }
}
