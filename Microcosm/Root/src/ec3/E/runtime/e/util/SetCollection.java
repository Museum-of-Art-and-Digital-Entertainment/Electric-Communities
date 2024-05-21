package ec.e.util;

import java.util.Hashtable;
import java.util.Enumeration;


/**
   A collection of elements resembling a set.
*/
public class SetCollection 
{
    private Hashtable elements;

    /**
       Create a new empty set.
    */
    public SetCollection() {
        elements = new Hashtable();
    }

    /**
       Return the number of elements in the set.
    */
    public int size() {
        return elements.size();
    }

    /**
       Add an element to the set.  Has no effect if the element is
       already a member of the set.
    */
    public void addElement(Object elt) {
        elements.put(elt, elt);
    }

    /**
       Remove an element from the set.  Has no effect if the element
       is not a member of the set.
    */
    public void removeElement(Object elt) {
        elements.remove(elt);
    }

    /**
       Returns true if the object is an element of the set.
    */
    public boolean contains(Object elt) {
        return elements.containsKey(elt);
    }

    /**
       Returns a new set that contains all elements in both this set
       and the other set.
    */
    public SetCollection intersection(SetCollection other) {
        SetCollection ret = new SetCollection();
        SetCollection shorter;
        SetCollection longer;
        
        if (other.size() < this.size()) {
            shorter = other;
            longer = this;
        }
        else {
            shorter = this;
            longer = other;
        }
        
        Enumeration en = shorter.enumerate();
        while (en.hasMoreElements()) {
            Object elt = en.nextElement();
            if (longer.contains(elt)) {
                ret.addElement(elt);
            }
        }
        return ret;
    }

    /**
       Returns a new set that contains all elements that are in either
       this set or the other set.
    */
    public SetCollection union(SetCollection other) {
        SetCollection ret = new SetCollection();
        Enumeration en = enumerate();
        while (en.hasMoreElements()) {
            ret.addElement(en.nextElement());
        }
        en = other.enumerate();
        while (en.hasMoreElements()) {
            ret.addElement(en.nextElement());
        }
        return ret;
    }

    /**
       Returns a new set that contains all elements that are in this
       set, but not in the other set.
    */
    public SetCollection difference(SetCollection other) {
        SetCollection ret = new SetCollection();
        Enumeration en = enumerate();
        while (en.hasMoreElements()) {
            ret.addElement(en.nextElement());
        }
        en = other.enumerate();
        while (ret.size() > 0 && en.hasMoreElements()) {
            ret.removeElement(en.nextElement());
        }
        return ret;
    }

    /**
       Returns an Enumeration of all of the elements in the set.
    */
    public Enumeration enumerate() {
        return elements.elements();
    }
}
