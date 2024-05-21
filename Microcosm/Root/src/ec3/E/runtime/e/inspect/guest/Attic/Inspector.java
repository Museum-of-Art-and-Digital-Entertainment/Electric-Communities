package ec.e.inspect.guest;
import java.util.Hashtable;

class Inspector {

    /**

     * gather() (in its six variants - three here and three in
     * crew.inspector) will gather the 'object' argument into a
     * hashtable under a given or generated string key. The user can
     * select any of these collected objects for inspection at any
     * time.

     */

    /**

     * gather - collect an object for possible later inspection.

     * @param object, nullOK, suspect - The object to be
     * gathered. You can only mark any object for inspection once so
     * if it is already been marked in another category or by another
     * name then another marking will be ignored. Not always what you
     * want, but allowing duplicates would use lots of hashtable entries
     * if you attempted to mark an object for inspection when in a
     * loop. Also, please note that any objects you mark for
     * inspection will never be garbage collected since the inspector
     * holds a reference to it. Therefore there exists a way to delete
     * all entries from the gathering hashtables.

     * @param category, nullOK, suspect - A string category name;
     * like a folder that can be used to organize objects to be
     * inspected. It may be null.

     * @param name, nullOK, suspect - A string to use as the name of
     * the object. If the name is already used in the category given
     * (if any) by another object then we add a unique integer to its
     * name.

     * @Note This is a major capability and security leak. There
     * should probably be a capability required to gather objects to
     * each category, and each of the categories should be inspectable
     * only if you have the capability to that category at inspection
     * request time. This all should be controlled from the TCB.

     */

    public static void gather(Object iobj, String category, String name) {
        Object oldObject;
        Hashtable catTable;

        if (iobj == null) return; // Can't store these in a hashtable
        if (name == null) name = "Object";

        // Gathering shold be possible from any thread, so we synchronize on all our tables

        synchronized(ec.e.inspect.Inspector.getGatheredObjects()) { // Inspected guest objects

            // See if the object already exists in the global object collection

            Object oldName = ec.e.inspect.Inspector.getGatheredObjects().get(iobj);
            if (oldName != null) return; // Object has already been marked for inspection.

            // Supplying a null category means we should collect to the "uncategorized" table
            
            if (category == null) catTable = ec.e.inspect.Inspector.getObjectsWithoutCategory();
            else {
                synchronized(ec.e.inspect.Inspector.getObjectCategories()) {
                    catTable = (Hashtable)ec.e.inspect.Inspector.getObjectCategories().
                      get(category);
                    if (catTable == null) { // No entries under this category name before
                        catTable = new Hashtable(10); // so add the category
                        ec.e.inspect.Inspector.getObjectCategories().
                          put(category,catTable); // and save the category table
                    }
                }
            }
        }
    
        // catTable is now our target Hashtable.

        synchronized(catTable) {
            oldObject = catTable.get(name); // Check if some other object has used this name here
            if (oldObject != null) // if so, add uniqueifying number
                name = name + " " + ec.e.inspect.Inspector.nextGensym++;
            catTable.put(name,iobj); // Gather the object
        }
    }

    /**

     * gather - Collect an object that we don't want to classify in
     * any category for possible inspection sometime later.

     * @param object, nullOK, suspect - The object to be
     * gathered. You can only mark any object for inspection once so
     * if it is already been marked in another category or by another
     * name then another marking will be a no-op. Not always what you
     * want. Allowing duplicates would use lots of hashtable entries
     * if you attempted to mark an object for inspection when in a
     * loop. Also, please note that any objects you mark for
     * inspection will never be garbage collected since the inspector
     * holds a reference to it. Therefore there exists a way to delete
     * all entries from the gathering hashtables.

     * @param name, nullOK, suspect - A string to use as the name of
     * the object. If the name is already used in the category given
     * (if any) by another object then we add a unique integer to its
     * name.

     */

    public static void gather(Object object, String name) { gather(object,null,name); }
}
