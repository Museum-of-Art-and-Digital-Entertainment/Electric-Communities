// These are a collection of classes used to do in-memory cacheing.  It's all
// about as generic as it can be, and they can be used to cache just about
// anything, with customisable cache-management policy etc..
//
// Harry Richardson - 10/6/97

package ec.misc.cache;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;



//
// Class used to report errors
//
public class CacheException extends Exception {
    public CacheException() {super();}
    public CacheException(String s) {super(s);} 
}



//
// This interface defines the methods that are used to customise the cache.
// Rather than inheriting from the Cache and overriding the methods (which
// is potentially messy) all the specialities can be grouped together into
// one object.
//
public interface CacheMethods {

    // This returns the relative worth of the item in the cache.  Used for replacement
    // policy.  Higher values of Heuristic will make an item more likely to remain
    // in the cache
    public float getHeuristic(long size, long time, int ref_count, int cumulative_count);

    // Returns the object if it can get it, but returns null if it cannot.
    // Null is NOT an acceptable object to returns except as an indication
    // of failure (since I can't put a null into a hashtable anyway).
    public Object getObject(Object key);

    // This should return the size in bytes of the object data.  Key is supplied
    // here in case it is useful in obtaining this information.
    public long getSize(Object data, Object key);
}



//
// Class that is used both to keep an object and its cache management info
// together, and which implements the HeapItem interface so that all 
// discardable objects can be thrown onto a heap and the least useful removed
// when necessary
//
public class CacheBundle implements HeapItem {
    private Object my_data; // The actual data object
    private Object my_key;  // The key under which it is requested
    private CacheMethods my_cache_methods; 

    protected long my_size; // size in bytes.
    protected long my_time = 0; // the last time at which this was in use.
    protected int my_reference_count = 0; // Number of times in use

    protected int my_cumulative_count = 0; 
        // used to help cache management decisions. cumulative count is the 
        // total number of times it has been referenced since it was loaded.

    public CacheBundle(Object data, Object key, CacheMethods cache_methods)
        throws CacheException {
        
        // First check for invalid stuff...

        if (cache_methods == null) {
            throw new CacheException("CacheBundle: null cache methods");
        }
        if (key == null) {
            throw new CacheException("CacheBundle: null key");
        }
        if (data == null) {
            throw new CacheException("CacheBundle: null data");
        }

        my_data = data;
        my_key = key;
        my_cache_methods = cache_methods;
        my_size = my_cache_methods.getSize(data, key);
    }   

    public Object getData() {
        return my_data;
    }

    public Object getKey() {
        return my_key;
    }

    public boolean greaterThan(HeapItem hi) {
        // This method is used by the Heap for ordering. Required by HeapItem.
        return (getHeuristic() > ((CacheBundle) hi).getHeuristic()) ? true : false;
    }

    public float getHeuristic() {
        // This calls the CacheMethods.getHeuristic function.  It passes in all
        // the information that the method might want as primitives, rather
        // than as a reference, so that nothing in this object can be messed
        // around with...

        return my_cache_methods.getHeuristic(my_size, 
                                             my_time, 
                                             my_reference_count, 
                                             my_cumulative_count);
    }   

    public void print() {
        // The print method is required by the HeapItem interface
    }
}



//
//
//
public class Cache {
    private Heap my_not_in_use; // A heap of all potentially removable items
    private Hashtable my_hash_by_key;  // CacheBundles hashed by key
    private Hashtable my_hash_by_data; // CacheBundles hashed by data
    private boolean my_cache_nulls = false; // Whether we are using null_list
    private int my_null_list_max_size; // The max size of the null_list
    private Vector my_null_list; // Array of keys whose lookup failed.
    private CacheMethods my_cache_methods = null;
    private long my_initial_size; // Maximum amount of memory we want to use
    private long my_memory_used = 0; // Amount of memory that we are using 
    
