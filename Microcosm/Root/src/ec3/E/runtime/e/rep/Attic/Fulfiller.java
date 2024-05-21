package ec.e.rep;

import java.util.Vector;
import java.util.Hashtable;
import java.io.IOException;
import ec.e.file.EDirectoryRootMaker;
import ec.e.rep.steward.SimpleRepository;
import ec.e.rep.steward.RepositoryAccessException;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.RepositoryNeedParameterException;
import ec.e.rep.steward.DataHolder;

/**

 * A Fulfiller is a Guest object that knows how to resolve DataHolders
 * to their specified objects. Fulfillers are created for you at
 * startup and when you connect to a new host and the appropriate
 * Fulfiller field is added to every ExpectedData holder you get from
 * local repositories or from the remote host by your own Comm system,
 * upon decode of the object.<p>

 * The URL to use to lookup or search for the data is constructed of
 * pieces that get appended together: The hints (several may be
 * available), and The cryptohash key (as a hex integer), and possibly
 * other stuff. The design of this is open-ended - Hints are specified
 * to be an array of strings but we don't specify (yet) a format for
 * them or how we will interpret them. <p>

 */

// Note: We must spend some effort in examining all constructed URL's
// to make sure that they look "normal" , so that noone can sneak in a
// URL that terminates before the cryptohash key is appended. This
// parser may need to detect other URL terminator strings besides "?".

public class Fulfiller {
    private Vector myHints = null;
    private Object myURLFetcher = null;
    private SimpleRepository myRepository = null;
    private Hashtable myParimeters = new Hashtable(10);

    /**
     * No-argument constructor. Creates a useless Fulfiller. Used in some Parimeter tables.
     */

    public Fulfiller() {
    }

    public Fulfiller(SimpleRepository repository, Object urlFetcher, Vector hints) {
        myRepository = repository;
        myURLFetcher = null;
        myHints = hints;

        // We build or parimeters table here. Add lines below if more needed.

        myParimeters.put("Repository",myRepository);
    }

    /**

     * We need to retrieve the data over the network.  Construct the
     * URL and invoke the URL getter capability. When the URL getter
     * has retrieved the data it calls
     * DataHolder.dataArrived(data)

     */

    // Currently not implemented. API not yet defined.

    public void requestURLRetrieval(Object key, DataHolder yourHolder) {
        if (myURLFetcher == null) return;
    }

    /**

     * Retrieve an object from our Repositories.

     * @param key untrusted notNull - A object key to use to retrieve
     * data from our Repositories.

     */

    public Object getFromRepository(Object key)
         throws RepositoryKeyNotFoundException, IOException {
             return myRepository.get(key,myParimeters);
    }
}
