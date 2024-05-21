package ec.e.rep.crew;

import ec.cert.CryptoHash;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.SuperRepository;
import ec.e.start.EEnvironment;
import ec.e.start.crew.CrewCapabilities;
import ec.util.NestedException;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

/**

 * CREW classes can use this class to access the SuperRepository.
 * Later we may well decide to use different
 * Respositories for CREW and GUEST code, but for the time
 * being this is how we do it. <p>

 * While you can have as many SuperRepositories as you want, one of
 * them is privileged in that it is the value of theOne and many CREW
 * classes will use simple static methods in this class for all their
 * Repository accesses.<p>

 * This class is never instantiated. It only provides a number of
 * static methods for convenience to CREW code that needs to use the
 * StandardRepository in a read-only fashion.

 */

public class CrewRepository {
    private static SuperRepository theOne = null; // Privileged SuperRepository, made available to CREW.

    /**

     * Allow CREW components easy access to the SuperRepository - Get
     * an object stored under a given key. This is the preferred
     * method for framework programmers since it provides the best
     * error management and the most features (that is, it has
     * parimeterArguments).<p>

     * Note that the keys in the Repository can be of two kinds -
     * Symbols and "real" keys. This method uses the "real" keys -
     * these are typically CryptoHash objects. To use a symbol
     * (typically a String) as a key, use the one-argument version of
     * get() below or use throwingGet(), also below.

     * @param key - a Cryptohash object or other object to use as a
     * real key to the Repository.

     * @param parimeterArguments - a Hashtable of "parimeter" objects -
     * Objects on the perimeter of the stored object graph that were
     * pruned when storing the object. Current values for these
     * prune-point objects must now be given at decode time to allow
     * the decoded object to be correctly connected to existing
     * objects in the calling environment. This argument can be given
     * as null only if no parimeterArguments were needed when encoding the
     * object.

     * @return An object - Most often a byte[] (byte array) but could
     * potentially be any Java object.

     * @exception IOException

     */

    public static Object get(Object key, Hashtable parimeterArguments) throws IOException {
        if (theOne == null) {
            theOne = SuperRepository.summon();
            if (theOne == null) {
                theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            }
        }

        return theOne.get((CryptoHash)key, parimeterArguments);
    }

    /**

     * Allow CREW components easy access to the SuperRepository - Get
     * an object that has an entry in the SymbolTable. <p>

     * This method throws IOExceptions. If you want a cleaner
     * interface call that only throws RuntimeExceptions (that don't
     * have to be declared) use CrewRepository.getBySymbol()
     * instead. <p>

     * @param key - A String that is looked up in the Repository's
     * SymbolTable to yield a Cryptohash object to use as a real key
     * to the Repository.

     * @return An object - Most often a byte[] (byte array) but could
     * potentially be any Java object.

     * @exception IOException

     */

    public static Object throwingGetBySymbol(Object key) throws IOException {
        if (theOne == null) {
            theOne = SuperRepository.summon();
            if (theOne == null) {
                theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            }
        }
        Object cryptoKey = theOne.getCryptoHash(key);
        if (cryptoKey == null)
            throw new RepositoryKeyNotFoundException("System object named '" +
                                                     key + "' not in symbol table");
        return theOne.get((CryptoHash)cryptoKey);
    }

    /**

     * Allow CREW components easy access to the SuperRepository - Get
     * an object that has an entry in the SymbolTable.<p>

     * This method attempts to catch all exceptions under it and only
     * throws RuntimeException, to simplify code that calls it. If you
     * want to get thrown more differentiated exceptions, call
     * throwingGetBySymbol() instead. <p>

     * For now, for backward compatibility reasons, if the key is a
     * string that starts with "images/", then we delete that part of
     * the key string before looking it up.

     * @param key - A String that is looked up in the Repository's
     * SymbolTable to yield a Cryptohash object to use as a real key
     * to the Repository.

     * @return An object - Most often a byte[] (byte array) but could
     * potentially be any Java object.

     */

    public static Object getBySymbol(Object key) {
        if (theOne == null) {
            theOne = SuperRepository.summon();
            if (theOne == null) {
                theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            }
        }
        try {
            if (key instanceof String) { // XXX Temporary compatibility hack, remove when not needed anymore
                if (((String)key).startsWith("images/")) {
                    key = ((String)key).substring(7);
                }
            }

            Object cryptoKey = theOne.getCryptoHash(key);
            if (cryptoKey == null) {
                throw new RepositoryKeyNotFoundException("System object named '" +
                                                         key + "' not in symbol table");
            }
            return theOne.get((CryptoHash)cryptoKey);
        } catch (IOException iox) {
            throw new NestedException("Crew access to in-vat SuperRepository using key " + key, iox);
        }
    }

    /**

     * This is the "get" method that is being used by ByteArrayURLConnection.  It is being called
     * by constructor of ByteArrayURLConnection.

     */

    public static byte[] getImageBytes(Object key) {
        if (theOne == null) {
            theOne = SuperRepository.summon();
            if (theOne == null) {
                theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            }
        }
        try {
            Object cryptoKey = theOne.getCryptoHash(key);
            if (cryptoKey == null) {
                throw new RepositoryKeyNotFoundException("System object named '" +
                                                         key + "' not in symbol table");
            }
            return (byte[])theOne.get((CryptoHash)cryptoKey);
        } catch (IOException iox) {
            throw new NestedException("Crew access to in-vat SuperRepository using key " + key,iox);
        }
    }

    /**
     * Checks whether or not CrewRepository has been initialized.
     * Quietly attempts to initialize if not, but is prepared for it to fail.

     * @return true if yes, false otherwise
     */

    public static boolean exists() {
        try {
            if (theOne == null) {
                theOne = SuperRepository.summon();
                if (theOne == null) {
                    theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
                }
            }
        }
        catch (RuntimeException rtx) {
            return false;       // Nope.
        }
        if (theOne == null) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Checks whether or not image is stored in repository, needed during transition period
     * @return true if yes, false otherwise
     */

    public static boolean imageExists(String key) {
        if (! exists()) return false;
        try {
            return (theOne.getCryptoHash(key) == null) ? false : true;
        } catch (Exception iox) {
            return false;
        }
    }

    /**

     * Allow CREW components to use group vectors.

     * @param key - A String that is looked up in all repositories

     * @return A Vector - contains CryptoHash objects or other keys
     * for group members.

     */

    public static Vector getKeywordSet(Object keyword) {
        if (theOne == null) {
            theOne = SuperRepository.summon();
            if (theOne == null) {
                theOne = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            }
        }
        try {
            return theOne.getKeywordSet(keyword);
        } catch (Exception iox) {
            throw new NestedException("Crew access to keyword set in Repository using key " + keyword, iox);
        }
    }
}

