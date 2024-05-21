package ec.e.hab;
import java.util.Enumeration;
import java.util.Hashtable;

/**
  A Haberdashery directory unit.

  A unit maps from names (which are Strings) to DirectDesignators for the
  things pointed to by the unit. By entering the DirectDesignators of other
  Units into a Unit, you can create a hierarchical namespace.
*/
public class Unit extends HaberdasheryObject {
    /**
    * How we implement the mapping, internally.
    */
    private Hashtable dir;

    /**
    * Fetch an entry from the unit.
    *
    * @param name The name that you want to look up.
    * @return The DirectDesignator that the name maps to.
    */
    public DirectDesignator get(String name) {
        DirectDesignator result = (DirectDesignator) dir.get(name);
        if (result == null)
            throw new HaberdasherException("entry " + name +
                                           " not found in unit");
        return(result);
    }

    /**
    * Construct a new unit with an additional or changed entry.
    *
    * @param name The name you want to add.
    * @param entry The DirectDesignator that the name should map to.
    * @return A copy of the unit with the indicated entry.
    */
    public Unit copyWith(String name, DirectDesignator entry) {
        Hashtable newDir = (Hashtable) dir.clone();
        newDir.put(name, entry);
        return(new Unit(newDir));
    }

    /**
    * Construct a new unit with an entry deleted. It is an error if the unit
    * does not already map this name.
    *
    * @param name The name whose mapping you want to remove.
    * @return A copy of the unit with the indicated entry deleted.
    */
    public Unit copyWithout(String name) {
        if (!dir.containsKey(name))
            throw new HaberdasherException("entry " + name +
                                           " not found in unit");
        Hashtable newDir = (Hashtable) dir.clone();
        newDir.remove(name);
        return(new Unit(newDir));
    }

    /**
    * Construct an empty unit.
    */
    public Unit() {
        this.dir = new Hashtable();
    }

    /**
    * Internally, construct a unit with given mappings.
    *
    * @param dir The mappings Hashtable to use.
    */
    private Unit(Hashtable dir) {
        this.dir = dir;
    }

    /**
    * Enumerate the Designators this Unit maps to.
    *
    * @return An Enumeration of the Unit's (Direct)Designators.
    */
    public Enumeration designators() {
        return(dir.elements());
    }

    /**
    * Enumerate the names this Unit maps from.
    *
    * @return An Enumeration of the Units names.
    */
    public Enumeration names() {
        return(dir.keys());
    }

    /**
    * Test if this HaberdasheryObject is determined (it is).
    *
    * @return true: Units are always determined
    */
    public boolean isDetermined() {
        return(true);
    }

    /**
    * Test if this HaberdasheryObject is specified (it is).
    *
    * @return true: Units are always specified
    */
    public boolean isSpecified() {
        return(true);
    }
}
