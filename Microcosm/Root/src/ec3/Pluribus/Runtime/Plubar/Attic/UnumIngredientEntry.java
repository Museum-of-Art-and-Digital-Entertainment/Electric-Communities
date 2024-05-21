package ec.pl.runtime;

import java.io.IOException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;

/**
 * For use in the UnumDefinition's table of ingredients; includes
 * a flag indicating if this ingredient's state should be encoded
 * upon Unum encoding.
 */
public class UnumIngredientEntry {
    String name = null;
    boolean encodeState = false;

    /**
     * The constructor for UnumIngredientEntry
     *
     * @param ingredientName  The role name of an ingredient
     * @param encode  Whether or not to encode this ingredient's state
     *                when sending an Unum across the wire
     */
    public UnumIngredientEntry (String ingredientName, boolean encode) {
        name = ingredientName;
        encodeState = encode;
    }

    /**
     * Encode this entry.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an encoding error occurs
     */
    public void encode(RtEncoder encoder) throws UnumException {
        try {
            encoder.encodeObject(name);
            encoder.writeBoolean(encodeState);
        } catch (IOException exc) {
            throw new UnumException("error encoding UnumIngredientEntry " + name);
        }
    }

    /**
     * Decode this definition.
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an decoding error occurs
     */
    public void decode(RtDecoder decoder) throws UnumException {
        try {
            name = (String)decoder.decodeObject();
            encodeState = decoder.readBoolean();
        } catch (IOException exc) {
            throw new UnumException("error decoding UnumIngredientEntry " + name);
        }
    }
}

