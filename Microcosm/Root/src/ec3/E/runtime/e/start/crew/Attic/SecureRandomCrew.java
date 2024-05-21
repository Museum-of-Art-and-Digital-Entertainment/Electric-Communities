// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.security.crew;

public class SecureRandomCrew {
    private static SecureRandomCrew theSecureRandomCrew = null;
    static public void provideEntropy(byte entropy[], int bitEstimate) {}
    static public void setMouseSeed(int x, int y, int type) {}
    static public void setKeySeed(int key, int modifiers, int type) {}
//    static public SecureRandomCrew getTheSecureRandomCrew(
//                    FragileRootHolder innerRandom,
//                    byte[] /*nilok*/ entropy,
//                    int bitEstimate) { return null; }
    public synchronized void setSeed(byte seed[]) {}
    public synchronized void setSeed(byte seed[], int entropy) {}
    public void nextBytes(byte bytes[]) {}
    public int availableEntropy() { return 0; }
}
