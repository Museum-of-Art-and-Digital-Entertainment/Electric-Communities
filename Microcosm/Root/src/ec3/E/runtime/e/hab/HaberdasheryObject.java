package ec.e.hab;

import java.util.Enumeration;
import ec.e.util.EmptyEnumeration;
import ec.e.rep.steward.Repository;

/**
  Abstract base class for all objects that are to be stored by the
  Haberdashery.
*/
abstract public class HaberdasheryObject {

    /**
    * Since this is an abstract class, this constructor should never be called
    * directly.
    */
    protected HaberdasheryObject() {
    }

    /**
    * Enumerate the Designators which this HaberdasheryObject contains. All
    * HaberdasheryObjects are expected to be able to do this, so that methods
    * can walk the reference graph. Default is that object contains no
    * Designators.
    */
    public Enumeration designators() {
        return(new EmptyEnumeration());
    }

    /**
    * Test if this HaberdasheryObject is determined.
    *
    * @return true iff the HaberdasheryObject is determined.
    */
    public boolean isDetermined() {
        Enumeration designators = designators();
        while (designators.hasMoreElements()) {
            Designator designator = (Designator) designators.nextElement();
            if (!designator.isDirect())
                return(false);
        }
        return(true);
    }

    /**
    * Test if this HaberdasheryObject is specified
    *
    * @return true iff the HaberdasheryObject is specified.
    */
    public boolean isSpecified() {
        Enumeration designators = designators();
        while (designators.hasMoreElements()) {
            Designator designator = (Designator) designators.nextElement();
            if (designator.isUnknown())
                return(false);
        }
        return(true);
    }

    /**
    * Test if this HaberdasheryObject is fully determined.
    *
    * @return true iff the HaberdasheryObject is fully determined.
    */
    public boolean isFullyDetermined(Repository rep, Unit rootUnit) {
        Enumeration designators = designators();
        while (designators.hasMoreElements()) {
            Designator designator = (Designator) designators.nextElement();
            if (!designator.isDirect())
                return(false);
            if (!designator.isFullyDetermined(rep, rootUnit))
                return(false);
        }
        return(true);
    }

    /**
    * Test if this HaberdasheryObject is fully specified.
    *
    * @return true iff the HaberdasheryObject is fully specified.
    */
    public boolean isFullySpecified(Repository rep, Unit rootUnit) {
        Enumeration designators = designators();
        while (designators.hasMoreElements()) {
            Designator designator = (Designator) designators.nextElement();
            if (designator.isUnknown())
                return(false);
            if (!designator.isFullySpecified(rep, rootUnit))
                return(false);
        }
        return(true);
    }
}
