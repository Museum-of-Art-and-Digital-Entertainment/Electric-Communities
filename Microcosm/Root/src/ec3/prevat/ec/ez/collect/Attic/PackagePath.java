package ec.ez.collect;
import java.util.Hashtable;
import ec.ez.prim.EZStaticWrapper;

public class PackagePath implements NameTable {
    String prefix;
    Hashtable wrapperTable = new Hashtable();

    public PackagePath(String thePrefix) {
        prefix = thePrefix;
    }

    public PackagePath() {
        prefix = "";
    }

    public Object get1(String name) {
        String className = prefix + name;
        Class classFound = null;
        EZStaticWrapper wrapper = (EZStaticWrapper) wrapperTable.get(name);
        if(wrapper != null) {
            return(wrapper);
        }
        try { // See if a class is defined by name at this level
            classFound = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return(null);
        }

        wrapper = new EZStaticWrapper(classFound);
        wrapperTable.put(name, wrapper);
        return(wrapper);
    }

    public Object get(String name) throws NotFoundException {
       Object val = get1(name);
       if (val != null) {
            return val;
       } else {
            throw new NotFoundException(name);
       }
    }

    /**
     * Returns the NameTable stone-cast to give only a read-only
     * view.  If this NameTable is already read-only, just return it.
     */
    public NameTable readOnly() {
        return(this);
    }

    /**
     * Makes and returns a new NameTableEditor that inherits from this
     * NameTable.  This new NameTable starts with an empty set of
     * locals.
     */
    public NameTableEditor sprout() {
        return new NameTableEditorImpl((NameTable) this);
    }

    /**
     * Like sprout, but also defines in the result each pair of a name
     * and a value.  The names and values arrays must have the same
     * arity.
     */
    public NameTableEditor extend(String[] names, Object[] values)
         throws ArityMismatchException, AlreadyDefinedException {
        if (names.length != values.length) {
            throw new ArityMismatchException(names.length
                                             + " != " + values.length);
        }
        NameTableEditor result = sprout();
        for (int i = 0; i < names.length; i++) {
            result.introduce(names[i], values[i]);
        }
        return result;
    }

    /**
     * A snapshot of the name-to-value mapping represented by
     * this NameTable.  This is composed through the inheritance
     * chain, with a child's association occluding a parent's
     * association for the same name.
     */
    public Mapping mapping()  {
        return(null);
    }
}