package ec.vcache;

public final class BooleanCache
{
    static int TheHits = 0;
    static int TheMisses = 0;
    static int TheChurns = 0;

    static Boolean TheFalse = Boolean.FALSE;
    static Boolean TheTrue = Boolean.TRUE;

    private BooleanCache() {
    }

    static public Boolean toObject(boolean b) {
        if (VCache.KEEP_STATS) {
            TheHits++;
        }
        return b ? TheTrue : TheFalse;
    }
}
