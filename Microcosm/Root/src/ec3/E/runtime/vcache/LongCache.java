package ec.vcache;

public final class LongCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    private LongCache() {
    }

    static public Long toObject(long val) {
        if (VCache.KEEP_STATS) {
            TheMisses++;
            TheChurns++;
        }
        return new Long(val);
    }
}
