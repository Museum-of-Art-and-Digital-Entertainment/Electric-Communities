package ec.ez.prim;

import ec.ez.runtime.MethodNode;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class ConstructorNode extends JavaMemberNode {

    public ConstructorNode(Constructor constr) {
        super(constr);
    }

    public String verb() {
        return "new";
    }

    public Class[] parameterTypes() {
        return ((Constructor)member()).getParameterTypes();
    }

    protected Object innerExecute(Object rec, Object[] args) 
         throws Exception {

        return ((Constructor)member()).newInstance(coerceArgs(args));
    }

    protected String explain(String args) {
        return "new " + member().getName() + "(" + args + ")";
    }

    static public void defineMembers(NameTableEditor vTable, Class clazz)
         throws AlreadyDefinedException {

        Constructor[] constrs = clazz.getConstructors();
        for (int i = 0; i < constrs.length; i++) {
            MethodNode meth = new ConstructorNode(constrs[i]);
            try {
                vTable.introduce(meth.mangle(), meth);
            } catch (AlreadyDefinedException e) {
                // JAY - Eat exceptions due to duplicate constructors
            }
        }
    }
}

