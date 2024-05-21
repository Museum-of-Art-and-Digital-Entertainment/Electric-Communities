package ec.vcache;

public final class DoubleCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    private DoubleCache() {
    }

    static public Double toObject(double val) {
        if (VCache.KEEP_STATS) {
            TheMisses++;
            TheChurns++;
        }
        return new Double(val);
    }
}
