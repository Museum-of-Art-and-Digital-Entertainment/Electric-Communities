package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  Abstract base class for Objects which designate templates stored by the
  Haberdashery.
*/
abstract public class Designator {

    /**
    * Since this is an abstract class, this constructor should never be called
    * directly.
    */
    protected Designator() {
    }

    /**
    * Test if this is a direct Designator.
    * @return true if direct, false if indirect.
    */
    public boolean isDirect() {
        /* Normally this is false. DirectDesignator will override. */
        return(false);
    }

    /**
    * Test if this is the 'null' Designator.
    * @return true iff this is the 'null' Designator.
    */
    public boolean isNull() {
        /* Normally this is false. NullDesignator will override. */
        return(false);
    }

    /**
    * Get the one and only 'null' Designator.
    * @return The 'null' Designator.
    */
    public static Designator getNull() {
        return(NullDesignator.getSelf());
    }

    /**
    * Test if this is the 'unknown' Designator.
    * @return true iff this is the 'unknown' Designator.
    */
    public boolean isUnknown() {
        /* Normally this is false. UnknownDesignator will override. */
        return(false);
    }

    /**
    * Get the one and only 'unknown' Designator.
    * @return The 'unknown' Designator.
    */
    public static Designator getUnknown() {
        return(UnknownDesignator.getSelf());
    }

    /**
    * Test if this is the 'root' Designator.
    * @return true iff this is the 'root' Designator.
    */
    public boolean isRoot() {
        /* Normally this is false. RootDesignator will override. */
        return(false);
    }

    /**
    * Get the one and only 'root' Designator.
    *
    * @return The 'root' Designator.
    */
    public static Designator getRoot() {
        return(RootDesignator.getSelf());
    }

    /**
    * Test if this Designator is fully determined (it can't be).
    *
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    * @return false, since an indirect Designator is never determined.
    */
    abstract boolean isFullyDetermined(Repository rep, Unit rootUnit);

    /**
    * Test if this Designator is fully specified.
    *
    * @param rep The Repository that stores the Haberdashery's object.
    * @param rootUnit The root unit for fetching indirectly designated objects.
    * @return true iff the designated object is fully specified.
    */
    abstract boolean isFullySpecified(Repository rep, Unit rootUnit);
}
