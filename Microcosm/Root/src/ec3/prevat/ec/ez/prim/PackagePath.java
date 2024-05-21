package ec.ez.prim;

import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.collect.Mapping;
import ec.ez.collect.ArityMismatchException;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NameTableEditorImpl;
import ec.ez.collect.NotFoundException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintStream;

public class PackagePath implements NameTable {
    String myPrefix;
    Hashtable myTable = new Hashtable();

    public PackagePath(String prefix) {
        myPrefix = prefix;
    }

    public PackagePath() {
        myPrefix = "";
    }

    public Object get1(String name) {
        String className = myPrefix + name;
        Class classFound = null;
        Object result = myTable.get(name);
        if (result != null) {
            return result;
        }
        try {
            // See if a class is defined by name at this level
            classFound = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Yes, I'm ignoring the error on purpose
        }
        if (classFound != null) {
            result = new EZStaticWrapper(classFound);
            myTable.put(name, result);
       } else {
            // XXX JAY Don't just generate package names!
            result = null;
        }
        return result;
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
        return new NameTableEditorImpl(this);
    }

    /**
     * Makes and returns a new NameTableEditor that inherits from this
     * NameTable. Since PackagePaths do not permit user definitions to be
     * added, we just do a standard sprout.
     */
    public NameTableEditor sprout(boolean redefOK) {
        return new NameTableEditorImpl(this);
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
        throw new RuntimeException("path.mapping() is not yet implemented");
    }

    public static NameTableEditor standardNameTable(boolean redefOK,
                                                    Hashtable initPOV,
                                                    PrintStream stdout) {
        PackagePath statics = new PackagePath();
        NameTableEditorImpl nameTable = new NameTableEditorImpl(redefOK);

        try {
            nameTable.introduce("true",         Boolean.TRUE);
            nameTable.introduce("false",        Boolean.FALSE);
            nameTable.introduce("null",         null);
            nameTable.introduce("stdout",       stdout);
            nameTable.introduce("print",        new PrintFunc(stdout));
            nameTable.introduce("println",      new PrintlnFunc(stdout));
            nameTable.introduce("makeTuple",    TupleMaker.theOne());

            if (initPOV != null) {
                Enumeration en = initPOV.keys();
                while (en.hasMoreElements()) {
                    String theKey = (String) en.nextElement();
                    Object theValue = (Object) initPOV.get(theKey);
                    nameTable.introduce(theKey, theValue);
                }
            }
// Setup the staticroot
         nameTable.inherit(new PackagePath());
         nameTable.inherit(new PackagePath("ec.ez."));
/*
         nameTable.introduce("java",    statics.get("java"));
         nameTable.introduce("ec",      statics.get("ec"));
*/
/*
         nameTable.inherit((NameTable)((NameTable)statics
                                          .get("java")).get("lang"));
         nameTable.inherit((NameTable)((NameTable)statics
                                          .get("ec")).get("ez"));
*/
        } catch (AlreadyDefinedException e) {
            e.printStackTrace();
            throw new Error("internal: " + e);
        }
/*
        catch (NotFoundException e) {
            e.printStackTrace();
            throw new Error("internal: " + e);
        }
*/
        return nameTable.readOnly().sprout(redefOK);
    }
}

