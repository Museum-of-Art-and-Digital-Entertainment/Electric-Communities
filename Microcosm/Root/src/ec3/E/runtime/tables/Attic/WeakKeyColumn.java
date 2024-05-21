package ec.tables;

import java.util.NoSuchElementException;


/**
 *
 */
public class WeakKeyColumn 
extends IdentityKeyColumn 
implements WeakColumnMarker {

    public WeakKeyColumn(Class memberType,
                         int initialCapacity,
                         double loadFactor,
                         int maxProbes) {
        super(memberType, initialCapacity, loadFactor, maxProbes);
    }

    /**
     * Constructs a new, empty one, with a reasonable set of defaults.
     */
    public WeakKeyColumn(Class memberType) {
        super(memberType);
    }

    /**
     * Constructs a new, empty one, with a reasonable set of defaults.
     */
    public WeakKeyColumn() {
        super();
    }
}