    private long my_memory_size; 
    // The maximum amount of memory that we have been forced to use.  This 
    // Starts off the same as initial_size, but if we cannot avoid increasing
    // the cache size then we will raise this.  The cache should attempt to
    // get back down to the initial_size if more space becomes available

    private Heap not_in_use; 
    // A heap of all HashBundles that are in cache, but not in use.  They 
    // are heaped by their utility functions


    public Cache(CacheMethods cache_methods, long initial_size)
        throws CacheException { 
        // This constructor should be used if you do not want to maintain a
        // null_list (that is a list of keys whose lookup has failed).
        // Having a null_list can increase the speed of cache rejection.
        // There are two reasons for not using one... 1. If the lookup in the
        // null_list vector is slower than the CacheMethods.getObject() method,
        // or 2) If the key that was previously invalid could have become valid
        // again between calls. 

        initialise(cache_methods, initial_size);
    }

    public Cache(CacheMethods cache_methods, 
                 long initial_size, 
                 int null_list_max_size) throws CacheException {    
        
        // The null_list_max_size arg specifies how big the null_list will be
        // allowed to grow before the older entries start dropping off the
        // front of the vector

        initialise(cache_methods, initial_size);
        
        // Check for silly values of null_list_max_size
        if (null_list_max_size < 1) {
            throw new CacheException(
                "null_list_max_size of < 1 passed into Cache constructor");
        }

        my_cache_nulls = true;
        my_null_list = new Vector();
        my_null_list_max_size = null_list_max_size;
    }

    private void initialise(CacheMethods cache_methods, long initial_size)
        throws CacheException {
        
        // This method is just the stuff that the two previous constructors
        // have in common (but Java doesn't seem to allow one constructor to
        // call another in the same class).

        if (cache_methods == null) {
            throw new CacheException(
                "Null CacheMethod passed into Cache constructor");
        }

        my_cache_methods = cache_methods;
        my_initial_size = initial_size;
        my_memory_size = initial_size;

        my_hash_by_key = new Hashtable();
        my_hash_by_data = new Hashtable();
        my_not_in_use = new Heap();
    }

    public boolean isDataInCache(Object data) {
        // This checks if the texture is in cache.
        return my_hash_by_data.contains(data);
    }

    public boolean isKeyInCache(Object key) {
        // This checks if the texture is in cache.
        return my_hash_by_key.containsKey(key);
    }

    public Object getData(Object key) 
        throws CacheException {
        // This checks if the object is already loaded, and if not loads it.
        // It updates the cache management information (cumulative count etc..)
        // The Cache may maintain a null_list of keys that it has
        // been asked for which it could not find.  This saves doing the
        // existance check many times.  It only stores a set number of names in
        // the list however (my_null_list_max_size), and when this limit is
        // reached it will drop early entries.      

        // If we are using the null_list, then check it first.
        if (my_cache_nulls) {
            if (my_null_list.contains(key)) {
                // We've stored this key as being one that we can't find
                throw new CacheException("Key " + key + " is in the Cache null list");
            }
        }

        CacheBundle bundle;     

        if (my_hash_by_key.containsKey(key)) {
            // We've already got the item in the cache, so go get it...
            bundle = (CacheBundle) my_hash_by_key.get(key);
        } else {
            // Use the CacheMethods method to get the data.  (ie this is 
            // probably loading something off disk).
            
            Object data = my_cache_methods.getObject(key);
        
            if (data == null) {
                // We couldn't find the data, so if we're using a null list then
                // we should put it in there.

                if (my_cache_nulls) {
                    my_null_list.addElement(key);
                    if (my_null_list.size() > my_null_list_max_size) {
                        my_null_list.removeElementAt(0);
                    }
                }

                // Now report the fact that we couldn't get the data...
                throw new CacheException(
                    "Couldn't get the key " + key + " into the cache");
            }

            bundle = new CacheBundle(data, key, my_cache_methods);

            // Check that there's enough space...
            makeRoom(bundle.my_size);
            my_memory_used += bundle.my_size;
          
            my_hash_by_key.put(key, bundle);
            my_hash_by_data.put(data, bundle);
        } 

        bundle.my_reference_count++;
        bundle.my_cumulative_count++;

        return bundle.getData();
    }

