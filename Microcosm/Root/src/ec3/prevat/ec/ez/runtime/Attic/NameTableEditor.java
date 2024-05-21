package ec.ez.runtime;

/**
 * The interface by which NameTables can be changed
 */
public interface NameTableEditor extends NameTable {

    /**
     * The locals must not already have a binding for 'name'.  A
     * binding from 'name' to 'value' is added to the locals.
     */
    public void define(String name, Object value) throws AlreadyDefinedException;

    /**
     * There must be a binding for 'name' somewhere in the inheritance
     * chain.  The first such binding is changed to map to 'value'.
     */
    public void put(String name, Object value) throws NotFoundException;

    /**
     * The locals must contain a binding for 'name'.  This
     * binding is removed, and its old value is returned.
     */
    public Object undefine(String name) throws NotFoundException;

    /**
     * A snapshot of the name-to-value mapping of the locals part of
     * this NameTable.  Even though this is a query only operation, we
     * put in on NameTableEditor since the distinction between locals
     * & non-locals has no effect on those that only have a
     * NameTable.
     */
    public Mapping localMapping();
}

