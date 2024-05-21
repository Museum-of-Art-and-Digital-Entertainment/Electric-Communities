package ec.e.run;
import ec.ez.prim.PromiseDistributor_$_Intf;

public class EZServiceEnvelope extends EZEnvelope {

    EZServiceEnvelope(RtSealer sealer, Object[] args) {
        super(sealer, args);
    }

    public EZServiceEnvelope(String theVerb, Object[] args,
                 PromiseDistributor_$_Intf replyTo) {
        super((RtSealer) null, args);
        myVerb = theVerb;
        replyDistributor = replyTo;
    }
}
