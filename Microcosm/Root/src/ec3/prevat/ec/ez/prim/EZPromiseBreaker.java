package ec.ez.prim;
import ec.e.run.*;

public class EZPromiseBreaker extends InternalECatchClosure {
    EUniDistributor_$_Intf distributor;

    public EZPromiseBreaker(EUniDistributor_$_Intf theDistributor) {
            distributor = theDistributor;
    }

    public void catchMe(Throwable e) {
       if(distributor != null) {
         //   distributor.breakPromise$async(e.getMessage());
       }
    }
}