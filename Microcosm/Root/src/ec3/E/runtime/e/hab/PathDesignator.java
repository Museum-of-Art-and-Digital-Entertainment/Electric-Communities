package ec.e.hab;
import java.util.Enumeration;
import ec.e.rep.steward.Repository;

/**
  An ordinary indirect Designator
*/
public class PathDesignator extends IndirectDesignator {

    /**
    * The key for getting the designated object.
    */
    private UnitPath key;

    /**
    * Make an indirect Designator given the a path for the object designated.
    *
    * @param key A pre-digested unit path for the object designated.
    */
    public PathDesignator(UnitPath key) {
        this.key = key;
    }

    /**
    * Make an indirect Designator given the path as a string.
    *
    * @param key An FQN string that is the path for the object designated.
    */
    public PathDesignator(String pathString) {
        this.key = new UnitPath(pathString);
    }

    /**
    * Make an indirect Designator given a base path and a string.
    *
    * @param basePath Another PathDesignator to base upon.
    * @param pathString A path string to concatenate.
    */
    public PathDesignator(PathDesignator basePath, String pathString) {
        this.key = new UnitPath(basePath.key, pathString);
    }

    /**
    * Produce a PathDesignator for this designator's parent in the path.
    */
    PathDesignator parent() {
        UnitPath newKey = key.parent();
        if (newKey == null)
            return(null);
        else
            return(new PathDesignator(newKey));
    }

    /**
    * Return the last element in the pathname of the path we are holding onto.
    */
    String childName() {
        return(key.childName());
    }

    /**
    * Determine the DirectDesignator that currently corresponds to a given
    * IndirectDesignator via a given root unit.
    *
    * @param rep The Repository that stores the Haberdashery's objects.
    * @param unit A root unit for looking up pathnames.
    * @return A DirectDesignator for the (indirectly) designated object.
    */
    DirectDesignator determine(Repository rep, Unit unit) {
        Enumeration pathElems = key.elements();
        while (true) {
            String elem = (String) pathElems.nextElement();
            if (pathElems.hasMoreElements()) {
                unit = (Unit) unit.get(elem).get(rep);
            } else {
                return(unit.get(elem));
            }
        }
    }

    /**
    * Return a pretty string representation.
    */
    public String toString() {
        return(key.toString());
    }
}
