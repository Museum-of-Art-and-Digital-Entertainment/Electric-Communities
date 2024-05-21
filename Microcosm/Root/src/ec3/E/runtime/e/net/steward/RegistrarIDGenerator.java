package ec.e.net.steward;

import ec.util.NestedException;
import ec.e.start.EEnvironment;
import ec.security.ECSecureRandom;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class RegistrarIDGenerator {
    private static final Trace tr = new Trace("ec.e.net.RegistrarIDGenerator");
    
    private RegistrarIDGenerator() { }

    static public KeyPair generateRegistrarKeyPair(EEnvironment env) {
        String IdKeyStrength = env.getProperty("IdKeyStrength");
        int strength = 0;
        if (IdKeyStrength != null) {
            try {
                strength = Integer.parseInt(IdKeyStrength);
            }
            catch (NumberFormatException e) {
                tr.verbosem("couldn't parse property IdKeyStrength: " + IdKeyStrength, e);
            }
        }
        if (strength != 512 && strength != 768) {
            strength = 1024;
        }
        
        ECSecureRandom sr = ECSecureRandom.summon(env);
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("DSA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new NestedException("Can't load crypto routines for DSA", e);
        }
        
        gen.initialize(strength, sr);
        return gen.generateKeyPair();
    }

    static public String calculateRegistrarID(PublicKey key) {

        byte encoding[] = key.getEncoded();
        MessageDigest sha = null; // javac doesn't know startupError always throws
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch(NoSuchAlgorithmException e) {
            throw new NestedException("Can't load crypto routines for SHA", e);
        }
        BigInteger hash = new BigInteger(1, sha.digest(encoding));
        String rid = hash.toString(36);
        
        if (tr.debug && Trace.ON) tr.debugm("calculated registrar Id " + rid + " from PublicKey " + key);
        return rid;
    }
}
        

