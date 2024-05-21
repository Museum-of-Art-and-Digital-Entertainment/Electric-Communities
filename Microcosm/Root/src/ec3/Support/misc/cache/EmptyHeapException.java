/*
 * @(#)EmptyHeapException.java  1.0 96/04/14 Walter Korman
 */

package ec.misc.cache;

/**
 * Signals that the heap is empty.
 * @version     1.0, 04/14/96
 * @author  Walter Korman
 * @see Heap
 */

public class EmptyHeapException extends RuntimeException {
    /**
     * Constructs a new EmptyHeapException with no detail message.
     */
    public EmptyHeapException() {}
}

