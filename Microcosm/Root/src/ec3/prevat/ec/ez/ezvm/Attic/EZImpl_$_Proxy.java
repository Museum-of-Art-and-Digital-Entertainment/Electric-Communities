package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.runtime.Script;
import ec.ez.collect.NameTable;
import ec.ez.prim.Promise_$_Impl;
import ec.ez.prim.PromiseDistributor_$_Intf;
import ec.e.run.*;


/**
 * XXX Is this a proxy to a remote EZImpl?  --MarkM
 */
public class EZImpl_$_Proxy extends EObject_$_Proxy implements EZObject {

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection {

        Promise_$_Impl replyChannel = new Promise_$_Impl();
        PromiseDistributor_$_Intf distrib
            = (PromiseDistributor_$_Intf) replyChannel.distributor();
        EZEnvelope env = new EZEnvelope(verb, args, distrib);
        RtRun.enqueue((EObject_$_Intf) this, env);
        return replyChannel;
    }
}
