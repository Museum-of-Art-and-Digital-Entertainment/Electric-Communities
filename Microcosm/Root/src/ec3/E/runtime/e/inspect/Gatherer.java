package ec.e.inspect;
import java.util.Hashtable;
import ec.e.start.EEnvironment;

/**

 * The Gatherer class simply replaces three hashtables that used 
 * to be static variables with a public record of same. This is an
 * in-vat object. All normal gathers are added to these tables. In
 * order to gather CREW objects, use the crewGather() set of methods
 * that parallel the normal gather methods.

 */

public class Gatherer {
    public Hashtable objectCategories;
    public Hashtable objectsWithoutCategory;
    public Hashtable gatheredObjects;

    public Gatherer(EEnvironment env) {
        this();
    }

    public Gatherer() {
        gatheredObjects = new Hashtable(100);
        objectsWithoutCategory = new Hashtable(100);
        objectCategories = new Hashtable(100);

        // Uncategorized sorts on top because of white space first!
        objectCategories.put(" Uncategorized ", objectsWithoutCategory);
    }

    /**

     * Forget all objects we've gathered. You may want to do this if
     * you are performing some measurements or want to ship a
     * release. Note that the easiest way to accomplish this is to not
     * gather any objects at all in the first place by turning the
     * inspector off.

     */

    public void forgetAllGatheredObjects() {
        gatheredObjects = new Hashtable(1);
        objectsWithoutCategory = new Hashtable(2);
        objectCategories = new Hashtable(2);
        // Uncategorized sorts on top because of white space first!
        objectCategories.put(" Uncategorized ", objectsWithoutCategory);
    }
}
