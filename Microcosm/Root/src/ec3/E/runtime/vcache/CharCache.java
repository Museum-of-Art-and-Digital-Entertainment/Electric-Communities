package ec.vcache;

public final class CharCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    static private final char RANGE_MIN = 0;
    static private final char RANGE_MAX = 255;
    static private final int CACHE_SIZE = 1171; // NOTE: must be a prime

    static private Character[] TheChars = 
        new Character[RANGE_MAX - RANGE_MIN + 1];
    static private char[] CacheKey1 = new char[CACHE_SIZE];
    static private char[] CacheKey2 = new char[CACHE_SIZE];
    static private Character[] CacheObj1 = new Character[CACHE_SIZE];
    static private Character[] CacheObj2 = new Character[CACHE_SIZE];
    static private boolean StoreIn1 = false;

    private CharCache() {
    }

    static public Character toObject(char val) {
        if (VCache.KEEP_STATS) {
            TheHits++;
        }
        if ((val >= RANGE_MIN) && (val <= RANGE_MAX)) {
            int index = val - RANGE_MIN;
            if (TheChars[index] == null) {
                TheChars[index] = new Character(val);
                if (VCache.KEEP_STATS) {
                    TheHits--;
                    TheMisses++;
                }
            }
            return TheChars[index];
        }

        int index = val % CACHE_SIZE;
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

        Character result = new Character(val);
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
