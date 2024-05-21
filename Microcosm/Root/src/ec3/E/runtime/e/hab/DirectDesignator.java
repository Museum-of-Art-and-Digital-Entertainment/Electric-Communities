package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  Abstract base class for all direct Designators.
*/
abstract public class DirectDesignator extends Designator {

    /**
    * Since this is an abstract class, this constructor should never be called
    * directly.
    */
    protected DirectDesignator() {
    }

    /**
    * Test if this is a direct Designator (it is).
    *
    * @return true
    */
    public boolean isDirect() {
        return(true);
    }

    /**
    * Get the designated object out of the Haberdashery.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @return The designated object.
    */
    abstract HaberdasheryObject get(Repository rep);

    /**
    * Delete the designated object from this Haberdashery.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    */
    abstract void delete(Repository rep);

    /**
    * Test if this Designator is fully determined.
    *
    * @return true iff the designated object is fully determined.
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    */
    boolean isFullyDetermined(Repository rep, Unit rootUnit) {
        return(get(rep).isFullyDetermined(rep, rootUnit));
    }

    /**
    * Test if this Designator is fully specified.
    *
    * @return true iff the designated object is fully specified.
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    */
    boolean isFullySpecified(Repository rep, Unit rootUnit) {
        return(get(rep).isFullySpecified(rep, rootUnit));
    }
}
