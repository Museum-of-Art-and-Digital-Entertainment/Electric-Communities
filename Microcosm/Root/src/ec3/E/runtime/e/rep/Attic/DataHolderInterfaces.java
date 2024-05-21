package ec.e.rep;
import java.io.IOException;

/**

 * Anyone using ExpectedDataHolders needs to define a callback object that
 * implements the DataRequestor interface. It will get called with the
 * data when the data becomes available. <p>

 * If there is a problem, including timeout, then the handleFailure
 * method gets called with an Exception object.

 */

public interface DataRequestor {
    void acceptData(Object data, DataHolder yourHolder);
    void handleFailure(Exception failure, DataHolder yourHolder);
}

public interface DataHolder {
    Object held() throws IOException;              // Attempt to get the data synchronously
    void giveDataTo(DataRequestor requestor); // Request to get the data asynchronously
}

/**
  
 * A Fulfiller is a general policy object for data retrieval. It
 * manages repositories, caches, and data retrieval from the network
 * based on complete URL's and partial URL's (known as hints). <p>

 */

public interface Fulfiller {
    public Object getFromRepository(Object key) throws IOException;
    public void requestURLRetrieval(Object key, DataHolder yourHolder);
}

/**
  
 * A SimpleRepository knows how to retrieve simple objects locally.
 * An object is simple if it does not need any support at decode time
 * such as a perimeter object table or decoding properties table.

 */

public interface SimpleRepository {
    public Object get(Object key);
    public void reOpenRepository();
}

