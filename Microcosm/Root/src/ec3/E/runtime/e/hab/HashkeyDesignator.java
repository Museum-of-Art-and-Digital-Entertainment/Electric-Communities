package ec.e.hab;
import ec.e.rep.steward.Repository;
import ec.cert.CryptoHash;
import java.io.IOException;

/**
  A normal direct Designator
*/
public class HashkeyDesignator extends DirectDesignator {

    /**
    * The key for getting the designated object.
    */
    private CryptoHash key;

    /**
    * Make a direct Designator given the hash of the object designated.
    *
    * @param key The cryptographic hash of the object being designated
    */
    public HashkeyDesignator(CryptoHash key) {
        this.key = key;
    }

    /**
    * Make a direct Designator given the object designated itself.
    *
    * @param designee The designated object.
    */
    /* not yet
    public HashkeyDesignator(HaberdasheryObject designee) {
        this.key = new CryptoHash(designee);
    } */

    /**
    * Retrieve an instance of the designated object.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @return The object which this direct Designator designates.
    */
    HaberdasheryObject get(Repository rep) {
        HaberdasheryObject result = (HaberdasheryObject) rep.get((Object) key);
        if (result == null)
            throw new HaberdasherException("no such object");
        return(result);
    }

    /**
    * Delete the designated object from this Haberdashery.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    */
    void delete(Repository rep) throws HaberdasherException{
        try {
            rep.delete(key);
        } catch (IOException iox) {
            throw new HaberdasherException("repository delete failed");
        }
    }

    /**
    * Convert a hashkey into printable form.
    */
    static public String keyToString(CryptoHash key) {
        return(key.asHex());
    }

    /**
    * Return a pretty string representation.
    */
    public String toString() {
        return(key.toString());
    }
}
