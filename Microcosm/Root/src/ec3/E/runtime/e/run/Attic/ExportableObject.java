// E-Support, (C) Electric Communities (and Michael Philippsen),
// 1997, all rights reserved.
// =============================================================

package ec.e.run;

import ec.e.net.NetIdentityMaker;

public abstract class ExportableObject implements Exportable
{
    // Identity is maintained invariant for all exports of this Object
    private long identity = 0L;

    public final long getIdentity() {
        if (identity == 0L) {
            identity = NetIdentityMaker.nextIdentity();
        }
        return identity;
    }
    
    public abstract void invoke(RtSealer sealer, Object[] args, RtExceptionEnv ee);
}   

