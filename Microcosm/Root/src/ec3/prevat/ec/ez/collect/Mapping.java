package ec.ez.collect;
import java.util.Enumeration;

/**
 * A Mapping is a finite single-valued mapping of keys to values.
 * Equivalently, it can be considered a finite set of pairs, where
 * each is a pair of a key and a value, and no two pairs have the
 * same key.  The notion of equality used is currently that defined
 * by Object.equals() and Object.hashCode(), and the correct
 * functioning of Mappings depends on keys implementing these methods
 * correctly.  XXX  The notion of equality that will be used is
 * whatever is the standard EZ equality.
 */
public interface Mapping {

    /**
     * How many keys are mapped from?  Equivalently, how many pairs are in
     * the set?
     */
    public int size();

    /**
     * Is there a mapping for 'key'?
     */
    public boolean containsKey(Object key);

    /**
     * What value does 'key' map to?
     */
    public Object get(Object key) throws NotFoundException;

    /**
     * Returns a Mapping just like this one, except that 'key' maps to
     * 'newValue'.
     */
    public Mapping with(Object key, Object newValue);

    /**
     * Returns a Mapping just like this one, except that there is no
     * mapping for 'key'.
     */
    public Mapping without(Object key);

    /**
     * Enumerates all the keys
     */
    public Enumeration keys();

    /**
     * Enumerates each value, once for every time it's mapped to.  When a
     * Mapping is coerced to an Enumeration (as in an EZ 'for' expression),
     * this is the Enumeration used.
     */
    public Enumeration asEnumeration();

    public AssociationEnumeration associations();

    /**
     * Returns a Tuple of two Mappings that represent the result of occluding
     * 'under' by this Mapping.  The result[0] Mapping contains all the pairs
     * of this Mapping, and additionally contains the pairs from 'under' whose
     * keys do not conflict.  The result[1] Mapping contains exactly those
     * pairs from 'under' whose keys do conflict with a key of this Mapping. <p>
     *
     * Among other pleasant consequences, the domain of result[0] is the union
     * of the original domains, and the domain of result[1] is the intersection
     * of the original domains.
     */
    public Tuple occlude(Mapping under);
}
