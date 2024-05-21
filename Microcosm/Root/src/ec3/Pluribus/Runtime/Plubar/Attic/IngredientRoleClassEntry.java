package ec.pl.runtime;

import java.io.IOException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;

/**
 * For use in the Unum's encoding/decoding methods to store ingredient
 * role names coupled with class names
 */
public class IngredientRoleClassEntry {
    String roleName = null;
    String className = null;

    /**
     * The constructor for IngredientRoleClassEntry
     *
     * @param ingredientName  The role name of an ingredient
     * @param className  The class name of an ingredient
     */
    public IngredientRoleClassEntry (String ingredientName, String theClass) {
        roleName = ingredientName;
        className = theClass;
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
            encoder.encodeObject(roleName);
            encoder.encodeObject(className);
        } catch (IOException exc) {
            throw new UnumException("error encoding IngredientRoleClassEntry " + roleName);
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
            roleName = (String)decoder.decodeObject();
            className = (String)decoder.decodeObject();
        } catch (IOException exc) {
            throw new UnumException("error decoding IngredientRoleClassEntry " + roleName);
        }
    }
}

