package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.Script;
import ec.ez.runtime.ParseNode;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.Tuple;
import ec.ez.collect.TupleImpl;
import java.io.PrintStream;
import java.io.IOException;


public class MatchNode extends ParseNode implements Script {

    private Pattern myPattern;
    private Expr myBody;

    public MatchNode(Pattern pattern, Expr body) {
        myPattern = pattern;
        myBody = body;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("match ");
        myPattern.printOn(os, indent);
        os.print(" ");
        myBody.printAsBlockOn(os, indent);
    }

    public Pattern pattern() { return myPattern; }
    public Expr   body() { return myBody; }

    public Object execute(Object rec, String verb, Object[] args)
         throws Exception, Ejection {

        Tuple message = TupleImpl.run(verb, TupleImpl.make(args));
        NameTableEditor pov = ((EZImpl_$_Impl)rec).pov().sprout();
        //it *must* bind when it's directly executed, as opposed to when
        //it's in a switch
        return myBody.eval(myPattern.mustBind(pov, message));
    }
}

