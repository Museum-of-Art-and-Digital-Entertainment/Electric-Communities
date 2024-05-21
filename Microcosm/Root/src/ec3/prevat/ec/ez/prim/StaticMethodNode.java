package ec.ez.prim;

import ec.ez.runtime.MethodNode;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 *
 */
public class StaticMethodNode extends JavaMemberNode {

    public StaticMethodNode(Method method) {
        super(method);
        if (! Modifier.isStatic(method.getModifiers())) {
            throw new Error("internal: not a static method");
        }
    }

    public Class[] parameterTypes() {
        return ((Method)member()).getParameterTypes();
    }

    protected Object innerExecute(Object rec, Object[] args) 
         throws Exception {

        Method meth = (Method)member();
        return meth.invoke(null, coerceArgs(args));
    }

    protected String explain(String args) {
        return member().getDeclaringClass().getName() + "."
                + verb() + "(" + args + ")";
    }

    static public void defineMembers(NameTableEditor vTable, Class clazz)
         throws AlreadyDefinedException {

        Method[] meths = clazz.getMethods();
        for (int i = 0; i < meths.length; i++) {
            if (Modifier.isStatic(meths[i].getModifiers())) {
                MethodNode meth = new StaticMethodNode(meths[i]);
                vTable.introduce(meth.mangle(), meth);
            }
        }
        vTable.introduce(TheClassVerb.mangle(), TheClassVerb);
    }

    static private final MethodNode TheClassVerb = classVerb();

    //This indirect nonesense is needed because static init
    //expressions may not have Exceptions in need of declaration.
    static private MethodNode classVerb() {
        Method classMethod;
        try {
            classMethod = Class.forName("ec.ez.prim.EZStaticWrapper")
                     .getDeclaredMethod("myclass", new Class[0]);

        } catch (ClassNotFoundException ex) {
            throw new Error("where's EZStaticWrapper? " + ex);
        } catch (NoSuchMethodException ex) {
            throw new Error("where's EZStaticWrapper.myclass()? " + ex);
        }
        return new RenamedInstanceMethodNode(classMethod, "class");
    }
}

