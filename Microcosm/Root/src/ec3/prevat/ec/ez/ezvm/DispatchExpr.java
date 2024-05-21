package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.ParseNode;
import ec.ez.runtime.Script;
import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NameTableEditorImpl;
import ec.ez.collect.Mapping;
import ec.ez.collect.NotFoundException;
import ec.ez.prim.EZUniversal;
import ec.ez.prim.SugarMethodNode;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;


/**
 * BNF: "dispatch" "{" method* [ match ] "}" <p>
 *
 * Yields an object that closes over the current scope, and responds
 * to requests by dispatching to one of its matching methods, or to
 * match if provided and none match.
 */
public class DispatchExpr extends Expr implements Script {

    private NameTableEditor myMethods;
    private MatchNode myOptOtherwise;

    public DispatchExpr(NameTableEditor optMethods,
                        MatchNode optOtherwise) {
        if (optMethods == null) {
            optMethods = new NameTableEditorImpl();
        }
        if (optOtherwise == null) {
            optMethods = optMethods.sprout();
            // XXX JAY - edit here to remove method spam.
            try {
                SugarMethodNode.defineMembers(optMethods, EZUniversal.TYPE);

            } catch (AlreadyDefinedException ex) {
                throw new RuntimeException("XXX1 not yet implemented " + ex);
            }
        }
        myMethods = optMethods;
        myOptOtherwise = optOtherwise;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("dispatch {");
        Mapping meths = myMethods.mapping();
        Enumeration iter = meths.asEnumeration();
        while (iter.hasMoreElements()) {
            ParseNode meth = (ParseNode)iter.nextElement();
            meth.lnPrintOn(os, indent+1);
        }
        if(myOptOtherwise != null) {
            myOptOtherwise.lnPrintOn(os, indent+1);
        }
        lnPrintOn(os, indent, "}");
    }

    public NameTableEditor methods() { return myMethods; }
    public MatchNode otherwise() { return myOptOtherwise; }

    public Object execute(Object rec, String verb, Object[] args)
         throws Exception, Ejection {

        String mangle = verb + "/" + args.length;
        Script next = null;
        try {
            next = (Script)myMethods.get(mangle);
        } catch (NotFoundException ex) {
            if (myOptOtherwise == null) {
                throw ex;
            }
            next = myOptOtherwise;
        }
        return next.execute(rec, verb, args);
    }

    public Object eval(NameTable pov) throws Exception {
        return new EZImpl_$_Impl(pov, this);
    }
}

