package ec.e.rep.steward;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
  
 * A SimpleRepository knows how to retrieve simple objects locally.
 * An object is simple if it does not need any support at decode time
 * such as a perimeter object table or decoding properties table.<p>

 * An attempt to access potentially non-simple objects through this
 * interface may throw a RepositoryNeedParameterException (a subclass
 * of IOException). <p>

 * A request for a record with a key that cannot be found will throw a
 * RepositoryKeyNotFoundException, also a subclass of IOException

 */

public interface SimpleRepository {
    public Object getCryptoHash(Object symbol) throws IOException; // Symbol table lookup only
    public Object maybeGet(Object key);               // Returns object or null
    public Object get(Object key) throws IOException; // Returns object or throw
    public Object maybeGetBySymbol(Object symbol);    // Get object by symbolic name or null
    public Object getBySymbol(Object symbol) throws IOException; // Get object by symbolic name or throw
    public Vector getAll(Object symbol);              // Get all occurrences of symbol
    public Vector getKeywordSet(Object keyword);      // Get keyword Vector for given keyword
}
