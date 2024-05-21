package ec.ez.prim;

import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.runtime.Ejection;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NameTableEditorImpl;
import ec.ez.collect.Mapping;
import ec.ez.collect.NotFoundException;
import ec.ez.prim.SugarMethodNode;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;


/**
 * 
 */
public class SimpleScript implements Script {

    private NameTableEditor myMethods;

    public SimpleScript(NameTableEditor methods) {
        myMethods = methods.sprout();
        try {
            SugarMethodNode.defineMembers(myMethods, EZUniversal.TYPE);
            
        } catch (AlreadyDefinedException ex) {
            throw new RuntimeException("XXX1 not yet implemented " + ex);
        }
    }

    public NameTableEditor methods() { return myMethods; }

    public Object execute(Object rec, String verb, Object[] args)
         throws Exception, Ejection {

        String mangle = verb + "/" + args.length;
        Script script = (Script)myMethods.get(mangle);
        return script.execute(rec, verb, args);
    }
}

