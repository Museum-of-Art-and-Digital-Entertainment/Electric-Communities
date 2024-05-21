package ec.e.serial;

import ec.trace.Trace;
import ec.e.file.EStdio;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import ec.util.NestedException;
import ec.tables.TableEditorImpl;
import ec.tables.IntTable;
import ec.util.ReadOnlyHashtable;
import ec.e.run.Vat;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ObjOpener;



/**
 * The encoding format is a binary form of typed scheme
 * s-expressions.  Positive index numbers are like variable
 * names--they refer to values which are already bound to these
 * numbers.  The pre-existing bindings are two->null, three->a
 * StringOpener, and four->an OpenerChef.  References to these objects
 * are just to their index.  When a not-yet-encoded object is
 * encountered, output an expression to produce a corresponding object
 * on decode, as well as assigning it a number for future
 * references. <p>
 *
 * The expression consists of APPLY--the moral equivalent of the
 * s-expression's open paren and represented by the number one, an
 * ObjOpener--the moral equivalent of the function being applied, and the
 * arguments.  Unlike scheme functions, the ObjOpener provides a static
 * type signature.  As a result, arguments statically typed as scalars
 * can be encoded directly with no extra overhead.  Further, no close
 * parent is needed since the static signature specifies the number of
 * arguments.  The ObjOpener and its non-scalar arguments are themselves
 * encoded as s-expressions. <p>
 *
 * An ObjOpener's static signature further divides the argument list into
 * two parts: the preface and the body.  The preface arguments are
 * read first.  Using these the object is made and registered under a
 * newly assiged number.  Then the body arguments are read and used to
 * further initialize the object.  Only data needed to create the
 * object at all should be in the preface.  In particular, the object
 * itself shouldn't be reachable from the preface, because there's no
 * way to break the resulting circularity.  Circular references from
 * the body args are fine, as the object is already registered. <p>
 *
 * One special case: for arrays, the length is the preface since it is
 * needed to create the array.  However, the body args cannot be
 * specified by a static signature, so arrays are just understood as
 * consisting of 'length' repetitions of whatever the element
 * signature declares.
 */
public class Serializer extends DataOutputStream implements RtEncoder {

    static private final Trace tr = new Trace("ec.e.serial.Serializer");

    /** XXX If these numbers are changed, edit Unserializer */
    static final int FULL_NUM = 0;
    static final int APPLY = 1;
    static final int NULL_INDEX = 2;

    static final int SERIAL_ID = 255;

    /*package*/ static final int DEFAULT_INITIAL_TABLE_SIZE = 2000;
    /**
     * highest assigned index
     */
    private int myIndex;
    private IntTable myObjectToIndexes;
    private OpenerRecipe myMaker;
    private int myDepth = 0;

    /**
     * The new Serializer will output onto 'out', using 'maker' to
     * determine what ObjOpener to use for which object.
     */
    /*package*/ Serializer(OutputStream out, OpenerRecipe maker) {
        this(out, maker, DEFAULT_INITIAL_TABLE_SIZE);
    }

    /*package*/ Serializer(OutputStream out,
                           OpenerRecipe maker,
                           int tablesize) {
        super(out);
        myMaker = maker;
        int size = 1+(int)(tablesize/TableEditorImpl.DEFAULT_LOAD_FACTOR);
        myObjectToIndexes = new IntTable(true, size);

        //since we're not skipping the null case
        myIndex = NULL_INDEX - 1;
        registerNext(null);

        ObjOpener stringOpener = maker.forEncoding("");
        registerNext(stringOpener);
        registerNext(maker.forEncoding(stringOpener));
    }

    /**
     * Create a Serializer
     *
     * @param out;
     * @param maker;
     */
    static public Serializer make(OutputStream out, OpenerRecipe maker)
        throws IOException
    {
        return make(out, maker, DEFAULT_INITIAL_TABLE_SIZE);
    }

    static public Serializer make(OutputStream out,
                                  OpenerRecipe maker,
                                  int tablesize)
        throws IOException
    {
        out.write(SERIAL_ID);
        return new Serializer(out, maker, tablesize);
    }

    /**
     * Associate 'obj' with the next unassigned index number
     */
    /*package*/ int registerNext(Object obj) {
        if (myObjectToIndexes.containsKey(obj)) {
            throw new Error("Internal: Already assigned " + obj);
        }
        myIndex++;
        myObjectToIndexes.putInt(obj, myIndex);
        return myIndex;
    }

    /**
     * Our cheesy compression scheme.  Most numbers should be small,
     * so use one byte for byte-sized numbers, and 5 bytes for the
     * rest.
     */
    private void writeNum(int num) throws IOException {
        if (num > 0 && num <= 255) {
            writeByte(num);
        } else {
            writeByte(FULL_NUM);
            writeInt(num);
        }
    }


    public void encodeGraph(Object obj) throws IOException {
        if (myDepth != 0) {
            throw new Error("internal: encodeGraph shouldn't be nested");
        }
        myDepth++;
        encodeObject(obj);
        myDepth--;
    }


    /**
     * Encode 'obj' as an s-expression.
     *
     * @param obj nullOk;
     */
    public void encodeObject(Object obj) throws IOException {
        if (myDepth == 0) {
            throw new Error
              ("internal: use encodeGraph for root instead of encodeObject");
        }
        try {
            int result = myObjectToIndexes.getInt(obj, -1);
            if (result != -1) {
                //We've seen it before.  Just say its number.
                //      like a variable name: var
                writeNum(result);
                return;
            }

            myDepth++;
            //output apply expression:
            //      ((rec var (opener preface-args)) body-args)

            //the open parens
            writeByte(APPLY);

            //the inner funtion
            ObjOpener opener = openerForObject(obj);
            encodeObject(opener);

            //pre-identity args
            opener.encodePreface(obj, this);

            //give obj a numeric identity
            registerNext(obj);

            //post-identity args
            opener.encodeBody(obj, this);

        } catch (IOException ex) {
            tr.errorm("while encoding " + obj);
            throw new NestedException("Problem Encoding " + obj, ex);
        } catch (Error err) {
            tr.errorm("while encoding " + obj);
            throw err;
        } catch (RuntimeException rex) {
            tr.errorm("while encoding " + obj);
            throw rex;
        }
        myDepth--;
    }


    /*package*/ ObjOpener openerForObject(Object obj) {
        return myMaker.forEncoding(obj);
    }


    /**
     * Method from RtEncoder that Serializer should not implement
     */
    public ReadOnlyHashtable getProperties() {
        throw new Error("XXX Should not be called");
    }

    /**
     * Method from RtEncoder that Serializer should not implement
     */
    public byte[] getBytes() {
        throw new Error("XXX Should not be called");
    }

    /**
     * Really, how many indexes are assigned.  This is one more than
     * the highest index which has been assigned.
     */
    public int numObjects() {
        return myIndex+1;
    }

    /**
     * Get the keeper for this serializer. Eventually we may want
     * make this something other than always null.
     */
    public RtExceptionEnv getKeeper() {
        return null;
    }
}
