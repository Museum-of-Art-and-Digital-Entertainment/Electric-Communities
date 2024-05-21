package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * 
 */
public class WeakColumn 
extends RefColumn 
implements WeakColumnMarker {

    public WeakColumn(Class memberType) {
        super(memberType);
    }
}
