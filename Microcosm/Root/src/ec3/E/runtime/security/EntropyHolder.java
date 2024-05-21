package ec.security;

public interface EntropyHolder
{
    public void holdEntropy(byte[] entropy, int bitEstimate);
}
