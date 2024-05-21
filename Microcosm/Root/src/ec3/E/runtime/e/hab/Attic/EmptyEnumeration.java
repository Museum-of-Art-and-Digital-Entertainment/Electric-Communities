package ec.e.hab;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
  A utility class that provides an enumeration with no elements in it.
  Arguably this should be in some more generally useful package than ec.e.hab
  (arguably this should have been part of the standard Java class library all
  along).
*/
public class EmptyEnumeration implements Enumeration {

    /**
    * All purpose constructor
    */
    public EmptyEnumeration() { }

    /**
    * Test if this Enumeration has more elements (hint: it doesn't).
    *
    * @return false, always
    */
    public boolean hasMoreElements() {
        return(false);
    }

    /**
    * Get the (non-existent) next element in this Enumeration.
    *
    * @return Never returns, always throws a NoSuchElementException
    */
    public Object nextElement() {
        throw(new NoSuchElementException("EmptyEnumeration never has elements"));
    }
}
