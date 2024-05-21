package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  A special direct Designator which designates the root unit object.
*/
public class RootDesignator extends DirectDesignator {

    /**
    * The single static instance of itself.
    */
    final private static RootDesignator self = new RootDesignator();

    /**
    * The special key for storing the root unit in the Repository.
    */
    final private static String ROOT_KEY = "root";

    /**
    * Get the single static instance of this class.
    * @return The one and only 'root' Designator
    */
    static Designator getSelf() {
        return(self);
    }

    /**
    * A private constructor so nobody else can make one.
    */
    private RootDesignator() {
    }

    /**
    * Test if this is the 'root' Designator (it is).
    * @return true (all other Designators should return false).
    */
    public boolean isRoot() {
        return(true);
    }

    /**
    * Retrieve an instance of the designated object. The root unit is the
    * sole mutable object which the Haberdashery keeps in its Repository, so
    * it has a special key instead of using the cryptographic hash of its
    * contents.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @return The root unit object.
    */
    HaberdasheryObject get(Repository rep) {
        HaberdasheryObject result = (HaberdasheryObject) rep.get(ROOT_KEY);
        if (result == null)
            throw new HaberdasherException("no root in Haberdashery");
        return(result);
    }

    /**
    * Delete the designated object from this Haberdashery. Of course, you can't
    * delete the root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    */
    void delete(Repository rep) {
        throw new HaberdasherException("can't delete the root unit");
    }

    /**
    * Let insiders find out about the root key.
    *
    * @return The root key object.
    */
    static public Object getRootKey() {
        return(ROOT_KEY);
    }

    /**
    * Return a pretty string representation.
    */
    public String toString() {
        return(".");
    }
}
