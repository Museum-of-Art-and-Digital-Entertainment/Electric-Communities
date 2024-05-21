package ec.ez.prim;

import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NameTableEditorImpl;


/**
 * How a Java class's static methods are made accessible to EZ
 */
public class EZStaticWrapper implements EZObject {

    private Class myClass;
    private Script myScript;

    public EZStaticWrapper(Class clazz) {
        myClass = clazz;
        NameTableEditor methods = new NameTableEditorImpl();
        try {
            StaticMethodNode.defineMembers(methods, clazz);
            ConstructorNode.defineMembers(methods, clazz);
        } catch (AlreadyDefinedException ex) {
            throw new RuntimeException("XXX3 not yet implemented " + ex);
        }
        myScript = new SimpleScript(methods);
    }

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection {

        return myScript.execute(this, verb, args);
    }

    public Class myclass () {
        return myClass;
    }
}

