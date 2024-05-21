package ec.e.rep.steward;

import ec.cert.CryptoHash;
import ec.e.hold.DataHolder;
import ec.e.hold.DataRequestor;
import ec.e.hold.Fulfiller;
import ec.e.hold.RemoteRetriever;
import ec.e.net.Registrar;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefMaker;
import ec.e.rep.RepositoryPublisher;
import ec.e.rep.steward.CacheRepository;
import ec.e.rep.steward.EDataRequestor;
import ec.e.rep.steward.ERepositoryPublisher;
import ec.e.rep.steward.RepositoryTether;
import ec.e.rep.steward.SuperRepository;
import ec.e.run.Trace;
import ec.e.start.EEnvironment;
import ec.e.start.SmashedException;
import ec.e.start.Tether;
import ec.util.NestedException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**

 * The RemoteDownloader attempts to fulfill DataHolders by downloading the data over the net
 * whenever the data in the DataHolder cannot be found in the local Repository.
 * if successful in getting the data, the data is saved in the CacheRepository
 * so it won't have to be donwloaded again.

 * The RemoteDownloader is STEWARD. The Fulfiller accesses it through its
 * ParimeterizedRepository reference.

 */

class RemoteDownloader implements RemoteRetriever {
    EEnvironment myEnv;

    RemoteDownloader(EEnvironment env) {
        myEnv = env;            // Do we really need this for anything??
    }

//     public void requestRetrieval(CryptoHash hash,
//                                  Hashtable parimeterArguments,
//                                  Vector myHints,
//                                  DataRequestor requestor) {


//         // XXX This simple loop needs to be replaced by some smarter policy
//         // before we accept more than one kind of downloader here.

//         for (int i = 0; i< myHints.size(); i++) {
//             Object hint = myHints.elementAt(i);
//             if (hint instanceof String) {

//                 // Hint is some URL-like object. These are ignored for now.

//             } else if (hint instanceof SturdyRef) {

//                 // For each sturdyref we want to download
//                 // we create a new downloader that handles that request
//                 // and then calls a callback to the requestor
//                 // with the result - success (data) or failure (exception).

//                 EDataRequestor downloader =
//                   new EDataRequestor(hash,(SturdyRef)hint, requestor);
//                 break;          // We use this one, and that's it - For now.
//             }
//             // Ignore anything in the hints vector that we don't understand,
//             // for future compatibility.
//         }
//     }


    /**

     * Requests that an attempt be made to find an object on the
     * network identified by a given CryptoHash and using the hints
     * (such as SturdyRefs and URL's and URL fragments and other hints
     * in yet to be determined formats). The object, if found, is
     * handed to a given DataRequestor (WITHOUT first being decoded to
     * an object) using a callback routine. The reason for letting the
     * requestor do its own decoding is that it might have parimeter
     * tables that it wants to use but does not want to hand them out
     * to anyone, and that the object may have come from a Repository
     * or even a web server with no parimeter environment
     * available. Besides, we save one decoding from the Repository
     * and one encoding to the wire in the server end.

     */

    public void requestByteRetrieval(CryptoHash hash,
                                     Vector myHints,
                                     DataRequestor requestor) {

        if (Trace.repository.debug && Trace.ON)
            Trace.repository.debugm
              ("RemoteDownloader.RequestByteRetrieval(" + hash + ") - hints is " + myHints);

        if (myHints == null) {
            NullPointerException npe =
              new NullPointerException("Null Hints Vector to requestByteRetrieval");
            requestor.handleFailure(npe,requestor);
            return;
        }

        if (hash == null) {
            NullPointerException npe =
              new NullPointerException("Null cryptohash argument to requestByteRetrieval");
            requestor.handleFailure(npe,requestor);
            return;
        }

        // XXX This simple loop needs to be replaced by some smarter policy
        // before we accept more than one kind of downloader here.

        for (int i = 0; i< myHints.size(); i++) {
            Object hint = myHints.elementAt(i);
            if (hint instanceof String) {

                // Hint is some URL-like object. These are ignored for now.

            } else if (hint instanceof SturdyRef) {

                // For each sturdyref we want to download
                // we create a new downloader that handles that request
                // and then calls a callback to the requestor
                // with the result - success (data) or failure (exception).

                EDataRequestor downloader = new EDataRequestor(hash,(SturdyRef)hint, requestor);
                break;          // We use this one, and that's it - For now.
            }
            // Ignore anything in the hints vector that we don't understand,
            // for future compatibility.
        }
    }
}
