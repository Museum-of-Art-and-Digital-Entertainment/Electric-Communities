package ec.e.rep.steward;

import ec.e.hold.DataRequestor;
import ec.e.hold.DataHolder;
import ec.e.net.SturdyRef;
import ec.cert.CryptoHash;
import ec.e.rep.steward.EByteArray;

    /** 

     * The EDataRequestor objects resolve their SturdyRefs to a (remote)
     * ERepositoryPublisher object, then ask that publisher for the
     * data identified by the hash, and then pass the results to their
     * requestor - the fulfiller that asked for it.

     */

eclass EDataRequestor {
    EDataRequestor() {
    }

    EDataRequestor(CryptoHash hash, SturdyRef ref, DataRequestor requestor) {
        ERepositoryPublisher publisher =
          (ERepositoryPublisher) EUniChannel.construct(ERepositoryPublisher.class);
        EUniDistributor publisher_dist = EUniChannel.getDistributor(publisher);
        ref.followRef(publisher_dist);
        EByteArray b = (EByteArray) EUniChannel.construct(EByteArray.class);
        EUniDistributor b_dist = EUniChannel.getDistributor(b);
        
        etry {
            publisher <- getBytes((Object)hash, b_dist);
            ewhen b (byte[] data) {
                requestor.acceptByteData(data, requestor);
            }
        } ecatch (Exception e) { 
            e.printStackTrace();
            requestor.handleFailure(e, requestor);
        }
    }
}
