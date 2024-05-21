package ec.ez.runtime;

/**
 * A potentially mutable mapping from names (strings) to values
 * (arbitrary objects, including null).  NameTables inherit from each
 * other in a tree like fashion, so they can be used to model nesting
 * lexical environments.  The associations in the most leafward part
 * of a NameTable are called "locals". <p>
 *
 * The type NameTable only provides query operations but a subtype,
 * NameTableEditor, provides edit operations as well.  A given
 * NameTable reference may or may not actually be a NameTableEditor,
 * depending on whether it is supposed to grant edit authority.
 */
public interface NameTable {

    /**
     * What value is associated with 'name'?  Uses the first
     * associating up the inheritance chain.
     */
    public Object get(String name) throws NotFoundException;

    /**
     * Returns the NameTable stone-cast to give only a read-only
     * view.  If this NameTable is already read-only, just return it.
     */
    public NameTable readOnly();

    /**
     * Makes and returns a new NameTableEditor that inherits from this
     * NameTable.  This new NameTable starts with an empty set of
     * locals.
     */
    public NameTableEditor sprout();

    /**
     * Like sprout, but also defines in the result each pair of a name
     * and a value.  The names and values arrays must have the same
     * arity.
     */
    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException;

    /**
     * A snapshot of the name-to-value mapping represented by
     * this NameTable.  This is composed through the inheritance
     * chain, with a child's association occluding a parent's
     * association for the same name.
     */
    public Mapping mapping();
}

