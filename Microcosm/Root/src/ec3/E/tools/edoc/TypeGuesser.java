/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */

package ec.edoc;

/** This class is pure kludge. What it does it collects a list of classes known
 *  to be eclasses or einterfaces. it then allows one to look up a named class
 *  to determine if it is (a) definitely an eclass or 
 *  (b) unknown, hence assumed to be a java class.
 */

class TypeGuesser {
    
    /** used to store the state of the classes */
    private java.util.Hashtable table;
    
    /* Construct a new guesser. For now this will populate the list.
     * if we are to use this system then this will be read in 
     * from a file. perhaps both. */
    TypeGuesser() {
        
        table = new java.util.Hashtable();
        
        table.put("ec.e.lang.EDouble", this);
        table.put("ec.e.lang.EFloat", this);
        table.put("ec.e.lang.EInteger", this);
        table.put("ec.e.lang.ELong", this);
        table.put("ec.e.lang.EString", this);
        table.put("ec.e.run.EBoolean", this);
        table.put("ec.e.run.EChannel", this);
        table.put("ec.e.run.EDistributor", this);
        table.put("ec.e.run.EFalse", this);
        table.put("ec.e.run.ENull", this);
        table.put("ec.e.run.EObject", this);
        table.put("ec.e.run.EPrintStream", this);
        table.put("ec.e.run.ESealer", this);
        table.put("ec.e.run.ETrue", this);
        table.put("ec.e.run.EUnsealer", this);
    }
    
    /** This method returns a guess as to the E-ness of that class.
     *  @param name, nullFatal; the type to check
     *  @return; true for a definite eclass, false for unknown */
    boolean guess(String name) {
    
        if (table.get(name) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    /** This method makes a note of a new class as an eclass */
    void remember(String nameOfEClass) {
        
        table.put(nameOfEClass, this);
    }
    
}
        