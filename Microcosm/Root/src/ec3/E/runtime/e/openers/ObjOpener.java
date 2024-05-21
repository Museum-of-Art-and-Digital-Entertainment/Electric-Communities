package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Array;
import ec.util.NestedError;


/**
 * Opens objects for purposes of inspection or serializing / unserializing.
 */
public abstract class ObjOpener {

    static public final Class OBJ_OPENER_TYPE
        = JavaUtil.classForName("ec.e.openers.ObjOpener");

    private OpenerRecipe myMaker;
    /*package*/ Class myClass;
    private OpenerID myOpenerID;


    ObjOpener(OpenerRecipe maker,
              Class clazz,
              OpenerID openerID) {
        myMaker = maker;
        myClass = clazz;
        myOpenerID = openerID;
    }

    void init(OpenerID openerID) {
        if (myOpenerID != null) {
            throw new IllegalArgumentException("already initialized");
        }
        myOpenerID = openerID;
    }

    /**
     * Returns the OpenerRecipe for making openers with the same point
     * of view as this one.
     */
    public OpenerRecipe maker() {
        return myMaker;
    }

    public Class type() {
        return myClass;
    }

    public OpenerID openerID() {
        if (myOpenerID == null) {
            throw new IllegalArgumentException("not initialized");
        }
        return myOpenerID;
    }

    /**
     * Returns an Object to be taken as a semantics-free commentary on
     * the kind of information revealed by this RefOpener.
     * In the current system this happens to be a String[] of virtual
     * field names--one for each preface and body arg--to be used in
     * debugging tools such as inspectors.  <p>
     *
     * Such tools should cope with finding nulls, objects they
     * do not know how to interpret as commentary, or a String[] of
     * the wrong length.  A String[] of the wrong length should be
     * matched against the args in order, and the tool should cope
     * with the rest. <p>
     *
     * @return nullOk, suspect;
     */
    public abstract Object commentary();


    /**
     * Encode preface information for 'ref' onto 'encoder'.  The
     * preface of an object's encoding is that information which must
     * be decoded in order to make the object at all, and thereby to
     * be able to decode pointers to it.  For example, arrays encode
     * their length as part of their preface, since this info must be
     * known before the array can be created and registered.  The
     * preface must not contain any pointers to this very object,
     * since the object isn't yet registered to resolve these
     * pointers. <p>
     *
     * Most objects have no preface, hence the default implementation
     * does nothing.
     *
     * @param ref nullOk, suspect;
     * @param encoder suspect;
     */
    public abstract void encodePreface(Object ref, RtEncoder encoder)
         throws IOException;

    /**
     * Assuming the preface has already been encoded, encode the
     * body of 'ref' onto encoder.
     *
     * @param ref nullOk, suspect;
     * @param encoder suspect;
     */
    public abstract void encodeBody(Object ref, RtEncoder encoder)
         throws IOException;

    /**
     * Decodes the object's preface from 'decoder', then makes and
     * returns the object to be registered for decoded pointers to
     * point to.  This object will later be handed to decodeBody to be
     * filled in.
     *
     * @param decoder suspect;
     * @return nullOk, suspect;
     */
    public abstract Object decodePreface(RtDecoder decoder) throws IOException;

     /**
     * Assuming the preface has already been decoded, decode into the
     * body of 'ref' from the decoder, and initialize 'ref' accordingly.
     *
     * @param ref nullOk, suspect;
     * @param decoder suspect;
     */
    public abstract void decodeBody(Object ref, RtDecoder decoder)
         throws IOException;
}


/**
 * The ObjOpener for Strings is a special case since Strings directly
 * encode at UTF.
 */
public final class StringOpener extends ObjOpener {

    public StringOpener(OpenerRecipe maker) {
        super(maker,
              JavaUtil.STRING_TYPE,
              StringOpenerID.theOne());
    }

    public String toString() {
        return "String";
    }

    /**
     * StringOpeners currently need no commentary, so they currently
     * return null.
     */
    public Object commentary() {
        return null;
    }

    /** The preface is the string in UTF */
    public void encodePreface(Object string, RtEncoder encoder)
         throws IOException {
        encoder.writeUTF((String)string);
    }

    /** Does nothing, since Strings only have preface */
    public void encodeBody(Object obj, RtEncoder encoder) {}

    /** The preface is the string in UTF */
    public Object decodePreface(RtDecoder decoder) throws IOException {
        return decoder.readUTF();
    }

    /** Does nothing, since Strings only have preface */
    public void decodeBody(Object obj, RtDecoder decoder) {}
}


/**
 * An ObjOpener for one-dimensional Java arrays.  Higher-dimesional
 * arrays are described by ArrayOpeners whose element openers are also
 * ArrayOpeners.
 */
public final class ArrayOpener extends ObjOpener {

    private VarOpener myElementOpener;

    /**
     * Returns a Class representing the type "array of elem".
     */
    static private Class arrayClassOf(Class elemClass) {
        return Array.newInstance(elemClass, 0).getClass();
    }

    /**
     * Make an opener for opening arrays of objects of type 'elem'.
     */
    ArrayOpener(OpenerRecipe maker, Class elemClass) {
        super(maker,
              arrayClassOf(elemClass),
              OpenerID.make("[" + JavaUtil.signature(elemClass.getName())));
        myElementOpener = VarOpener.fromClass(elemClass);
    }

    public String toString() {
        return myElementOpener + "[]";
    }


    /**
     * ArrayOpeners currently return as commentary an array of one
     * String, "length", describing their fixed preface field.
     */
    public Object commentary() {
        String[] result = { "length" };
        return result;
    }

    public void encodePreface(Object array, RtEncoder encoder)
         throws IOException {
        encoder.writeInt(Array.getLength(array));
    }

    public void encodeBody(Object array, RtEncoder encoder)
         throws IOException {
        myElementOpener.encodeElements(array, encoder);
    }

    public Object decodePreface(RtDecoder decoder) throws IOException {
        return Array.newInstance(myElementOpener.type(), decoder.readInt());
    }

    public void decodeBody(Object array, RtDecoder decoder)
         throws IOException {
        myElementOpener.decodeElements(array, decoder);
    }

    /**
     * VarOpener for the array's element type
     */
    public VarOpener elementOpener() {
        return myElementOpener;
    }
}

