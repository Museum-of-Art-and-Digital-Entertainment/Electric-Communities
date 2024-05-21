package ec.e.hab;
import ec.e.rep.steward.Repository;

/**
  A special indirect Designator which explicitly designates an unknown object.
*/
public class UnknownDesignator extends IndirectDesignator {

    /**
    * The single static instance of itself.
    */
    final private static UnknownDesignator self = new UnknownDesignator();

    /**
    * Get the single static instance of this class.
    * @return The one and only 'unknown' Designator
    */
    static Designator getSelf() {
        return(self);
    }

    /**
    * A private constructor so nobody else can make one.
    */
    private UnknownDesignator() {
    }

    /**
    * Test if this is the 'unknown' Designator (it is).
    * @return true (all other Designators should return false).
    */
    public boolean isUnknown() {
        return(true);
    }

    /**
    * Determine the DirectDesignator that currently corresponds to a given
    * IndirectDesignator via a given root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param unit A root unit for looking up pathnames.
    * @return Never returns, always throws a HaberdasherException
    */
    DirectDesignator determine(Repository rep, Unit rootUnit) {
        throw new HaberdasherException("the unknown designator is undeterminable");
    }

    /**
    * Return a pretty string representation.
    */
    public String toString() {
        return("?");
    }
}
