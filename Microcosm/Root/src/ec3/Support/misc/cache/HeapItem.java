// HeapItem.java originally 1.0 96/04/14 Walter Korman (found on WWW)
// This version has been tweaked - Harry Richardson 18/02/97


package ec.misc.cache;

//
// HeapItem interface.  Must be implemented by any class which will
// be stored in the Heap class.  The print() method isn't really
// necessary, but can be useful for debugging/perusal of your
// data structures.
//
public interface HeapItem {

    public boolean greaterThan(HeapItem item);
    public void print();
}

