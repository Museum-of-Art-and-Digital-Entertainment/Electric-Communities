package ec.e.openers;

import java.util.Vector;


/**
 * An openerID identifies both the type encoded/decoded by
 * the opener, as well as the types of the arguments to this
 * encoding/decoding.  An opener is encoded by using its
 * openerID so an object can be encoded in a self describing
 * format. <p>
 *
 * The format of an openerID is:
 * <pre>
 *     openerID  ::= '(' '(' varSig* ')' varSig* ')' objSig
 *                 | arraySig
 *                 | 'Ljava/lang/String;'
 *     varSig       ::= oneof("ZBCSIJFD")
 *                 | arraySig
 *                 | objSig
 *     objSig    ::= 'L' [a-zA-Z0-9/]* ';'
 *     arraySig  ::= '[' varSig
 * </pre>
 *
 * For an opener of the first kind, the sequence of signatures
 * within the inner parens describes the preface argument types,
 * the sequence of signatures following this within the outer
 * parens describes the body argument types, and the final objSig
 * describes the resulting type.  <p>
 *
 * When the openerID is an array signature, the implicit
 * preface is the length of the array, and the body is that
 * number of repetitions of the array's element type. <p>
 *
 * StringOpeners just use their signature as their
 * openerID.  They are a special case because they encode
 * their preface in UTF.  They have no body. <p>
 *
 * @see ec.e.openers.OpenerRecipe#forOpenerDesc
 */
public abstract class OpenerID extends Object {

    static public final Class OPENER_ID_TYPE 
        = JavaUtil.classForName("ec.e.openers.OpenerID");

    private String myDesc;

    /*package*/ OpenerID(String desc) {
        myDesc = desc;
    }

    static public OpenerID make(String desc) {
        char first = desc.charAt(0);
        if (first == '(') {
            return new NormalOpenerID(desc);
        } else if (first == '[') {
            return new ArrayOpenerID(desc);
        } else if (desc.equals(JavaUtil.STRING_SIGNATURE)) {
            return StringOpenerID.theOne();
        } else {
            throw new IllegalArgumentException("unrecognized: " + desc);
        }
    }

    public String descString() {
        return myDesc;
    }

    public String toString() {
        return "OpenerID(\"" + myDesc + "\")";
    }

    public int hashCode() {
        return myDesc.hashCode();
    }

