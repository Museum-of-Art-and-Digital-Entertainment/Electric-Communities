package ec.e.run;

import ec.vcache.ClassCache;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Vector;

public abstract class RtSealer
implements RtCodeable
{
    protected int my_$_Index;
    protected String my_$_Name;
    private String myToString = null;
    private int myHashCode = 0;
    private boolean myIsSplit = false;
    private String myMethodName = null;
    private Class myReturnType = null;
    private Class[] myArgTypes = null;

    public RtSealer(int index, String name) {
        my_$_Index = index;
        my_$_Name = name;
    }

    /**
     * Kludge pending SealerRecipes.  Hopefully Sealers will go away
     * before we need to bother.
     */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        RtSealer o = (RtSealer) other; // won't fail, because of above tests
        return my_$_Name.equals(o.my_$_Name);
    }

    /**
     * Turn a JVM style sig type to a human string. Extra characters
     * (such as the rest of a function type sig) are ignored.
     */
    static public String sigToHumanString(String sig) {
        int arrayCount = 0;
        String result;
        int i = 0;
        while (sig.charAt(i) == '[') {
            arrayCount++;
            i++;
        }
        switch (sig.charAt(i)) {
            case 'B': { result = "byte"; break; }
            case 'C': { result = "char"; break; }
            case 'D': { result = "double"; break; }
            case 'F': { result = "float"; break; }
            case 'I': { result = "int"; break; }
            case 'J': { result = "long"; break; }
            case 'S': { result = "short"; break; }
            case 'V': { result = "void"; break; }
            case 'Z': { result = "boolean"; break; }
            case 'L': {
                int argEnd = sig.indexOf(";");
                result = sig.substring(i + 1, argEnd).replace('/', '.');
                break;
            }
            default: { result = "?"; break; }
        }
        while (arrayCount > 0) {
            result += "[]";
            arrayCount--;
        }
        return result;
    }

    /**
     * Turn a JVM style sig type to a Java type. Extra characters
     * (such as the rest of a function type sig) are ignored.
     */
    static public Class sigToType(String sig) {
        int arrayCount = 0;
        Class result;
        int i = 0;
        while (sig.charAt(i) == '[') {
            arrayCount++;
            i++;
        }
        switch (sig.charAt(i)) {
            case 'B': { result = Byte.TYPE; break; }
            case 'C': { result = Character.TYPE; break; }
            case 'D': { result = Double.TYPE; break; }
            case 'F': { result = Float.TYPE; break; }
            case 'I': { result = Integer.TYPE; break; }
            case 'J': { result = Long.TYPE; break; }
            case 'S': { result = Short.TYPE; break; }
            case 'V': { result = Void.TYPE; break; }
            case 'Z': { result = Boolean.TYPE; break; }
            case 'L': {
                int argEnd = sig.indexOf(";");
                String clName = sig.substring(i + 1, argEnd).replace('/', '.');
                try {
                    result = ClassCache.forName(clName);
                } catch (ClassNotFoundException e) {
                    result = null;
                }
                break;
            }
            default: { result = null; break; }
        }
        if ((result != null) && (arrayCount != 0)) {
            int[] dims = new int[arrayCount];
            // icky, but it works
            result = Array.newInstance(result, dims).getClass();
        }
        return result;
    }

    /**
     * Return the rest of the given string after the type signature
     * that starts it. This is useful for iterating over a JVM-style
     * type signature.
     */
    static public String sigToNextSig(String sig) {
        int i = 0;
        while (sig.charAt(i) == '[') {
            i++;
        }
        if (sig.charAt(i) != 'L') {
            return sig.substring(i+1);
        } else {
            int argEnd = sig.indexOf(";");
            return sig.substring(argEnd + 1);
        }
    }

    public String toString ()
    {
        if (myToString == null) {
            String name = this.getClass().getName();
            if (name.endsWith("_$_Sealer")) {
                name = name.substring(0, name.length() - 9);
            }
            String meth = my_$_Name;

            int oparen = meth.indexOf("(");
            String sofar = meth.substring(0, oparen);
            meth = meth.substring(oparen + 1);
            if (sofar.endsWith("$async")) {
                sofar = "<-" + sofar.substring(0, sofar.length() - 6);
            } else {
                sofar = "." + sofar;
            }
            sofar = name + sofar + "(";
            boolean firstArg = true;
            for (;;) {
                if (meth.charAt(0) == ')') {
                    break;
                }
                if (firstArg) {
                    firstArg = false;
                } else {
                    sofar += ", ";
                }
                sofar += sigToHumanString(meth);
                meth = sigToNextSig(meth);
            }
            myToString = sigToHumanString(meth.substring(1)) +
                " " + sofar + ")";
        }

        return myToString;
    }

    public int hashCode() {
        if (myHashCode == 0) {
            myHashCode = my_$_Name.hashCode() ^ 0xdbdbdbdb;
            if (myHashCode == 0) {
                myHashCode = 1;
            }
        }
        return myHashCode;
    }

    public String classNameToEncode (RtEncoder encoder)
    {
        return null;
    }

    public void encode (RtEncoder coder)
    {
        try
        {
            coder.writeUTF (my_$_Name);
        }
        catch (IOException e)
        {
            RtRun.tr.errorReportException(e, "encoding sealer");
        }
    }

    public Object decode (RtDecoder coder)
    {
        Object theSealer = null;
        try
        {
            String sealerName = coder.readUTF();
            return getSealerOrUnknownSealer(this.getClass(), sealerName);
        }
        catch (Exception e)
        {
            RtRun.tr.errorReportException(e, "decoding sealer");
            return (null);
        }
    }

    public String getSignature() {
        return my_$_Name;
    }

    private void splitSignature() {
        if (myIsSplit) {
            return;
        }
        String sig = my_$_Name;
        int oparen = sig.indexOf("(");
        myMethodName = sig.substring(0, oparen);
        sig = sig.substring(oparen + 1);
        Vector argTypes = new Vector();
        for (;;) {
            if (sig.charAt(0) == ')') {
                break;
            }
            argTypes.addElement(sigToType(sig));
            sig = sigToNextSig(sig);
        }
        myArgTypes = new Class[argTypes.size()];
        argTypes.copyInto(myArgTypes);
        myReturnType = sigToType(sig.substring(1));
        myIsSplit = true;
    }

    public String getMethodName() {
        if (! myIsSplit) {
            splitSignature();
        }
        return myMethodName;
    }

    public int getArgCount() {
        if (! myIsSplit) {
            splitSignature();
        }
        return myArgTypes.length;
    }

    public Class getArgType(int n) {
        if (! myIsSplit) {
            splitSignature();
        }
        return myArgTypes[n];
    }

    public Class getReturnType() {
        if (! myIsSplit) {
            splitSignature();
        }
        return myReturnType;
    }

    protected final void badSealer ()
    {
        throw (new RtInvocationException ("Sealer " + getClass() +
            " has bad index " + my_$_Index));
    }

    protected final void badTarget (Object target)
    {
        throw (new RtInvocationException ("Sealer " + this +
            " called with bad target " + target));
    }

    protected final void badArgs (Object target, Object[] args)
    {
        String message = "Sealer " + this + " on " + target +
            " called with bad arguments: ";
        if (args == null)
        {
            message += "null";
        }
        else
        {
            for (int i = 0; i < args.length; i++)
            {
                if (i != 0) message += ", ";
                message += args[i];
            }
        }
        throw (new RtInvocationException (message));
    }

    static public RtSealer getSealerOrUnknownSealer(Class cl, String name) {
        RtSealer theSealer = RtSealer.getSealer(cl, name);
        if (theSealer == null) {
            // Find an UnknownSealer if it exists. note this currently
            // always returns null until the real implementation is put in.
            theSealer = UnknownSealer.get(cl.getName(), name);
            if (theSealer == null) {
                throw new RtRuntimeException(
                    "Can't find Sealer for signature " + name);
            }
        }
        return theSealer;
    }


    static public RtSealer getSealer(String className, String sig) {
        Class theClass;
        try {
            theClass = ClassCache.forName(className + "_$_Sealer");
        } catch (ClassNotFoundException e) {
            throw new RtRuntimeException("eclass " + className + " not found");
        }
        return getSealer(theClass, sig);
    }

    static public RtSealer getSealer(Class theClass, String sig) {
        char[] orig = new char[sig.length()];
        sig.getChars(0, orig.length, orig, 0);
        char[] var = new char[orig.length * 2 + 9];
        var[0] = 's';
        var[1] = 'e';
        var[2] = 'a';
        var[3] = 'l';
        var[4] = 'e';
        var[5] = 'r';
        var[6] = '_';
        var[7] = '$';
        var[8] = '_';
        int j = 9;
        for (int i = 0; i < orig.length; i++) {
            char c = orig[i];
            if (c == ')') {
                break;
            }
            switch (c) {
                case '[': { var[j++] = '$'; var[j++] = 'A'; break; }
                case '(': { var[j++] = '$'; var[j++] = 'O'; break; }
                case '/': { var[j++] = '$'; var[j++] = 'D'; break; }
                case '$': { var[j++] = '$'; var[j++] = '$'; break; }
                case ';': { var[j++] = '$'; var[j++] = 'S'; break; }
                default:  { var[j++] = c; break; }
            }
        }
        String varName = new String(var, 0, j);
        try {
            Field f;
            // XXX BUG: all this gafoofoo is to work around a bug in java where it
            // can't find fields with names longer than 255 characters --danfuzz
            try {
                f = theClass.getDeclaredField(varName);
            } catch (NoSuchFieldException e) {
                Field[] fs = theClass.getDeclaredFields();
                int i;
                for (i = 0; i < fs.length; i++) {
                    if (fs[i].getName().equals(varName)) {
                        break;
                    }
                }
                if (i == fs.length)  {
                    throw e;
                }
                f = fs[i];
            }
            return (RtSealer) f.get(null);
        } catch (NoSuchFieldException e) {
            throw new RtRuntimeException("didn't find method " +
                theClass.getName() + " " + sig);
        } catch (IllegalAccessException e) {
            throw new RtRuntimeException("couldn't access sealer " +
                theClass.getName() + " " + sig);
        } catch (ClassCastException e) {
            throw new RtRuntimeException("found non-sealer " +
                theClass.getName() + " " + sig);
        }
    }

    /**
     * This turns a human-readable type name into a signature type
     * string. The human form is either a primitive type name
     * ("int", etc.) or a class name, which must be fully-qualified
     * unless it's in the java.lang package. It may be followed
     * by pairs of square brackets ("[]") to indicate an array
     * type.
     */
    static public String humanToType(String human) {
        String result = "";
        while (human.endsWith("[]")) {
            result += '[';
            human = human.substring(0, human.length() - 2);
        }
        if (human.equals("byte")) {
            result += 'B';
        } else if (human.equals("char")) {
            result += 'C';
        } else if (human.equals("double")) {
            result += 'D';
        } else if (human.equals("float")) {
            result += 'F';
        } else if (human.equals("int")) {
            result += 'I';
        } else if (human.equals("long")) {
            result += 'J';
        } else if (human.equals("short")) {
            result += 'S';
        } else if (human.equals("boolean")) {
            result += 'Z';
        } else {
            if (human.indexOf('.') == -1) {
                // assume package java.lang
                human = "java.lang." + human;
            }
            result += 'L' + human.replace('.', '/') + ';';
        }
        return result;
    }

    /**
     * This parses a human-readable string into a sealer signature.
     * The human form is this:
     *    methodName(argType, ...)
     * where argTypes are specified according to humanToType()
     * (above). Note that a class name target is not part of the
     * signature--it is inherent in the sealer class.  The method is
     * implemented with a little state machine parser.
     */
    static public String humanToSignature(String human) {
        char[] orig = new char[human.length()];
        human.getChars(0, orig.length, orig, 0);
        char[] sig = new char[orig.length * 5]; // XXX--may not be enough
        int i = 0;
        int j = 0;
        int state = 0;
        int typeBegin = 0;
        machine: while (i < orig.length) {
            char c = orig[i];
            switch (state) {
                case 0: {
                    // expecting the method name until we see a '('
                    // or a space.
                    if ((c == '(') || (c == ' ')) {
                        if (i == 0) {
                            throw new RuntimeException(
                                "parse error from missing method name");
                        }
                        sig[j++] = '$';
                        sig[j++] = 'a';
                        sig[j++] = 's';
                        sig[j++] = 'y';
                        sig[j++] = 'n';
                        sig[j++] = 'c';
                        state = 1;
                    } else {
                        sig[j++] = c;
                        i++;
                    }
                    break;
                }
                case 1: {
                    // expecting zero or more spaces followed by a '('
                    // after getting '(' we expect some comma-separated
                    // types
                    if (c == ' ') {
                        i++;
                    } else if (c == '(') {
                        sig[j++] = c;
                        i++;
                        state = 2;
                    } else {
                        throw new RuntimeException(
                            "parse error after method name");
                    }
                    break;
                }
                case 2: {
                    // expecting zero or more spaces before a type name
                    // or a ')'
                    if (c == ' ') {
                        i++;
                    } else if (c == ')') {
                        state = 6;
                    } else {
                        typeBegin = i;
                        state = 3;
                    }
                    break;
                }
                case 3: {
                    // expecting a type name until a space, comma, or ')'
                    if ((c == ' ') || (c == ',') || (c == ')')) {
                        String sigType = humanToType(
                            new String(orig, typeBegin, i - typeBegin));
                        char[] typeChars = new char[sigType.length()];
                        sigType.getChars(0, typeChars.length, typeChars, 0);
                        for (int k = 0; k < typeChars.length; k++) {
                            sig[j++] = typeChars[k];
                        }
                        state = 4;
                    } else {
                        i++;
                    }
                    break;
                }
                case 4: {
                    // expecting spaces until a comma or ')'
                    if (c == ' ') {
                        i++;
                    } else if (c == ',') {
                        i++;
                        state = 5;
                    } else if (c == ')') {
                        state = 6;
                    } else {
                        throw new RuntimeException(
                            "parse error after type name");
                    }
                    break;
                }
                case 5: {
                    // expecting spaces until anything but a ')'
                    if (c == ')') {
                        throw new RuntimeException(
                            "parse error from missing type");
                    } else if (c == ' ') {
                        i++;
                    } else {
                        state = 2;
                    }
                    break;
                }
                case 6: {
                    // only got here if we encountered a ')' in an
                    // appropriate place--i.e. it's a stop state
                    i++;
                    sig[j++] = ')';
                    sig[j++] = 'V';
                    break machine;
                }
            }
        }
        if (state == 0) {
            throw new RuntimeException("parse error from missing '('");
        } else if (state != 6) {
            throw new RuntimeException("parse error from missing ')'");
        } else if (i != orig.length) {
            throw new RuntimeException("parse error from extra chars");
        }
        return new String(sig, 0, j);
    }

    /**
     * This turns a dynamic method specification consisting of a method
     * (verb) name and an argument count into a sealer signature string.
     */
    static public String dynamicMethodToSignature(String name, int count) {
        String result = name + "$async(";
        while (count > 0) {
            result += "Ljava/lang/Object;";
            count--;
        }
        result += ")Ljava/lang/Object;";
        return result;
    }

    abstract public void invoke(Object target, Object[] args) throws Exception;
}
