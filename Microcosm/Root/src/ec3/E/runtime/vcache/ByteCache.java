package ec.vcache;

public final class ByteCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    static Byte[] TheBytes = new Byte[256];

    private ByteCache() {
    }

    static public Byte toObject(byte b) {
        if (VCache.KEEP_STATS) {
            TheHits++;
        }
        int index = ((int) b) + 128;
        if (TheBytes[index] == null) {
            TheBytes[index] = new Byte(b);
            if (VCache.KEEP_STATS) {
                TheHits--;
                TheMisses++;
            }
        }
        return TheBytes[index];
    }
}
