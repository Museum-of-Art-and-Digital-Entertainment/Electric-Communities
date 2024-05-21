package ec.e.net.steward;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import sun.security.provider.DSAPublicKey;

public class MakeDSAPublicKey {

    static public PublicKey make(byte[] derKey) throws InvalidKeyException {
        return new DSAPublicKey(derKey);
    }

}
