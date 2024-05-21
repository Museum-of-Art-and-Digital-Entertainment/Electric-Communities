package ec.ez.collect;
import java.util.Enumeration;

/**
 * A Tuple is a Mapping whose keys are exactly the integers
 * from 0 to size()-1
 */
public interface Tuple extends Mapping {

    /**
     * For the EZ programmer, this is synonymous with 'get'.  Provided as
     * a convenience for the Java programmer.
     */
    public Object index(int key) throws NotFoundException;

    /**
     * Returns the portion of this Tuple's values starting at key 'start'
     * and going up till key 'bound'.  If 'bound' is the same as 'size()',
     * it is all the Tuple's values starting at key 'start'.
     */
    public Tuple slice(int start, int bound) throws NotFoundException;

    /**
     * Return a new Tuple consisting of this Tuple's values followed by
     * 'other's values.  It has this peculiar name so that EZ's "+"
     * operator will invoke it.
     */
    public Tuple add(Tuple other);

    /**
     * XXX Ad hockery For supporting coercions
     */
    public Object[] asArray();
}

