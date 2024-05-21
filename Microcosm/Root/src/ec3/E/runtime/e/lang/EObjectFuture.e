package ec.e.lang;

/////////////////////////////////////////////////////////////
// Object futures 
/////////////////////////////////////////////////////////////

/**
*   Object futures for E and Java objects
*/

public eclass EObjectFuture {
    EObject myValue = null; // to avoid channel creation
    
    public EObjectFuture(EObject aValue) {
        myValue = aValue;
    }
    
    EObject value() {
        return myValue;
    }
}

public eclass jObjectFuture {
    Object myValue = null; // to avoid channel creation
    
    public jObjectFuture(Object aValue) {
        myValue = aValue;
    }
    
    Object value() {
        return myValue;
    }
}
