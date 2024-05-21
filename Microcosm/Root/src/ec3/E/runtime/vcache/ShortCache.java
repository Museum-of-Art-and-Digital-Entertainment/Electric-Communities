package ec.vcache;

public final class ShortCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    private ShortCache() {
    }

    static public Short toObject(short val) {
        if (VCache.KEEP_STATS) {
            TheMisses++;
            TheChurns++;
        }
        return new Short(val);
    }
}
