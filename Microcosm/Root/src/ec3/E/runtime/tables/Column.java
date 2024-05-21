package ec.tables;


/**
 * A column of a Table.  A Table has a KeyColumn for the keys that it
 * maps from, and a Column for the values being mapped to.  The
 * entries in a column are called slots, and are indexed into with
 * slot numbers.
 */
abstract /*package*/ class Column implements Cloneable {

    /**
     * Exists only because ecomp doesn't yet understand the Foo.class
     * syntax 
     */
    static /*package*/ final Class THE_OBJECT_CLASS = new Object().getClass();

    /*package*/ Column() {}

    /**
     * Make a value-column that can only hold values that conform to
     * 'memberType', hash 'numSlots' slots, and is weak or not
     * according to 'isWeak'.  If the memberType is a scalar type, the
     * column will represent these values unboxed.
     */
    static public Column values(Class memberType, 
                                int numSlots,
                                boolean isWeak) {
        if (memberType == Void.TYPE) {
            return new VoidColumn(numSlots);
        } else if (memberType == Integer.TYPE) {
            return new IntColumn(numSlots);
        } else if (memberType.isPrimitive()) {
            return new ScalarColumn(memberType, numSlots);
        } else {
            return new RefColumn(memberType, numSlots, isWeak);
        }
    }

    /**
     * defaults to strong (not weak)
     */
    static public Column values(Class memberType, int numSlots) {
        return values(memberType, numSlots, false);
    }

    /**
     * memberType defaults to Object
     */
    static public Column values(int numSlots) {
        return values(THE_OBJECT_CLASS, numSlots, false);
    }

    /**
     *
     */
    abstract /*package*/ int numSlots();

    /**
     * Is this column weak?  If so, then some appropriate garbage
     * collector will not retain an object based purely on membership
     * in this column.  Rather, it will atomically collect the object
     * and cause this slot in the table (both key and value columns)
     * to be vacated, and then it will ensure that interested parties
     * are eventually informed.
     */
    /*package*/ boolean isWeak() {
        return false;
    }

    /**
     * 
     */
    abstract /*package*/ Object get(int slot);

    /**
     * 
     */
    abstract /*package*/ void put(int slot, Object value);

    /**
     * Stop pointing at an object from this slot.  If this is a scalar
     * column, does nothing
     */
    abstract /*package*/ void vacate(int slot);

    /**
     * All the members of the column must conform to this type
     */
    abstract /*package*/ Class memberType();

    /**
     * A shallow copy of the column.  The members are shared, not copied.
     */
    abstract protected Object clone();

    /**
     * Makes a new column just like this one, except of the specified
     * size and without any members.
     */
    abstract /*package*/ Column newVacant(int numSlots);
}
