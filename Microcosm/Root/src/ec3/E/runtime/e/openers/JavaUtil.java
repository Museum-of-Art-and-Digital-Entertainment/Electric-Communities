package ec.e.openers;

import ec.vcache.VCache;
import ec.vcache.ClassCache;
import ec.tables.TableEditor;
import ec.tables.TableEditorImpl;
import ec.util.NestedError;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 *
 */
public abstract class JavaUtil {

    static public final Class OBJECT_TYPE = new Object().getClass();

    static public final Class STRING_TYPE = "".getClass();

    static /*package*/ final String STRING_SIGNATURE = "Ljava/lang/String;";

    static public final Class CLASS_TYPE = OBJECT_TYPE.getClass();

    static public final Class TABLE_TYPE = new TableEditorImpl().getClass();


    static final Object[][] PRIM_TYPES_BY_FQN = {
        { "boolean",    Boolean.TYPE },
        { "byte",       Byte.TYPE },
        { "char",       Character.TYPE },
        { "short",      Short.TYPE },
        { "int",        Integer.TYPE },
        { "long",       Long.TYPE },
        { "float",      Float.TYPE },
        { "double",     Double.TYPE },
        { "void",       Void.TYPE },
    };


    /**
     * Maps fully qualified class names of primitive classes to
     * corresponding signatures.
     */
    static final TableEditor PrimTypes = new TableEditorImpl(STRING_TYPE, CLASS_TYPE);

    static {
        PrimTypes.putPairs(PRIM_TYPES_BY_FQN, true);
    }


    static final String[][] PRIM_SIGS_BY_FQN = {
        { "boolean",    "Z" },
        { "byte",       "B" },
        { "char",       "C" },
        { "short",      "S" },
        { "int",        "I" },
        { "long",       "J" },
        { "float",      "F" },
        { "double",     "D" },
        { "void",       "V" },
    };


    /**
     * Maps fully qualified class names of primitive classes to
     * corresponding signatures.
     */
    static final TableEditor PrimNames = new TableEditorImpl(STRING_TYPE, STRING_TYPE);

    static {
        PrimNames.putPairs(PRIM_SIGS_BY_FQN, true);
    }


    /**
     * Since a static initialization expression can't throw anything
     * that needs to be declared, we provide this wrapping of Class.forName()
     */
    static /*package*/ Class classForName(String fqn) {
        Class result = (Class)PrimTypes.get(fqn, null);
        if (result != null) {
            return result;
        }
        try {
            return ClassCache.forName(fqn);
        } catch (ClassNotFoundException ex) {
            throw new NestedError("initialization error: " + fqn, ex);
        }
    }


    /**
     * Given a fully qualified name of a class (as defined by
     * Class.getName(), return a corresponding signature, as defined
     * by the JVM spec.
     */
    static public String signature(String fqn) {
        String result = fqn.replace('.', '/');
        if (fqn.charAt(0) == '[') {
            return result;
        }
        String prim = (String)PrimNames.get(result, null);
        if (prim != null) {
            return prim;
        }
        return 'L' + result + ';';
    }


    /**
     * Given a signature of a class, as defined by the JVM spec,
     * return the fully qualified name of the class, as defined by
     * Class.getName().
     */
    static public String fullyQualifiedName(String sig) {
        String result = sig.replace('/', '.');
        switch(result.charAt(0)) {
            case 'Z':   return "boolean";
            case 'B':   return "byte";
            case 'C':   return "char";
            case 'S':   return "short";
            case 'I':   return "int";
            case 'J':   return "long";
            case 'F':   return "float";
            case 'D':   return "double";
            case 'V':   return "void";
            case '[':   return result;
            case 'L': {
                int lastPos = result.length() -1;
                if (result.charAt(lastPos) != ';') {
                    throw new IllegalArgumentException
                      (sig + " must start in 'L' and and in ';'");
                }
                return result.substring(1, lastPos);
            }
            default: {
                throw new IllegalArgumentException("unrecognized: " + sig);
            }
        }
    }


    /**
     *
     */
    static /*package*/ Class classForSignature(String signature) {
        switch(signature.charAt(0)) {
            case 'Z':   return Boolean.TYPE;
            case 'B':   return Byte.TYPE;
            case 'C':   return Character.TYPE;
            case 'S':   return Short.TYPE;
            case 'I':   return Integer.TYPE;
            case 'J':   return Long.TYPE;
            case 'F':   return Float.TYPE;
            case 'D':   return Double.TYPE;
            case 'V':   return Void.TYPE;
            default: {
                return classForName(fullyQualifiedName(signature));
            }
        }
    }


    /**
     * Appends two arrays into a result array of the same type as the
     * first array
     */
    static public Object append(Object arrayA, Object arrayB) {
        Class elementType = arrayA.getClass().getComponentType();
        int lenA = Array.getLength(arrayA);
        int lenB = Array.getLength(arrayB);
        Object result = Array.newInstance(elementType, lenA + lenB);
        System.arraycopy(arrayA, 0, result, 0, lenA);
        System.arraycopy(arrayB, 0, result, lenA, lenB);
        return result;
    }
}

