package ec.security;

import java.security.Key;

public class DESKey implements Key 
{
    private byte myKey[];
    
    public DESKey(byte key[]) {
        myKey = key;
    }

    public byte[] getBytes() {
        return myKey;
    }

    public String getAlgorithm() {
        return "DES";
    }
    
    public String getFormat() {
        return null;
    }
    
    public byte[] getEncoded() {
        return null;
    }
}
