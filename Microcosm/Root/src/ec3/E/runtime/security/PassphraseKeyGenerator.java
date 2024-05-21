//package ec.e.util.crew;
package ec.security;

import ec.util.NestedException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import sun.security.provider.SecretKey;

import ec.security.DESKey;

public class PassphraseKeyGenerator 
{
    static public Key generateKey(String passphrase, String algorythm) {
        byte dataBytes[] = passphrase.getBytes();
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //return new SecretKey(md.digest(dataBytes), algorythm);
            return new DESKey(md.digest(dataBytes));
        }
        catch (NoSuchAlgorithmException e) {
            throw new NestedException("Can't get MD5 crypto routines", e);
        }
    }
}
