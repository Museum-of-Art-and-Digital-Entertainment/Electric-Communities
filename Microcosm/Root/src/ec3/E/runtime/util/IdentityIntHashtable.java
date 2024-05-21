package ec.util;

/**
 * This class is an identity IntHashtable, that is, it one whose membership
 * check is == and whose hashing behavior is identityHashCode.
 *
 * @author Dan Bornstein, danfuzz@communities.com
 */
public class IdentityIntHashtable
extends GenericIntHashtable
{
    public IdentityIntHashtable(int initialCapacity, double loadFactor,
        int maxProbes, int noValue) {
        super(initialCapacity, loadFactor, maxProbes, noValue);
    }

    /**
     * Implementation of method from superclass.
     */
    public boolean performEquals(Object o1, Object o2) {
        return o1 == o2;
    }

    /**
     * Implementation of method from superclass.
     */
    public int performHashCode(Object target) {
        return System.identityHashCode (target);
    }
}
