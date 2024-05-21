package ec.vcache;

public final class IntCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    static private final int RANGE_MIN = -25;
    static private final int RANGE_MAX = 200;
    static private final int CACHE_SIZE = 1171; // NOTE: must be a prime

    static private Integer[] TheRange = new Integer[RANGE_MAX - RANGE_MIN + 1];
    static private int[] CacheKey1 = new int[CACHE_SIZE];
    static private int[] CacheKey2 = new int[CACHE_SIZE];
    static private Integer[] CacheObj1 = new Integer[CACHE_SIZE];
    static private Integer[] CacheObj2 = new Integer[CACHE_SIZE];
    static private boolean StoreIn1 = false;

    private IntCache() {
    }

    static public Integer toObject(int val) {
        if (VCache.KEEP_STATS) {
            TheHits++;
        }
        if ((val >= RANGE_MIN) && (val <= RANGE_MAX)) {
            int index = val - RANGE_MIN;
            if (TheRange[index] == null) {
                TheRange[index] = new Integer(val);
                if (VCache.KEEP_STATS) {
                    TheHits--;
                    TheMisses++;
                }
            }
            return TheRange[index];
        }

        int index = (val & 0x7fffffff) % CACHE_SIZE;
        if (CacheKey1[index] == val) {
            return CacheObj1[index];
        }
        if (CacheKey2[index] == val) {
            return CacheObj2[index];
        }

        if (StoreIn1) {
            if (CacheKey1[index] != 0) {
                StoreIn1 = false;
            }
        } else {
            if (CacheKey2[index] != 0) {
                StoreIn1 = true;
            }
        }

        Integer result = new Integer(val);
        if (StoreIn1) {
            if (VCache.KEEP_STATS && (CacheKey1[index] != 0)) {
                TheChurns++;
            }
            CacheKey1[index] = val;
            CacheObj1[index] = result;
        } else {
            if (VCache.KEEP_STATS && (CacheKey2[index] != 0)) {
                TheChurns++;
            }
            CacheKey2[index] = val;
            CacheObj2[index] = result;
        }

        if (VCache.KEEP_STATS) {
            TheHits--;
            TheMisses++;
        }

        return result;
    }
}
