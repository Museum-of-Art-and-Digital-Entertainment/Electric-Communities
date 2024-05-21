package ec.util;

import java.util.Hashtable;
import java.util.Enumeration;

/* A read-only hashtable class. By KJD. 
   Instances are (well, contain) copies of regular hashtables that get
   frozen at creation time.  */

public class ReadOnlyHashtable {
    private Hashtable htab;
    
    public ReadOnlyHashtable(Hashtable ht) {
        htab = new Hashtable(ht.size() + 20);
        Enumeration e = ht.keys();
        
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            htab.put(key,ht.get(key));
        }
    }
    
    public Object get(Object key) {
        return htab.get(key);
    }
    
    public int size() {
        return htab.size();
    }
    
    public Enumeration keys() {
        return htab.keys();
    }
    
    public Enumeration elements() {
        return htab.elements();
    }
}
