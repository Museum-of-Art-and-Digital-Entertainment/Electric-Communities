package ec.ez.runtime;

import java.util.Hashtable;

public class NameTableEditorImpl implements NameTableEditor {

    private NameTable myOptParent;
    private Hashtable myLocals = new Hashtable();
    private Object myNullMarker = new Object();

    public NameTableEditorImpl() {
        myOptParent = null;
    }

    private NameTableEditorImpl(NameTable optParent) {
        myOptParent = optParent;
    }

    /*
     * **************************************
     * implementing the NameTable interface *
     ****************************************
     */

    public Object get(String name) throws NotFoundException {
        Object result = myLocals.get(name);
        if (result == myNullMarker) {
            return null;
        } else if (result != null) {
            return result;
        } else if (myOptParent != null) {
            return myOptParent.get(name);
        } else {
            throw new NotFoundException(name);
        }
    }

    public NameTable readOnly() {
        /* should eventually just use a stonecast mechanism */
        return new NameTableReadOnly(this);
    }

    public NameTableEditor sprout() {
        return new NameTableEditorImpl(this);
    }

    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException {
        if (names.length != values.length) {
            throw new ArityMismatchException(names.length
                                             + " != " + values.length);
        }
        NameTableEditor result = sprout();
        for (int i = 0; i < names.length; i++) {
            result.define(names[i], values[i]);
        }
        return result;
    }

    public Mapping mapping() {
        Mapping localMap = localMapping();
        if (myOptParent == null) {
            return localMap;
        } else {
            return localMap.occlude(myOptParent.mapping());
        }
    }

    /*
     * ********************************************
     * implementing the NameTableEditor interface *
     **********************************************
     */

    public void define(String name, Object value) throws AlreadyDefinedException {
        if (myLocals.get(name) != null) {
            throw new AlreadyDefinedException(name);
        } else if (value == null) {
            myLocals.put(name, myNullMarker);
        } else {
            myLocals.put(name, value);
        }
    }

    public void put(String name, Object value) throws NotFoundException {
        if (myLocals.get(name) != null) {
            myLocals.put(name, value);
        } else if (myOptParent != null) {
            ((NameTableEditor)myOptParent).put(name, value);
        } else {
            throw new NotFoundException(name);
        }
    }

    public Object undefine(String name) throws NotFoundException {
        Object result = myLocals.remove(name);
        if (result == null) {
            throw new NotFoundException(name);
        } else if (result == myNullMarker) {
            return null;
        } else {
            return result;
        }
    }

    public Mapping localMapping() {
        return new MappingImpl(myLocals, myNullMarker);
    }
}


class NameTableReadOnly implements NameTable {

    private NameTable myWrapped;

    public NameTableReadOnly(NameTable wrapped) {
        myWrapped = wrapped;
    }

    public Object get(String name) throws NotFoundException {
        return myWrapped.get(name);
    }

    public NameTable readOnly() {
        return this;
    }

    public NameTableEditor sprout() {
        return myWrapped.sprout();
    }

    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException {
        return myWrapped.extend(names, values);
    }

    public Mapping mapping() {
        return myWrapped.mapping();
    }
}