    /**
     * Compares this OpenerID to the specified object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is an <code>OpenerID</code> object that represents
     * the same opener as this object.
     *
     * @param   anObject   the object to compare this <code>OpenerID</code>
     *                     against.
     * @return  <code>true</code> if the <code>OpenerID </code>are equal;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object anObject) {
        boolean result = false;
        if ((anObject != null) && (anObject instanceof OpenerID)) {
            result = myDesc.equals(((OpenerID)anObject).descString());
        }
        return result;
    }
}


/**
 *
 */
/*package*/ final class ArrayOpenerID extends OpenerID {

    /*package*/ ArrayOpenerID(String desc) {
        super(desc);
    }

    /*package*/ static ArrayOpenerID ofElementType(Class clazz) {
        return new ArrayOpenerID("[" + JavaUtil.signature(clazz.getName()));
    }

    /*package*/ Class elementType() {
        String fqn = JavaUtil.fullyQualifiedName(descString().substring(1));
        return JavaUtil.classForName(fqn);
    }
}


/**
 *
 */
/*package*/ final class StringOpenerID extends OpenerID {

    static private StringOpenerID THE_ONE = new StringOpenerID();

    private StringOpenerID() {
        super(JavaUtil.STRING_SIGNATURE);
    }

    /*package*/ static StringOpenerID theOne() {
        return THE_ONE;
    }
}


/**
 *
 */
/*package*/ final class NormalOpenerID extends OpenerID {

    VarOpener[] myPreface;
    VarOpener[] myBody;

    /*package*/ NormalOpenerID(String desc) {
        super(desc);

        if (! desc.startsWith("((")) {
            throw new RuntimeException(desc + " must start with '((' ");
        }
        int[] pos = new int[1];
        pos[0] = 2; //skip "(("
        myPreface = forNextSigs(desc, pos);
        myBody = forNextSigs(desc, pos);
    }

    static public NormalOpenerID make(Object[][] prefaceParams,
                                      Object[][] bodyParams,
                                      Class resultType) {
        StringBuffer buf = new StringBuffer();
        buf.append("((");
        for (int i = 0; i < prefaceParams.length; i++) {
            Class clazz = (Class)prefaceParams[i][0];
            buf.append(JavaUtil.signature(clazz.getName()));
        }
        buf.append(")");
        for (int i = 0; i < bodyParams.length; i++) {
            Class clazz = (Class)bodyParams[i][0];
            buf.append(JavaUtil.signature(clazz.getName()));
        }
        buf.append(")");
        buf.append(JavaUtil.signature(resultType.getName()));
        return new NormalOpenerID(buf.toString());
    }

    /**
     * @see ec.e.openers.NormalOpenerID#body
     */
    public VarOpener[] preface() {
        return myPreface;
    }

    /**
     * Currently, a RefOpener for a reference argument will have an
     * accurate type.  We expect to change this to simply being a
     * generic RefOpener of the 'Object' class, so don't count on the
     * current specificity.
     */
    public VarOpener[] body() {
        return myBody;
    }


    /**
     * The fully qualified class name of the result type
     */
    public String resultName() {
        String desc = descString();
        int i = desc.lastIndexOf(')');
        if (i == -1) {
            throw new IllegalArgumentException
              (desc + " not a NormalOpenerID description");
        }
        return JavaUtil.fullyQualifiedName(desc.substring(i+1));
    }


    /**
     * Returns the next openerDesc in 'descs'.  The result can either
     * be a signature, an openerID, or null if a ')' is next.  'descs'
     * is read starting at pos[0], and leaves pos[0] positioned just
     * after the returned desc.  <p>
     *
     * The 'pos[0]' thing is a hack to work around the lack of
     * multiple return results in Java.
     *
     * @return nullOk;  If the stream was positioned at a close paren,
     * nextDesc will return null and leave the stream positioned
     * afterwards.
     *
     * @exception IllegalArgumentException thrown if the end of 'descs'
     * was reached before a complete openerDesc was parsed.
     *
     * @see ec.e.openers.ObjOpener#openerID
     */
    static private String nextDesc(String descs, int[] pos) {
        int start = pos[0];
        pos[0]++;
        switch (descs.charAt(start)) {
            case ')': {
                //terminates a sequence of signatures
                return null;
            }
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
            case 'J':
            case 'F':
            case 'D': {
                //scalar signature
                break;
            }
            case 'L': {
                //non-array class signature
                int semi = descs.indexOf(';', pos[0]);
                if (semi == -1) {
                    throw new IllegalArgumentException
                        ("';' not found in " + descs.substring(start));
                }
                pos[0] = semi + 1;
                break;
            }
            case '[': {
                //array signature
                nextDesc(descs, pos);
                break;
            }
            default: {
                throw new IllegalArgumentException
                   ("unrecognized openerDesc " + descs.substring(start));
            }
        }

        return descs.substring(start, pos[0]);
    }

    /**
     * Read a sequence of signatures and a terminating close paren.
     * Return a corresponding array of VarOpeners.  XXX The VarOpeners
     * for reference types are more specific than they will be.  Don't
     * count on the specificity.
     */
    static private VarOpener[] forNextSigs(String sigs, int[] pos) {
        Vector vec = new Vector();
        while (true) {
            String sig = nextDesc(sigs, pos);
            if (sig == null) {
                //close paren already read
                break;
            }
            vec.addElement(VarOpener.fromSignature(sig));
        }
        VarOpener[] result = new VarOpener[vec.size()];
        vec.copyInto(result);
        return result;
    }
}
