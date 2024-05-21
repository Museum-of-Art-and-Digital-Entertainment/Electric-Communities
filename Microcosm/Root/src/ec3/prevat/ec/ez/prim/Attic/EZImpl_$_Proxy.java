package ec.ez.prim;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.e.run.*;

/**
 * What a dispatch expression evaluates to.
 */
public class EZImpl_$_Proxy extends EObject_$_Proxy implements EZObject {

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection {

        Promise_$_Impl replyChannel = new Promise_$_Impl();
        PromiseDistributor_$_Intf distrib = (PromiseDistributor_$_Intf) replyChannel.distributor ();
        EZEnvelope env = new EZEnvelope(verb, args, distrib);
        RtRun.enqueue((EObject_$_Intf) this, env);
        return(replyChannel);
    }
}