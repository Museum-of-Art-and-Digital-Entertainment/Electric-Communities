package ec.e.serial;

import ec.e.file.EStdio;
import java.util.Vector;
import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import ec.util.NestedException;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ObjOpener;


/**
 * @see ec.e.serial.Serializer.java
 */
public class Unserializer extends DataInputStream implements RtDecoder {

    static private final Trace tr = new Trace("ec.e.serial.Unserializer");

    /**
     * highest assigned index
     */
    private int myIndex;
    private Vector myIndexToObjects;
    private OpenerRecipe myMaker;
    private Vector[] myDelayQueues = new Vector[NUM_DELAY];
    private int myDepth = 0;

    /**
     * The new Unserializer will read 'in', using 'maker' as the
     * source of authority for making various objects.
     */
    /*package*/ Unserializer(InputStream in, OpenerRecipe maker) {
        this(in, maker, Serializer.DEFAULT_INITIAL_TABLE_SIZE);
    }

    /*package*/ Unserializer(InputStream in,
                             OpenerRecipe maker,
                             int tablesize) {
        super(in);
        myIndexToObjects = new Vector(tablesize);
        myMaker = maker;
        for (int i = 0; i < myDelayQueues.length; i++) {
            myDelayQueues[i] = new Vector(100);
        }

        myIndexToObjects.addElement(null); //for FULL_NUM
        myIndexToObjects.addElement(null); //for APPLY

        //since we're not skipping the null case
        myIndex = Serializer.NULL_INDEX - 1;
        registerNext(null);

        //XXX calling forEncoding() here is kludgy, but it's ok
        ObjOpener stringOpener = maker.forEncoding("");
        registerNext(stringOpener);
        registerNext(maker.forEncoding(stringOpener));
    }

    /**
     * Create an Unserializer, used instead of a constructor.
     *
     * @param in;
     * @param maker;
     */
    static public Unserializer make(InputStream in, OpenerRecipe maker)
        throws IOException {
            return make(in, maker, Serializer.DEFAULT_INITIAL_TABLE_SIZE);
    }

    static public Unserializer make(InputStream in,
                                    OpenerRecipe maker,
                                    int tablesize) throws IOException
    {
            int firstbyte = in.read();
            if (firstbyte != Serializer.SERIAL_ID) {
                throw new IOException
                  ("Wrong ID code in Serial stream.  code=" + firstbyte);
            }
            return new Unserializer(in, maker, tablesize);
    }

    /**
     * Associate 'obj' with the next unassigned index number
     */
    /*package*/ int registerNext(Object obj) {
        myIndex++;
        myIndexToObjects.addElement(obj);
        return myIndex;
    }


    /**
     *
     * @see ec.e.serial.Serializer#writeNum
     */
    private int readNum() throws IOException {
        int result = readUnsignedByte();
        if (result == Serializer.FULL_NUM) {
            return readInt();
        } else {
            return result;
        }
    }


    /**
     * For use during unserialization by openers to postpone
     * initialization actions until after other initialization has
     * occurred.
     */
    public void delay(int delayCategory, Runnable thunk) {
        myDelayQueues[delayCategory].addElement(thunk);
    }

    /**
     * Decode an s-expression into an object or null.  For use by
     * outside clients.
     *
     * @return nullOk;
     */
    public Object decodeGraph() throws IOException {
        if (myDepth != 0) {
            throw new Error("internal: decodeGraph shouldn't be nested");
        }
        myDepth++;
        Object result = decodeObject();

        //do all the delayed initializations
        for (int i = 0; i < myDelayQueues.length; i++) {
            Vector q = myDelayQueues[i];
            for (Enumeration iter = q.elements();
                 iter.hasMoreElements(); ) {
                Runnable thunk = (Runnable)iter.nextElement();
                thunk.run();
            }
        }
        myDepth--;
        return result;
    }


    /**
     * Decode an s-expression into an object or null.  For use
     * during unserialization by the various openers.
     *
     * @return nullOk;
     */
    public Object decodeObject() throws IOException {
        if (myDepth == 0) {
            throw new Error
              ("internal: use decodeGraph on root instead of decodeObject");
        }

        int index = readNum();
        if (index >= Serializer.NULL_INDEX && index <= myIndex) {
            //evaluate a "variable reference" in the accumulated
            //"binding environment".
            return myIndexToObjects.elementAt(index);
        }
        if (index != Serializer.APPLY) {
            throw new IOException("Unrecognized reference encoding");
        }

        //decode an apply-form.  The "open paren" has already been
        //read.

        //Just because something encoded as non-null, doesn't mean it
        //must decode as non-null.
        myDepth++;
        ObjOpener opener = (ObjOpener)decodeObject();
        Object result = null;
        try {
            result = opener.decodePreface(this);
            registerNext(result);
            opener.decodeBody(result, this);

        } catch (Error ex) {
            diagnose(opener);
            throw ex;
        } catch (RuntimeException rex) {
            diagnose(opener);
            throw rex;
        } catch (IOException ioex) {
            diagnose(opener);
            throw ioex;
        }
        myDepth--;
        return result;
    }

    private void diagnose(ObjOpener opener) {
        //XXX too slow.
        //tr.errorm("while decoding with " + opener);
        
        //XXX Repository calls this before there's a vat
        //EStdio.err().println("while decoding with " + opener);

        System.err.println("while decoding with " + opener);
    }


    /**
     * Method from RtDecoder that Unserializer should not implement
     */
    public void replaceObjectInTable (Object old, Object obj) {
        throw new Error("XXX Should not be called");
    }

    /**
     * Method from RtDecoder that Unserializer should not implement
     */
    public void insertObjectInTable (int index, Object obj) {
        throw new Error("XXX Should not be called");
    }

    /**
     * Returns the internal vector and invalidates this Unserializer
     */
    public Vector done() {
        Vector result = myIndexToObjects;
        myIndexToObjects = null;
        return result;
    }

    /**
     * Get the keeper for this unserializer. Eventually we may want
     * make this something other than always null.
     */
    public RtExceptionEnv getKeeper() {
        return null;
    }
}
