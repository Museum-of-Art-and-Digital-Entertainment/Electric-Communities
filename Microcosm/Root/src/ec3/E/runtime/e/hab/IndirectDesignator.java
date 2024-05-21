package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  Abstract base class for all indirect Designators.
*/
abstract public class IndirectDesignator extends Designator {

    /**
    * Since this is an abstract class, this constructor should never be called
    * directly.
    */
    protected IndirectDesignator() {
    }

    /**
    * Determine the DirectDesignator that currently corresponds to a given
    * IndirectDesignator via a given root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param rootUnit A root unit for looking up pathnames.
    * @return A DirectDesignator for the (indirectly) designated object.
    */
    abstract DirectDesignator determine(Repository rep, Unit rootUnit);

    /**
    * Determine the DirectDesignator that currently corresponds to a given
    * IndirectDesignator via a designated root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param root The (direct) Designator of root unit for resolving pathnames.
    * @return A DirectDesignator for the (indirectly) designated object.
    */
    DirectDesignator determine(Repository rep, Designator root) {
        return(determine(rep, (Unit) ((DirectDesignator)root).get(rep)));
    }

    /**
    * Retrieve an instance of the designated object via a given root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param unit The root unit for fetching the object.
    * @return The object which this indirect Designator currently designates.
    */
    HaberdasheryObject get(Repository rep, Unit unit) {
        return(determine(rep, unit).get(rep));
    }

    /**
    * Retrieve an instance of the designated object via a designated root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param root The root unit designation for fetching the object.
    * @return The object which this indirect Designator currently designates.
    */
    HaberdasheryObject get(Repository rep, Designator root) {
        return(determine(rep, root).get(rep));
    }

    /**
    * Delete an object from this Haberdashery via a given root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param unit The root unit for fetching the object.
    */
    void delete(Repository rep, Unit unit) {
        determine(rep, unit).delete(rep);
    }

    /**
    * Delete an object from this Haberdashery via a designated root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param root The DirectDesignator of a root unit for looking up pathnames.
    */
    void delete(Repository rep, Designator root) {
        determine(rep, root).delete(rep);
    }

    /**
    * Test if this Designator is fully determined (it can't be).
    *
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    * @return false, since an indirect Designator is never determined.
    */
    boolean isFullyDetermined(Repository rep, Unit rootUnit) {
        return(false);
    }

    /**
    * Test if this Designator is fully specified.
    *
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    * @return true iff the designated object is fully specified.
    */
    boolean isFullySpecified(Repository rep, Unit rootUnit) {
        return(get(rep, rootUnit).isFullySpecified(rep, rootUnit));
    }
}
