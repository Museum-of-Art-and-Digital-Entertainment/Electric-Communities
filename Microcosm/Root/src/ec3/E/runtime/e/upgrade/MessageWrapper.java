package ec.e.upgrade;

public class MessageWrapper
{
    public RtSealer sealer;
    public Object[] args;
    public RtExceptionEnv ee;
    
    public MessageWrapper (RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        this.sealer = sealer;
        this.args = args;
        this.ee = ee;
    }   
}   