    public void releaseData(Object data) throws CacheException {
        // removes data from the hash tables and updates the cache management
        // information.  If the reference count of the data drops to zero then
        // we put it in the heap of things that are eligible to be removed 
        // (unless we've already gone over our cache size limit, in which case
        // trash it immidiately to reclaim memory).
        
        // If it's not in the hash table then abort...   
        CacheBundle cb = (CacheBundle) my_hash_by_data.get(data);
        if (cb == null) {
            throw new CacheException("Cache: releaseData->data not in cache");
        }

        cb.my_reference_count--;

        if (cb.my_reference_count == 0) {
            // We've no longer got any references to this Bundle...

            if (my_initial_size < my_memory_size) {
                // We've had to increase the size of the cache at some point,
                // so take advantage of this bundle being freed up to clear
                // out some more space...
            
                my_hash_by_data.remove(cb.getData());
                my_hash_by_key.remove(cb.getKey());                 
                my_memory_used -= cb.my_size;
                System.out.println("Removing object " + cb.getKey() 
                                       + " from the cache");
            } else {
                // We've no longer got any references to this, but we haven't
                // gone over our cache size limit, so put it into the
                // Heap-O-Doom as a possible candidate for removal when we need
                // the space...

                cb.my_time = System.currentTimeMillis();
                my_not_in_use.insert(cb);
            }
        }
    }

    public void releaseKey(Object key) throws CacheException {
        // Removes a texture accessed by name - see releaseTexture(Rexture)

        CacheBundle cb = (CacheBundle) my_hash_by_key.get(key);

        if (cb == null) {
            throw new CacheException("Cache: releaseKey->key not in cache");
        }

        releaseData(cb.getData());
    }

    public String toString() {
        // Print out some statistics on the current state of the cache

        int count = 0;
        Enumeration e = my_hash_by_key.elements();
        
        while (e.hasMoreElements()) {
            if (((CacheBundle) e.nextElement()).my_reference_count == 0) {
                count++;
            }
        }

        String result = "Cache status\n" +
                        "  Memory size: " + my_memory_size + "\n" +
                        "  Memory used: " + my_memory_used + "\n" +
                        "  Number of items in cache: " + my_hash_by_key.size() + "\n" +
                        "  Number of items in cache but not in use: " + count;

        return result;
    }

    private void makeRoom(long size_in_bytes) {
        // Make sure that there's enough free space for an object of size
        // size_in_bytes to be added to the cache.  If there isn't, and if 
        // we can't free up any more space (ie all textures in the cache are
        // currently in use), then we MUST increase the size of the cache 
        // (since we can't not draw something that should be there).  Since
        // everything will work with VM, increasing the cache space will
        // always work, but there will be performance problems, and we will
        // get less use out of the cache (though there is still the benefit
        // that we get reuse of textures.

        // First check if we need to clear out space or not.
        if (my_memory_size - my_memory_used > size_in_bytes) {
            return;
        }

        try {
            while(my_memory_size - my_memory_used < size_in_bytes) {
                // We need to get rid of the least useful item.
                CacheBundle cb = (CacheBundle) my_not_in_use.deleteMin();

                // Check that it hasn't gone back into use before getting 
                // rid of it.
                if (cb.my_reference_count == 0) {
                    System.out.println("Removing object " + cb.getKey() 
                                       + " from the cache");
                    my_hash_by_data.remove(cb.getData());
                    my_hash_by_key.remove(cb.getKey());                 
                    my_memory_used -= cb.my_size;
                }
            }
        } catch (EmptyHeapException e) {
            // There isn't enough memory to hold all necessary textures, so
            // increase the size of the cache.
            my_memory_size = my_memory_used + size_in_bytes;
            System.out.println("Increasing cache size to " + my_memory_size);
        }
    }
}