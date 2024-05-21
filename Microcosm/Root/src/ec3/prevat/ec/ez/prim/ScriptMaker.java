package ec.ez.prim;


import ec.ez.runtime.*;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NameTableEditorImpl;
import java.util.Enumeration;
import java.util.Hashtable;
import java.math.BigInteger;


/**
 * XXX Violates the rules of the vat.  This one is hard, so we should
 * make it a steward.  (We tried, but ran into the #$@%! circular
 * dependency problem.)
 */
public class ScriptMaker {

    static final ScriptMaker TheOne = new ScriptMaker();

    /** maps java classes to scripts */
    private Hashtable myScripts;

    /** maps java classes to the classes that sugar them */
    private Hashtable mySugars;

    private ScriptMaker() {
        myScripts = new Hashtable();

        mySugars = new Hashtable();
        mySugars.put(EZBoolean.OTHER_TYPE,      EZBoolean.TYPE);
        mySugars.put(EZDouble.OTHER_TYPE,       EZDouble.TYPE);
        mySugars.put(EZInteger.OTHER_TYPE,      EZInteger.TYPE);
        mySugars.put(EZEnumeration.OTHER_TYPE,  EZEnumeration.TYPE);
        mySugars.put(EZString.OTHER_TYPE,       EZString.TYPE);
    }

    static public ScriptMaker theOne() {
        return TheOne;
    }

    public Script instanceScript(Class clazz) {
        Script result = (Script)myScripts.get(clazz);
        if (result == null) {
            NameTableEditor methods = new NameTableEditorImpl();
            try {
               InstanceMethodNode.defineMembers(methods, clazz);
               result = optSweeten(methods, clazz, clazz);
                if (result != null) {
                    return result;
                }

            } catch (AlreadyDefinedException ex) {
                throw new RuntimeException("XXX is not yet implemented");
            }
            result = new SimpleScript(methods);
            myScripts.put(clazz, result);
        }
        return result;
    }


    private Script optSweeten(NameTableEditor methods,
                              Class actual, Class optClazz)
         throws AlreadyDefinedException {

        Script result;
        if (optClazz == null) {
            //the first base case
            return null;
        }
        Class sugar = (Class)mySugars.get(optClazz);
        if (sugar != null) {
            //the other base case
            methods = methods.sprout();
            SugarMethodNode.defineMembers(methods, sugar);
            result = new SimpleScript(methods);
            myScripts.put(actual, result);
            return result;
        }

        //recursive cases
        //XXX just takes the first match, instead of worrying about conflict

        result = optSweeten(methods, actual, optClazz.getSuperclass());
        if (result != null) {
            return result;
        }
        Class[] faces = optClazz.getInterfaces();
        for (int i = 0; i < faces.length; i++) {
            result = optSweeten(methods, actual, faces[i]);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
