package ec.e.rep;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import ec.cert.CryptoHash;
import ec.e.start.EEnvironment;
import ec.e.hold.DataRequestor;
import ec.e.net.SturdyRef;

public class StandardRepository {
    static {
        throw new ExceptionInInitializerError("Loaded dummy StandardRepository class");
    }
    public StandardRepository(EEnvironment env) {
        throw new RuntimeException("Dummy StandardRepository Constructor called - Please recompile e/runtime/e/rep");
    }
}

public class ParimeterizedRepository {
    static {
        throw new ExceptionInInitializerError("Loaded dummy ParimeterizedRepository class");
    }
    public ParimeterizedRepository(EEnvironment env) {
        throw new RuntimeException("Dummy ParimeterizedRepository Constructor called - Please recompile e/runtime/e/rep");
    }
    public Object get(Object key, Hashtable parimeterArguments) throws IOException {
        throw new RuntimeException("Dummy ParimeterizedRepository.get called - Please recompile e/runtime/e/rep");
    }
    
    public Object get(Object key) throws IOException {
        throw new RuntimeException("Dummy ParimeterizedRepository.get called - Please recompile e/runtime/e/rep");
    }
    
    public void requestByteRetrieval(CryptoHash hash, Vector myHints, DataRequestor requestor) {
        throw new RuntimeException("Dummy ParimeterizedRepository.requestByteRetrieval called - Please recompile e/runtime/e/rep");
    }

    public boolean isPublished(Object key) {
        throw new RuntimeException("Dummy ParimeterizedRepository.isPublished called - Please recompile e/runtime/e/rep");
    }

    public Object getCryptoHash(Object symbol) {
        throw new RuntimeException("Dummy ParimeterizedRepository.getCryptoHash called - Please recompile e/runtime/e/rep");
    }
    public CryptoHash putHashInCacheRepository(Object object) throws IOException {
        throw new RuntimeException("Dummy ParimeterizedRepository.putHashInCacheRepository called - Please recompile e/runtime/e/rep");
    }
}

public class RepositoryPublisher {
    static {
        throw new ExceptionInInitializerError("Loaded dummy RepositoryPublisher class");
    }
    public RepositoryPublisher(EEnvironment env) {
        throw new RuntimeException("Dummy RepositoryPublisher Constructor called - Please recompile e/runtime/e/rep");
    }
    public byte[] getBytes(Object key) throws IOException {
        throw new RuntimeException("Dummy RepositoryPublisher Constructor called - Please recompile e/runtime/e/rep");
    }
    public SturdyRef getSturdyRef() {
        throw new RuntimeException("Dummy RepositoryPublisher.getSurdyRef called - Please recompile e/runtime/e/rep");
    }
}

public class PublishRepository {
    static {
        throw new ExceptionInInitializerError("Loaded dummy PublishRepository class");
    }
    public PublishRepository(EEnvironment env) {
        throw new RuntimeException("Dummy PublishRepository Constructor called - Please recompile e/runtime/e/rep");
    }
}
