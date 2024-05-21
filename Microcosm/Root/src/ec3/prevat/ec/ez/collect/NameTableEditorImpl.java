package ec.ez.collect;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import ec.ez.runtime.AlreadyDefinedException;

public class NameTableEditorImpl implements NameTableEditor {

    private Hashtable myLocals = new Hashtable();
    private Vector myParents;
    private boolean redefinesOK = false;

    public NameTableEditorImpl() {
        myParents = null;
    }

    public NameTableEditorImpl(boolean redefOK) {
        myParents = null;
        redefinesOK = redefOK;
    }

    public NameTableEditorImpl(NameTable optParent) {
        myParents = new Vector();
        myParents.addElement(optParent);
    }

    NameTableEditorImpl(NameTable optParent, boolean redefOK) {
        myParents = new Vector();
        myParents.addElement(optParent);
        redefinesOK = redefOK;
    }

    /*
     * **************************************
     * implementing the NameTable interface *
     ****************************************
     */

    public Object get1(String name) {
        Object val = myLocals.get(name);
        if (val != null) {
            return (val); // JAY dropped ref to decode of null marker
        } else if (myParents != null) {
            for (Enumeration en = myParents.elements();
                 en.hasMoreElements(); ) {

                NameTable thisTable = (NameTable) en.nextElement();
                if((val = thisTable.get1(name)) != null) {
                    return(val);
                }
            }
        }
        return(null);
    }

    public Object get(String name) throws NotFoundException {
        return NullMarker.optDecode(get1(name), name);
    }

    public NameTable readOnly() {
        /* should eventually just use a stonecast mechanism */
        return new NameTableReadOnly(this);
    }

    public NameTableEditor sprout() {
        return new NameTableEditorImpl(this);
    }

    public NameTableEditor sprout(boolean redefOK) {
        return new NameTableEditorImpl(this, redefOK);
    }

    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException {

        if (names.length != values.length) {
            throw new ArityMismatchException(names.length
                                             + " != " + values.length);
        }
        NameTableEditor result = sprout();
        for (int i = 0; i < names.length; i++) {
            if(names[i] != null) {
                 // XXX JAY - allow null to appear in lambda args.
                result.introduce(names[i], values[i]);
            }
        }
        return result;
    }

    public Mapping mapping() {
        Mapping localMap = localMapping();
        if (myParents == null) {
            return localMap;
        } else {
            for (Enumeration en = myParents.elements();
                 en.hasMoreElements(); ) {

                NameTable thisTable = (NameTable) en.nextElement();
                Tuple pair = localMap.occlude(thisTable.mapping());
                try {
                    localMap = (Mapping)pair.index(0);
                } catch (NotFoundException ex) {
                    throw new Error("can't happen " + ex);
                }
            }
           return(localMap);
        }
    }

    /*
     * ********************************************
     * implementing the NameTableEditor interface *
     **********************************************
     */

    public void introduce(String name, Object value) throws AlreadyDefinedException {
        if ((myLocals.get(name) == null) || redefinesOK) { // JAY - allow redefs
            myLocals.put(name, NullMarker.encode(value));  // at listener root level.
        } else {
            throw new AlreadyDefinedException(name);
        }
    }

    public void put(String name, Object value) throws NotFoundException {
        if (myLocals.get(name) != null) {
            myLocals.put(name, NullMarker.encode(value)); // JAY - allow null to be stored.
        } else if (myParents != null) {
          for (Enumeration en = myParents.elements(); en.hasMoreElements(); ) {
            NameTable thisTable = (NameTable) en.nextElement();
            if(thisTable.get1(name) != null) { // JAY allow null marker
                ((NameTableEditor)thisTable).put(name, value);
                return;
            }
          }

       throw new NotFoundException(name);
       }
    }

    public Object undefine(String name) throws NotFoundException {
        Object result = myLocals.remove(name);
        if (result != null) {
            return NullMarker.optDecode(result);

        } else {
            throw new NotFoundException(name);
        }
    }

    public Mapping localMapping() {
        return MappingImpl.make(myLocals);
    }

    public void inherit(NameTable optParent) {
        if(myParents == null) {
            myParents = new Vector();
        }
        myParents.insertElementAt(optParent,0);
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

    public Object get1(String name) {
        return myWrapped.get1(name);
    }

    public NameTable readOnly() {
        return this;
    }

    public NameTableEditor sprout() {
        return myWrapped.sprout();
    }

    public NameTableEditor sprout(boolean redefOK) {
        return myWrapped.sprout(redefOK);
    }

    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException {
        return myWrapped.extend(names, values);
    }

    public Mapping mapping() {
        return myWrapped.mapping();
    }
}
