package ec.ez.prim;

import ec.ez.runtime.MethodNode;
import ec.ez.runtime.Ejection;
import ec.ez.collect.NotFoundException;
import ec.ez.collect.Tuple;
import ec.ez.collect.TupleImpl;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

public abstract class JavaMemberNode extends MethodNode {

    private Member myMember;    

    protected JavaMemberNode(Member member) {
        myMember = member;
    }

    public String verb() {
        return myMember.getName();
    }

    public int arity() {
        return parameterTypes().length;
    }

    protected Member member() { return myMember; }

    protected abstract Class[] parameterTypes();

    protected Object[] coerceArgs(Object[] args) {
        Class[] paramTypes = parameterTypes();
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = coerce(args[i], paramTypes[i]);
        }
        return result;
    }

    protected abstract Object innerExecute(Object rec, Object[] args) 
         throws Exception;

    public Object execute(Object rec, String aVerb, Object[] args)
         throws Exception, Ejection {

        if (! verb().equals(aVerb) || arity() != args.length) {
            throw new NotFoundException(aVerb + "/" + args.length);
        }
        try {
            return innerExecute(rec, coerceArgs(args));

        } catch (InvocationTargetException ex) {
            Throwable th = ex.getTargetException();
            if (th instanceof Error) {
                throw (Error)th;
            } else if (th instanceof Ejection) {
                throw (Ejection)th;
            } else {
                throw (Exception)th;
            }
        }
    }

    protected abstract String explain(String args);

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("to " + verb());
        int highParam = arity() -1;
        String args = "";
        for (int i = 0; i < highParam; i++) {
            args += " p" + i + ",";
        }
        if (highParam >= 0) {
            args += " p" + highParam;
        }
        os.print(args + " {");
        lnPrintOn(os, indent+1, "java : \"" + explain(args.trim()) +  "\"");
        lnPrintOn(os, indent, "}");
    }

    static public Object coerce(Object obj, Class clazz) {
        if (clazz.isInstance(obj)) {
            return obj;
        } else { // XXX JAY - need a better generalized way of doing this!
            if((clazz == Double.TYPE) && (obj instanceof BigInteger)) {
                return new Double(((BigInteger) obj).doubleValue());
            }
            if((clazz == Integer.TYPE) && (obj instanceof BigInteger)) {
                return new Integer(((BigInteger) obj).intValue());
            }

            if(clazz == EZString.OTHER_TYPE) {
                return new String(obj.toString());
            }

            Class objClass = obj.getClass();

            if (clazz == Tuple.class
                && objClass.isArray()
                && ! objClass.isPrimitive()) {
                //Array -> Tuple
                return TupleImpl.make((Object[])obj);
            }

            if (clazz.isArray()
                && ! clazz.isPrimitive()
                && obj instanceof Tuple) {
                //Tuple -> Array
                return ((Tuple)obj).asArray();
            }

            //throw new ClassCastException("XXX4 not yet implemented");
            return obj; //let invoke sort it out
        }
    }
}

