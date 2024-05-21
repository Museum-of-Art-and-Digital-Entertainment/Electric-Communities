package ec.e.openers.guest;

import ec.e.openers.Recipe;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ClassRecipe;
import ec.e.openers.RefOpener;
import ec.e.openers.Chef;
import ec.util.PEHashtable;
import ec.e.util.DiscreteEnumeration;
import ec.e.util.CompoundEnumeration;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * When encoding a graph of objects, one must figure out where to
 * stop.  There are several good answers, such as the 'stop at Tether'
 * rules used when checkpointing the vat.  ParamOpenerRecipe
 * provides for an answer based on the notion of quasi-literal
 * parameterization.  <p>
 *
 * In C, the string "name: %s, value: %d" is a literal string.
 * However, as an argument to printf it is used as a string template
 * with two parameters to be filled in.  The other arguments to printf
 * provide the corresponding arguments needed to produce an actual
 * string.  <p>
 *
 * Similarly, we can turn an object graph into a parameterized
 * template of an object graph.  Replacing the percent-convention is
 * an encodingParams table mapping from perimeter-crossing object
 * references to corresponding param objects.  When a
 * reference-to-be-encoded is found as a key in this table, what is
 * encoded instead uses the corresponding value in the table as a
 * param object.  The encoded expression effectively says: "decode
 * by looking up the argument value associated with this param
 * object."  The param objects serve the role of parameter
 * positions or parameter names, depending on how you want to draw the
 * analogy. <p>
 *
 * Each time such an encoded structure is decoded, the decodingParams
 * table provides the mapping from param object to argument value
 * used to fill in these positions in the object graph.
 *
 * @see ec.e.openers.guest.ParamRecipe
 */
public class ParamOpenerRecipe extends OpenerRecipe {

    private RefOpener myParamOpener;
    private PEHashtable myEncodingParams;
    
    /**
     * Like the OpenerRecipe constructor, but takes the tables for
     * encoding parameters and decoding arguments.
     *
     * @param cookbook Your standard cookbook of Recipes.
     * @param encodingParams Maps from a perimeter-crossing object
     * reference to a corresponding param object.
     * @param decodingParams Maps from a param object to a
     * corresponding argument. 
     *
     * @see ec.e.openers.OpenerRecipe#OpenerRecipe
     * @see ec.e.openers.guest.SingletonRecipe
     *
     */
    public ParamOpenerRecipe(Enumeration cookbook, 
                  ClassRecipe classRecipe,
                  PEHashtable encodingParams,
                  Hashtable decodingParams) {
        super(cookbook, classRecipe);
        ParamRecipe paramRecipe = new ParamRecipe(encodingParams,
                          decodingParams);
        myParamOpener = new Chef(this, paramRecipe, true);
        myEncodingParams = encodingParams;
    }

    /**
     *
     */
    public RefOpener forObject(Object obj) {
        if (myEncodingParams.contains(obj)) {
            return myParamOpener;
        } else {
            return super.forObject(obj);
        }
    }   
}       

