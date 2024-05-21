package ec.e.hold;

import java.util.Vector;
import java.util.Hashtable;
import ec.e.openers.Recipe;
import ec.e.openers.OpenerID;
//import ec.e.openers.RefOpener;
import ec.e.hold.DataHolder;
import ec.e.start.crew.CrewCapabilities;
import ec.e.rep.ParimeterizedRepository;
import ec.e.hold.DataHolderSteward;
import ec.cert.CryptoHash;

public class DataHolderRecipe extends Recipe {

    static final Class BYTE_ARRAY_TYPE = new byte[0].getClass();
    static final Class VECTOR_TYPE = new java.util.Vector(1).getClass();
    static public final Class DATAHOLDER_TYPE =
       new DataHolderSteward((CryptoHash)null,(Fulfiller)null,(Hashtable)null).getClass();
    private static ParimeterizedRepository theRepository = null;

    static private final Object[][] PrefaceParams = {
        { BYTE_ARRAY_TYPE, "cryptohash" },
    };

    static private final Object[][] BodyParams = {
        { ec.e.openers.JavaUtil.OBJECT_TYPE, "hints" },
        { ec.e.openers.JavaUtil.OBJECT_TYPE, "inheritedHints" },
    };

    public DataHolderRecipe() {
        super(PrefaceParams, BodyParams, DATAHOLDER_TYPE);
    }

    private static final DataHolderRecipe THE_ONE = new DataHolderRecipe();

    static public Recipe makeEncoder() {
        return THE_ONE;
    }

    public Class type() {
        return DATAHOLDER_TYPE;
    }

    static public Recipe makeDecoder(OpenerID opid) {
        if (THE_ONE.openerID().equals(opid)) {
            return THE_ONE;
        } else {
            return null;
        }
    }

    public Object[] prefaceArgs(Object obj) {
        Object[] result = new Object[1];
        result[0] = ((DataHolderSteward)obj).getCryptoHash().getCopyOfHashBytes();
        return result;
    }

    /**
     * Create a vector of Objects describing the object.
     * Currently the only objects are a Vector of hints (it may be null)
     */

    public Object[] bodyArgs(Object obj) {
        if (theRepository == null)
            theRepository = (ParimeterizedRepository)
              CrewCapabilities.getTheParimeterizedRepository();

        Object[] result = new Object[2];
        Vector hints = null;
        if (obj instanceof DataHolderSteward) {
            DataHolderSteward holder = (DataHolderSteward)obj;
            if (holder.myFulfiller != null) hints = holder.myFulfiller.getHints();

            // XXX Next few lines for debug only. You can delete all of them.

            CryptoHash hash = holder.getCryptohash();
            if (theRepository.isPublished(hash)) {
                if (Trace.repository.debug && Trace.ON)
                    Trace.repository.debugm
                      ("About to encode published cryptohash " + hash);
                if (hints == null) 
                    if (Trace.repository.debug && Trace.ON)
                        Trace.repository.debugm("but have no hints vector!");
            }   // END DEBUG LINES
        } else {
            if (Trace.repository.debug && Trace.ON)
                Trace.repository.debugm
                  ("in bodyargs - object was not a dataholder: " + obj);
        }
        result[0] = hints;                              // May be null
        result[1] = Fulfiller.defaultEmptyHints; // Gets parimeterized out, if curating.
        return result;
    }


    /**
     * Defaults to calling halfBakedInstanceOf using type().
     */

    public Object halfBakedInstance(Object[] prefaceArgs) {
        return halfBakedInstanceOf(type(), prefaceArgs);
    }

    /**
     * Ignores the sub and always returns a DataHolderSteward
     */

    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        CryptoHash hash = new CryptoHash((byte[])prefaceArgs[0],true);
        DataHolderSteward result = new DataHolderSteward(hash);
        return result;
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {

        // bodyArgs[0] is a hints vector. It may be null. If there
        // is a hints vector then we pass it to setFulfiller which may
        // or may not use it, depending on its policies regarding the
        // cryptohash in question - As an example, if it is published,
        // then we always include our sturdyref in the hints vector
        // whether or not we were given one from the
        // Repository.

        // bodyargs[1] is the inherited hints vector. Through an
        // unorthodox use of the Parimeterization mechanism this arg
        // becomes the hints vector for the parent dataholder of the
        // current dataholder. This means that the hints available
        // when decoding a dataholder for an appearance object will be
        // made available to the dataholders for the artwork
        // dataholders at the leaves also. Without this hack we cannot
        // decode dataholders to objects containing dataholders.

        DataHolderSteward result = (DataHolderSteward)halfBaked;
        Vector hints = (Vector)bodyArgs[0];
        Vector inheritedHints = null;

        if ((bodyArgs.length > 1) && (bodyArgs[1] instanceof Vector))
            inheritedHints = (Vector)bodyArgs[1];

        if (Trace.repository.debug && Trace.ON) {
            if (bodyArgs.length > 1)
                Trace.repository.debugm
                  ("in DataHolder decode - bodyargs[1] exists and is " + bodyArgs[1]);
        }
        result.setFulfiller(hints, inheritedHints);
    }
}
