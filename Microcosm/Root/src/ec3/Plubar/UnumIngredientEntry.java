package ec.plubar;

import java.io.IOException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;

/**
 * For use in the UnumDefinition's table of ingredients; includes
 * a flag indicating if this ingredient's state should be encoded
 * upon Unum encoding.
 *
 * @author Karl Schumaker
 * @version 1.0
 */
public class UnumIngredientEntry {
    /** The index of this Ingredient as it is added to the UnumDefinition;
    * used to look up an Ingredient in the Unum.myIngredients[] array. */
    int index = -1;
    /** A flag indicating if this Ingredients state bundle is to be encoded
     * or not*/
    boolean encodeState = false;

    /**
     * The constructor for UnumIngredientEntry
     *
     * @param number  The number of an ingredient
     * @param encode  Whether or not to encode this ingredient's state
     *                when sending an Unum across the wire
     */
    public UnumIngredientEntry (int number, boolean encode) {
        index = number;
        encodeState = encode;
    }

    /**
     * Encode this entry, writing int index and boolean encodeState.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an IOException encoding error occurs in
     * encoder.writeInt() or encode.writeBoolean().
     */
    public void encode(RtEncoder encoder) throws UnumException {
        try {
            encoder.writeInt(index);
            encoder.writeBoolean(encodeState);
        } catch (IOException exc) {
            throw new UnumException("error encoding UnumIngredientEntry " + index);
        }
    }

    /**
     * Decode this definition, decoding an int and a boolean.
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an IOException decoding error occurs in
     * decoder.readInt() or decoder.readBoolean()
     */
    public void decode(RtDecoder decoder) throws UnumException {
        try {
            index = decoder.readInt();
            encodeState = decoder.readBoolean();
        } catch (IOException exc) {
            throw new UnumException("error decoding UnumIngredientEntry " + index);
        }
    }
}

