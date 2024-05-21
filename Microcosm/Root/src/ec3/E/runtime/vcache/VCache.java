package ec.vcache;

public final class VCache
{
    static final boolean DO_CACHING = true;
    static final boolean KEEP_STATS = true;

    private VCache() {
    }

    static public Object toObject(boolean b) {
        if (DO_CACHING) {
            return BooleanCache.toObject(b);
        } else {
            return new Boolean(b);
        }
    }

    static public Boolean toBoolean(boolean b) {
        if (DO_CACHING) {
            return BooleanCache.toObject(b);
        } else {
            return new Boolean(b);
        }
    }

    static public Object toObject(byte b) {
        if (DO_CACHING) {
            return ByteCache.toObject(b);
        } else {
            return new Byte(b);
        }
    }

    static public Byte toByte(byte b) {
        if (DO_CACHING) {
            return ByteCache.toObject(b);
        } else {
            return new Byte(b);
        }
    }

    static public Object toObject(char c) {
        if (DO_CACHING) {
            return CharCache.toObject(c);
        } else {
            return new Character(c);
        }
    }

    static public Character toCharacter(char c) {
        if (DO_CACHING) {
            return CharCache.toObject(c);
        } else {
            return new Character(c);
        }
    }

    static public Object toObject(short s) {
        if (DO_CACHING) {
            return ShortCache.toObject(s);
        } else {
            return new Short(s);
        }
    }

    static public Short toShort(short s) {
        if (DO_CACHING) {
            return ShortCache.toObject(s);
        } else {
            return new Short(s);
        }
    }

    static public Object toObject(int i) {
        if (DO_CACHING) {
            return IntCache.toObject(i);
        } else {
            return new Integer(i);
        }
    }

    static public Integer toInteger(int i) {
        if (DO_CACHING) {
            return IntCache.toObject(i);
        } else {
            return new Integer(i);
        }
    }

    static public Object toObject(long l) {
        if (DO_CACHING) {
            return LongCache.toObject(l);
        } else {
            return new Long(l);
        }
    }

    static public Long toLong(long l) {
        if (DO_CACHING) {
            return LongCache.toObject(l);
        } else {
            return new Long(l);
        }
    }

    static public Object toObject(float f) {
        if (DO_CACHING) {
            return FloatCache.toObject(f);
        } else {
            return new Float(f);
        }
    }

    static public Float toFloat(float f) {
        if (DO_CACHING) {
            return FloatCache.toObject(f);
        } else {
            return new Float(f);
        }
    }

    static public Object toObject(double d) {
        if (DO_CACHING) {
            return DoubleCache.toObject(d);
        } else {
            return new Double(d);
        }
    }

    static public Object toDouble(double d) {
        if (DO_CACHING) {
            return DoubleCache.toObject(d);
        } else {
            return new Double(d);
        }
    }

    /** needed to support an ecomp idiom */
    static public Object toObject(Object o) {
        return o;
    }

    static private int TheTotalHits = 0;
    static private int TheTotalMisses = 0;
    static private int TheTotalChurns = 0;

    static public void debugReport() {
        TheTotalHits = 0;
        TheTotalMisses = 0;
        TheTotalChurns = 0;
        System.err.println("VCache Statistics:");
        reportOne("Boolean", BooleanCache.TheHits, BooleanCache.TheMisses,
            BooleanCache.TheChurns);
        reportOne("Byte", ByteCache.TheHits, ByteCache.TheMisses,
            ByteCache.TheChurns);
        reportOne("Char", CharCache.TheHits, CharCache.TheMisses,
            CharCache.TheChurns);
        reportOne("Double", DoubleCache.TheHits, DoubleCache.TheMisses,
            DoubleCache.TheChurns);
        reportOne("Float", FloatCache.TheHits, FloatCache.TheMisses,
            FloatCache.TheChurns);
        reportOne("Int", IntCache.TheHits, IntCache.TheMisses,
            IntCache.TheChurns);
        reportOne("Long", LongCache.TheHits, LongCache.TheMisses,
            LongCache.TheChurns);
        reportOne("Short", ShortCache.TheHits, ShortCache.TheMisses,
            ShortCache.TheChurns);
        reportOne("TOTAL", TheTotalHits, TheTotalMisses, TheTotalChurns);
    }

    static private void reportOne(String name, int hits, int misses,
            int churns) {
        TheTotalHits += hits;
        TheTotalMisses += misses;
        TheTotalChurns += churns;
        double perc = (hits + misses) / 100.0;
        String percString = "";
        if (perc != 0.0) {
            percString = "; " + (int) (hits / perc) + "% hits, " +
                (int) (churns / perc) + "% churns";
        }
        System.err.println("  " + name + ": " + hits + " hits, " + misses +
            " misses, " + churns + " churns" + percString);
    }
}
