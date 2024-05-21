package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.Ejector;
import ec.ez.runtime.ParseNode;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.Tuple;
import ec.ez.prim.JavaMemberNode;
import java.io.PrintStream;
import java.io.IOException;


/**
 * A Pattern 1) "evaluates" in a scope, 2) matches some specimen
 * object, 3) binding names in this scope to values derived (usually
 * extracted) from the specimen, and 4) returns whether the match was
 * successful. 
 */
public abstract class Pattern extends ParseNode {

    /**
     * The pov already represents a "hypothetical" binding environment
     * that will only be used if the overall match succeeds.
     * Otherwise, it will be discarded, along with any bindings
     * introduce()d into it.  Success is indicated by returning a
     * NameTable representing the scope in which any "then"
     * computations should then be executed.  Failure is indicated by
     * returning null.
     *
     * @return nullOk;
     */
    public abstract NameTable testBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection;


    /**
     * Like testBind, except that it fails by throwing an exception
     * rather than returning null 
     */
    public NameTable mustBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection
    {
        NameTable result = testBind(pov, specimen);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("can't bind " + specimen);
    }


    /** 
     * This is the only form of a defining occurence of a noun in an
     * EZ program.  Always succeeds and binds the name to the
     * specimen. 
     */
    static public Pattern name(String varName) {
        return new PatternName(varName);
    }


    /**
     * Always succeeds while binding nothing.
     */
    static public Pattern ignore() {
        return new PatternIgnore();
    }


    /**
     * Succeeds iff sub matches the specimen, and test -- when evaluated
     * with the resulting bindings in scope -- evaluates to true.
     */
    static public Pattern suchThat(Pattern sub, Expr test) {
        return new PatternSuchThat(sub, test);
    }    


    /**
     *
     */
    static public Pattern tuple(Pattern[] subs, Pattern optRest) {
        return new PatternTuple(subs, optRest);
    }    
}


/*package*/ class PatternName extends Pattern {

    private String myVarName;

    /*package*/ PatternName(String varName) {
        myVarName = varName;
    }
        

    public NameTable testBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection
    {
        pov.introduce(myVarName, specimen);
        return pov;
    }        

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print(myVarName);
    }
}

/*package*/ class PatternIgnore extends Pattern {

    /*package*/ PatternIgnore() {}

    public NameTable testBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection
    {
        return pov;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("_");
    }
}

/*package*/ class PatternSuchThat extends Pattern {

    private Pattern mySubPattern;
    private Expr myTest;

    /*package*/ PatternSuchThat(Pattern sub, Expr test) {
        mySubPattern = sub;
        myTest = test;
    }

    public NameTable testBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection
    {
        NameTable newPov = mySubPattern.testBind(pov, specimen);
        if (newPov == null) {
            return null;
        }
        return myTest.testBind(newPov);
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        mySubPattern.printOn(os, indent);
        os.print(" : ");
        myTest.printOn(os, indent);
    }
}


/*package*/ class PatternTuple extends Pattern {

    private Pattern[] mySubs;
    private Pattern myOptRest;

    /*package*/ PatternTuple(Pattern[] subs, Pattern optRest) {
        mySubs = subs;
        myOptRest = optRest;
    }
        

    public NameTable testBind(NameTableEditor pov, Object specimen)
         throws Exception, Ejection
    {
        Object shouldBeTuple = JavaMemberNode.coerce(specimen, Tuple.class);
        if (! (shouldBeTuple instanceof Tuple)) {
            return null;
        }
        Tuple tuple = (Tuple)shouldBeTuple;
        int size = tuple.size();
        if (size < mySubs.length) {
            return null;
        }
        for (int i = 0; i < mySubs.length; i++) {
            pov = (NameTableEditor)mySubs[i].testBind(pov, tuple.index(i));
        }
        if (myOptRest == null) {
            if (size == mySubs.length) {
                return pov;
            } else {
                return null;
            }
        } else {
            return myOptRest.testBind(pov, tuple.slice(mySubs.length, size));
        }
    }        


    public void printOn(PrintStream os, int indent) throws IOException {
        printListOn("[", mySubs, ", ", "]", os, indent);
        if (myOptRest != null) {
            os.print(" + ");
            myOptRest.printOn(os, indent);
        }
    }
}

