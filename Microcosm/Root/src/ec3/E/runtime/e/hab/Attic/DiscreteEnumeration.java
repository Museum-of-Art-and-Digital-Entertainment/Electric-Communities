package ec.e.hab;
import java.util.Enumeration;
import java.util.Vector;

/**
  A utility class that provides enumerations for small, fixed-size groups of
  objects.
*/
public class DiscreteEnumerationFactory {

    /**
    * Make the constructor private so nobody can make an instance!  This class
    * is purely computational.
    */
    private DiscreteEnumerationFactory() {
    }

    /** Make an enumeration for one object */
    static public Enumeration make(Object obj1) {
        Vector vector = new Vector(1);
        vector.addElement(obj1);
        return(vector.elements());
    }

    /** Make an enumeration for two objects */
    static public Enumeration make(Object obj1, Object obj2) {
        Vector vector = new Vector(2);
        vector.addElement(obj1);
        vector.addElement(obj2);
        return(vector.elements());
    }

    /** Make an enumeration for three objects */
    static public Enumeration make(Object obj1, Object obj2, Object obj3) {
        Vector vector = new Vector(3);
        vector.addElement(obj1);
        vector.addElement(obj2);
        vector.addElement(obj3);
        return(vector.elements());
    }

    /** Make an enumeration for four objects */
    static public Enumeration make(Object obj1, Object obj2, Object obj3,
                                   Object obj4) {
        Vector vector = new Vector(4);
        vector.addElement(obj1);
        vector.addElement(obj2);
        vector.addElement(obj3);
        vector.addElement(obj4);
        return(vector.elements());
    }

    /** Make an enumeration for five objects */
    static public Enumeration make(Object obj1, Object obj2, Object obj3,
                                   Object obj4, Object obj5) {
        Vector vector = new Vector(5);
        vector.addElement(obj1);
        vector.addElement(obj2);
        vector.addElement(obj3);
        vector.addElement(obj4);
        vector.addElement(obj5);
        return(vector.elements());
    }
}
