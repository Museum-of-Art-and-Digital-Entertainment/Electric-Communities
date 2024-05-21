package ec.e.openers;

public class RtWeakCellRecipe extends Recipe {

    static private final Object[][] PrefaceParams = {};

    static private final Object[][] BodyParams = {
        { JavaUtil.OBJECT_TYPE,     "target" },
    };

    static public final Class RTWEAKCELL_TYPE = new RtWeakCell(null).getClass();

    static private final RtWeakCellRecipe THE_ONE = new RtWeakCellRecipe();

    private RtWeakCellRecipe() {
        super(PrefaceParams, BodyParams, RTWEAKCELL_TYPE);
    }

    static public Recipe makeEncoder() {
        return THE_ONE;
    }

    static public Recipe makeDecoder(OpenerID opid) {
        if (THE_ONE.openerID().equals(opid)) {
            return THE_ONE;
        } else {
            return null;
        }
    }

    public Object[] prefaceArgs(Object obj) {
        return Recipe.NO_ARGS;
    }

    public Object[] bodyArgs(Object obj) {
        RtWeakCell cell = (RtWeakCell)obj;
        RtWeakling target = cell.get();

        Object[] result = new Object[1] ;
        result[0] = target;

        return result;
    }

    /**
     * Defaults to ignoring the prefaceArgs and just returns an
     * RtWeakCell to a dummy RtWeakling.
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        return new RtWeakCell(AWeakling.THE_WEAKEST);
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {
        RtWeakCell cell = (RtWeakCell)halfBaked;
        RtWeakling target = (RtWeakling)bodyArgs[0];
        cell.put(target);
    }
}

public class AWeakling implements RtWeakling
{
    static public AWeakling THE_WEAKEST = new AWeakling();

    AWeakling() {}

    public void addedToWeakCell (RtWeakCell cell) {}
    public void removedFromWeakCell (RtWeakCell cell) {}
    public void finalize () {}
}
