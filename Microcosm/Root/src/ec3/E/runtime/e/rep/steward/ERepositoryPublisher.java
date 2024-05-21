package ec.e.rep.steward;

import ec.e.rep.RepositoryPublisher;
import ec.e.rep.steward.EDataRequestor;
import java.io.IOException;

/**

 * A companion class to RepositoryPublisher. This E object receives
 * the getBytes() E messages and asks the RepositoryPublisher for the
 * data and then returns data or exception to the requestor using the
 * channel that accompanies the request.

 * The ERepositoryPublisher (there is only one in each vat) is a GUEST
 * and stays around forever. The RepositoryPublisher that services
 * *our* repository requests is self-healing after a quake so we don't
 * have to worry about it.

 */

eclass ERepositoryPublisher {
    RepositoryPublisher myPublisher = null;

    public ERepositoryPublisher(RepositoryPublisher publisher) {
        myPublisher = publisher;
    }

    // Service a request for data

    emethod getBytes(Object key, EResult requestor) throws Exception {
        System.out.println("ERepositoryPublisher getBytes() called - key is " + key);
        try {
            byte[] result = myPublisher.getBytes(key);
            if (result != null) {
                System.out.println("ERepositoryPublisher returns data to requestor: " + result);
                requestor <- forward(new EByteArray(result));
            }
            else ethrow(new Error("Shouldn't happen - publisher returned null but did not throw"));
        } catch (Exception e) {
            ethrow(e);
        }
    }
}


