package ec.e.openers;

/** 
 * Super-interface of marker interfaces for declaring what can be
 * serialized over a given serialization system.  Except for the 
 * enumerated constants, IGNORE, WARNING, VERBOSE_WARNING, and ERROR,
 * all the static public finals provided here are defaults to be
 * overridden by interfaces that extend this one.
 */
public interface SerializableMarker {

    /**
     * The bit positions of MODIFIER_MASK are interpreted according to
     * java.lang.reflect.Modifier.  Each 1 bit prevents the
     * serialization of fields for whom that bit is on.  Such fields
     * are unserialized to null or zero.
     */
    static public final int MODIFIER_MASK = 0;


    /**
     * An array of disallowed-field descriptions.  Each disallowed-field
     * description is itself an array whose zeroth member is the
     * fully qualified name of a class containing some disallowed
     * fields, and the rest of which are the names of the fields of
     * that class which are disallows.  A disallowed field is not
     * serialized, and is unserialized to null or zero.
     */
    static public final String[][] DISALLOWED_FIELDS = {
        { "java.lang.Throwable",            "backtrace" },
        { "ec.regexp.RegularExpression",    "raw_bits", "raw_offsets" },
        { "ec.e.run.Tether",              "myHeld" },
        { "ec.e.hold.DataHolderSteward",    "myDataRequestor" },
        { "ec.e.rep.RepositoryHandle",    "myRepositoryFile" },
        { "ec.e.rep.RepositoryDirectDataHandle",    "myData" }
    };


    /** 
     * A class can be declared to be serializable by implicit copy
     * over a given serialization system either by implementing the
     * marker interface for that serialization system (an interface
     * that extends SerializableMarker), or by having its fully
     * qualified class name listed in ALLOWED_CLASSES. <p>
     *
     * ALLOWED_CLASSES is a collection of Strings, but is declared as
     * an array of Objects.  It is actually a tree whose internal
     * nodes are arrays and whose leaves are Strings.  It represents
     * the collection of these leaves.  This allows the collection to
     * be assembled by static composition -- by listing another such
     * collection as a member of a given one. <p>
     *
     * @see ec.tables.Table#putAll
     */
    static public final Object[] ALLOWED_CLASSES = {};


    /**
     * A placeholder for a feature that may go away.  Don't use
     * without talking to me first.  (markm@caplet.com, 610 949 4871)
     */
    static public final Object[] NOT_ALLOWED = {};


    /**
     * Besides getting serialized by instance layout, objects can also
     * get serialized specially by a Recipe object.  The first column
     * of each ENCODER_MAKERS entry in a given marker class lists all
     * the classes whose instances should get serialized specially
     * over the serialization system described by that marker
     * class. <p> 
     *
     * The second column lists corresponding encoder-making classes.
     * An encoder-making class is any class that has static public
     * methods 'Recipe makeEncoder()'.  The resulting Recipe is used
     * to serialize instance of the class named in the first column
     * (and that class' slice of instances of subclasses of that
     * class, unless the subclass has its own recipe binding). <p>
     * 
     * These encoder-making classes will typically also be the class of
     * the Recipe being made, but not necessarily. <p>
     *
     * @see ec.e.openers.guest.SerializableMarker#DECODER_MAKERS
     */
    static public final String[][] ENCODER_MAKERS = {
        { "java.util.Hashtable", "ec.e.openers.HashtableRecipe", },
        { "ec.cosm.gui.appearance.Appearance2D", 
                                 "ec.cosm.gui.appearance.Recipe2D" },
        { "ec.cosm.gui.appearance.Appearance3D", 
                                 "ec.cosm.gui.appearance.Recipe3D" },
        { "ec.e.rep.RepositoryHandle", 
                                 "ec.e.rep.RepositoryHandleRecipe" },
        { "ec.e.run.RtSealer",   "ec.e.openers.RtSealerRecipe" },
        { "ec.e.run.RtWeakCell", "ec.e.openers.RtWeakCellRecipe" },
        { "ec.tables.Table",     "ec.e.openers.TableRecipe" },
    };


    /** 
     * Besides getting unserialized by instance layout, objects can
     * also get unserialized specially by a Recipe object.  The first
     * column of each DECODER_MAKERS entry in a given marker class
     * lists all the classes whose instances should may unserialized
     * specially over the serialization system described by that
     * marker class, depending on the OpenerID with which they were
     * encoded. <p> 
     *
     * The second column lists corresponding decoder-making classes.
     * A decoder-making class is any class that has static public
     * methods 'Recipe makeDecoder(OpenerID openerID)'.  If the result
     * is null, then we fall back to unserializing by instance layout
     * if that matches.  If non-null, the resulting Recipe is used to
     * unserialize instance of the class named in the first column
     * (and that class' slice of instances of subclasses of that
     * class, unless the subclass has its own recipe binding). 
     * XXX issue here. <p>
     * 
     * These decoder-making classes will typically also be the class of
     * the Recipe being made, but not necessarily. <p>
     * 
     * ENCODER_MAKERS and DECODER_MAKERS will have mostly the same
     * entries, but, for example, a Recipe for upgrading an old
     * instance format into the new one might be represented by Recipe
     * class whose makeDecoder() only returns an instance of itself
     * when given an OpenerID describing the old layout.  It wouldn't
     * be listed in ENCODER_MAKERS because we want to encode in the
     * new format, not the old one.
     *
     * @see ec.e.openers.guest.SerializableMarker#ENCODER_MAKERS
     */
    static public final String[][] DECODER_MAKERS = {
        { "java.util.Hashtable", "ec.e.openers.HashtableRecipe", },
        { "ec.cosm.gui.appearance.Appearance2D", 
                                 "ec.cosm.gui.appearance.Recipe2D" },
        { "ec.cosm.gui.appearance.Appearance3D", 
                                 "ec.cosm.gui.appearance.Recipe3D" },
        { "ec.e.rep.RepositoryHandle", 
                                 "ec.e.rep.RepositoryHandleRecipe" },
        { "ec.e.run.RtSealer",   "ec.e.openers.RtSealerRecipe" },
        { "ec.e.run.RtWeakCell", "ec.e.openers.RtWeakCellRecipe" },
        { "ec.tables.Table",     "ec.e.openers.TableRecipe" },
        { "ec.util.PEHashtable", "ec.e.openers.PEHashtableRecipe" },
        { "ec.util.ObjKeyTable", "ec.e.openers.ObjKeyTableRecipe" },
    };
    

    /** 
     * This ELSE_MODE causes disallowed classes to be serialized
     * silently anyway
     */
    static public final int IGNORE = 0;

    /**
     * This ELSE_MODE lets disallowed classes be serialized, but
     * Traces a warning for each occurence
     */
    static public final int WARNING = 1;

    /**
     * This ELSE_MODE lets disallowed classes be serialized, but
     * Traces a verbose warning for each occurence, hopefully enabling
     * one to diagnose why serialization encountered the questionable
     * class. 
     */
    static public final int VERBOSE_WARNING = 2;

    /**
     * This ELSE_MODE causes an attempt to serialize a disallowed
     * class to fail by throwing an exception
     */
    static public final int ERROR = 3;


    /** 
     * ELSE_MODE should be one of IGNORE, WARNING, VERBOSE_WARNING,
     * or ERROR, depending on how we wish to handle a disallowed
     * class.  Currently, the default is WARNING, but it will soon
     * escalate to ERROR.
     */
    static public final int ELSE_MODE = IGNORE;
}

