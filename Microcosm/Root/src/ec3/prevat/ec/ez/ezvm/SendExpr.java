package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;
import ec.e.run.*;
import ec.ez.prim.EZEObjectWrapper_$_Impl;
import ec.ez.prim.EZSealer;

/**
 * BNF: expr "<-" request <p>
 *
 * Tells the expression's value to perform the request at some later
 * time, in its own independent event.
 */
public class SendExpr extends Expr {

    private Expr myRecipient;
    private RequestNode myRequest;

    public SendExpr(Expr recipient, RequestNode request) {
        myRecipient = recipient;
        myRequest = request;
    }

    public SendExpr(Expr recipient, String verb, Expr[] args) {
        this(recipient, new RequestNode(verb, args));
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("(");
        myRecipient.printOn(os, indent);
        os.print(" <- ");
        myRequest.printOn(os, indent);
        os.print(")");
    }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object receiver = myRecipient.eval(pov);
        Expr[] argNodes = myRequest.args();
        Object[] args = new Object[argNodes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = argNodes[i].eval(pov);
        }
        return applyLater(receiver, myRequest.verb(), args);
    }

    static public Object applyLater(Object rec, String verb, Object[] args) {
        EUniChannel replyChannel = new EUniChannel();
        EUniDistributor_$_Intf distrib = replyChannel.getDistributor();
        EObject_$_Deflector promiseObj = new EObject_$_Deflector(replyChannel, replyChannel);

        RtExceptionEnv exEnv = new RtExceptionEnv(distrib, null);

        RtEnvelope env = EZSealer.makeEnvelope(verb, args, exEnv);
        if(!(rec instanceof EObject_$_Intf)) {
            EZEObjectWrapper_$_Impl wrapper = new EZEObjectWrapper_$_Impl(rec);
            RtRun.enqueue(wrapper, env);
        } else {
            RtRun.enqueue((EObject_$_Intf) rec, env);
        }
        return(promiseObj);
    }

 /*
   static public Object applyLater(Object rec, String verb, Object[] args) {
        EChannel replyChannel = new EChannel();
        EDistributor distrib = replyChannel.distributor ();
        EZEnvelope env = new EZEnvelope(verb, args, (Object) distrib);
        if(!(rec instanceof EObject)) {
            EZEObjectWrapper wrapper = new EZEObjectWrapper(rec);
            RtRun.enqueue(wrapper, env);
        } else {
            RtRun.enqueue((EObject_$_Intf) rec, env);
        }
        return(replyChannel);
     //   throw new RuntimeException("XXX2 Not yet implemented");
    }
*/

}

