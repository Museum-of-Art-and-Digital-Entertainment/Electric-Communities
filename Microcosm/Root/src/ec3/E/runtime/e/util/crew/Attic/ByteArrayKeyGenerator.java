package ec.e.util.crew;

import java.security.Key;
import sun.security.provider.SecretKey;


public class PassphraseKeyGenerator 
{
    static public Key generateKey(byte[] key, String algorythm) {
        return new SecretKey(key, algorythm);
    }
}
