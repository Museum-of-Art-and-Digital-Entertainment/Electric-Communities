package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  A special direct Designator which explicitly designates no object.
*/
public class NullDesignator extends DirectDesignator {

    /**
    * The single static instance of itself.
    */
    final private static NullDesignator self = new NullDesignator();

    /**
    * Get the single static instance of this class.
    * @return The one and only 'null' Designator
    */
    static Designator getSelf() {
        return(self);
    }

    /**
    * A private constructor so nobody else can make one.
    */
    private NullDesignator() {
    }

    /**
    * Test if this is the 'null' Designator (it is).
    * @return true (all other Designators should return false).
    */
    public boolean isNull() {
        return(true);
    }

    /**
    * Retrieve an instance of the designated object.
    *
    * @param rep The Repository that holds the Haberdashery's objects (ignored)
    * @return The object designated -- in this case a null reference.
    */
    HaberdasheryObject get(Repository rep) {
        return(null);
    }

    /**
    * Delete the designated object from this Haberdashery. Of course, you can't
    * delete null!
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    */
    void delete(Repository rep) {
        throw new HaberdasherException("can't delete null");
    }

    /**
    * Return a pretty string representation.
    */
    public String toString() {
        return("0");
    }
}
