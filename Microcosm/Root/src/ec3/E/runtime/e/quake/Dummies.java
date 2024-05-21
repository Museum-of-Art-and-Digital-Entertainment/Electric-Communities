package ec.e.quake;

import java.util.Properties;
import java.io.IOException;
import ec.e.run.OnceOnlyException;

public class StableStore 
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy StableStore class");
    }

    public static boolean checkPassphrase(String filename, String passphrase) {
        throw new Error("I'm a dummy, don't call me!");
    }
    
    public static boolean exists(String filename) throws IOException {
        throw new Error("I'm a dummy, don't call me!");
    }
}

public class Revive 
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Revive class");
    }

    static public void doRevival(String filename, String passphrase, String args[], Properties props) throws IOException, OnceOnlyException {
        throw new Error("I'm a dummy, don't call me!");
    }
}
