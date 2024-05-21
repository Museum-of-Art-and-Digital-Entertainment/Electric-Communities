package ec.e.serial;

import java.util.Hashtable;
import java.io.InputStream;
import ec.e.openers.Recipe;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.Chef;
import ec.e.openers.guest.ParamRecipe;
import java.io.IOException;

/**
 * @see ec.e.serial.Unserializer.java
 */
public class ParamUnserializer extends Unserializer {

    /** 
     * Same as Unserializer except make a Chef for the current
     * parimeter table and register it so the current table is used
     * for decoding items that were encoded with the encoding version
     * of the table
     */
    private ParamUnserializer(InputStream in,
                              OpenerRecipe maker,   
                              Hashtable parameters) {
        super(in, maker);
        Recipe paramRecipe = new ParamRecipe(null, parameters);
        //Note that we don't registerDecoder on the Chef
        registerNext(new Chef(maker, paramRecipe));
    }

    /** 
     * Create an Unserializer or ParamUnserializer according to what
     * was encoded.  Used instead of a constructor.  
     *
     * @param in;
     * @param maker;
     * @param parameters nullOk;
     */
    static public Unserializer make(InputStream in,
                                    OpenerRecipe maker,
                                    Hashtable parameters)
        throws IOException
    {
        int firstbyte = in.read();
        if (firstbyte == Serializer.SERIAL_ID) {
            return new Unserializer(in, maker);
        } else {
            if (firstbyte != ParamSerializer.PARAMSERIAL_ID) {
                throw new IOException("Wrong ID code in ParamSerial stream");
            }
            return new ParamUnserializer(in, maker, parameters);
        }
    }
}

