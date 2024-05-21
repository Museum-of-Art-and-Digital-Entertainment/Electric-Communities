package ec.ez.prim;

import ec.ez.runtime.MethodNode;
import java.lang.reflect.Method;


/**
 * Used to give EZStaticWrapper.myclass() the verb "class".
 */
public class RenamedInstanceMethodNode extends InstanceMethodNode {

    private String myVerb;

    RenamedInstanceMethodNode(Method method, String verb) {
        super(method);
        myVerb = verb;
   }

    public String verb() {
        return myVerb;
    }
}
