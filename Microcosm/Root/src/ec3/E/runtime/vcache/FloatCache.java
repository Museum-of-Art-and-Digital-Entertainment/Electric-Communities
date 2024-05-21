package ec.vcache;

public final class FloatCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    static private final int CACHE_SIZE = 1171; // NOTE: must be a prime

    static private float[] CacheKey1 = new float[CACHE_SIZE];
    static private float[] CacheKey2 = new float[CACHE_SIZE];
    static private Float[] CacheObj1 = new Float[CACHE_SIZE];
    static private Float[] CacheObj2 = new Float[CACHE_SIZE];
    static private boolean StoreIn1 = false;

    static private final Float ThePosInf = new Float(Float.POSITIVE_INFINITY);
    static private final Float TheNegInf = new Float(Float.NEGATIVE_INFINITY);
    static private final Float TheNaN = new Float(Float.NaN);
    static private final Float TheZero = new Float(0.0F);
    static private final Float TheOne = new Float(1.0F);
    static private final Float TheNegOne = new Float(-1.0F);
 
    private FloatCache() {
    }

    static public Float toObject(float val) {
        if (VCache.KEEP_STATS) {
            TheHits++;
        }

        if (val == 0.0F) {
            return TheZero;
        } else if (val == 1.0F) {
            return TheOne;
        } else if (val == -1.0F) {
            return TheNegOne;
        } else if (val == Float.POSITIVE_INFINITY) {
            return ThePosInf;
        } else if (val == Float.NEGATIVE_INFINITY) {
            return TheNegInf;
        } else if (val == Float.NaN) {
            return TheNaN;
        }

        int index = (Float.floatToIntBits(val) & 0x7fffffff) % CACHE_SIZE;
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

        Float result = new Float(val);
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
