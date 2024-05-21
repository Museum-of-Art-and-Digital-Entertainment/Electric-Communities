package ec.ez.ezvm;
import ec.ez.runtime.*;

/**
 * In EZ, all execution happens within a Point-of-View.  Each object
 * has a point-of-view within which its methods execute.  The EZ
 * command line interpreter generally executes from the point of view of
 * some object, allowing the user to experience the situation of that
 * object. <p>
 *
 * @see ec.ez.ezvm.PovExpr
 * @see ec.ez.ezvm.EnterPovExpr
 */
public class Pov {

    private NameTable myEnv;
    private Script myScript;

    public Pov(NameTable env, Script script) {
        myEnv = env;
        myScript = script;
    }

    /**
     * What scope are nouns evaluated in?
     */
    public NameTable env() { return myEnv; }

    /**
     * This script is used if someone sends a message to an object
     * whose point of view is this Pov.
     */
    public Script script() { return myScript; }

    /**
     * A Pov just like this one but with a different environment.
     */
    public Pov withEnv(NameTable newEnv) {
        return new Pov(newEnv, myScript);
    }

    /**
     * A Pov just like this one but with a different script.
     */
    public Pov withScript(Script newScript) {
        return new Pov(myEnv, newScript);
    }
}

