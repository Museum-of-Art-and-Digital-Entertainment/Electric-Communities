package ec.e.rep.steward;

import ec.cert.CryptoHash;

public class Repository 
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Repository class");
    }
    public static CryptoHash computeCryptoHash(Object data) {
        throw new RuntimeException("Dummy Repository.computeCryptoHash called - Please recompile e/runtime/e/rep/steward");
    }
}
