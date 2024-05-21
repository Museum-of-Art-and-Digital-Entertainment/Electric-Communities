package ec.e.util;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
   A collection whose entries expire.  Does not extend Hashtable since
   put() takes an extra argument.  Other than the extra argument, use
   put(), get(), and remove() like a normal Hashtable.  You are also
   expected arrange for expire() to be called periodically.
*/
public class ExpireCollection 
{
    private Hashtable myKeyMapping;
    private ExpirationEntry myExpirationsHead;
    private ExpirationEntry myExpirationsTail;
    private long myLastExpiration;
    
    /**
       Create a new ExpireCollection with no elements.  The algorithms
       used here will only perform well if entries are usually placed
       into the collection with constant offsets from a monotonically
       increasing expirationDate.<p>

       You can 'refresh' the expirationDate at any time simply by
       calling put() with the same key, the same (or a different)
       value, and the new expirationDate.<p>

       Attempts to put() entries with expirationDate's less than the
       last call to expire() are silently ignored.
    */
    public ExpireCollection() {
        myKeyMapping = new Hashtable();
        myExpirationsHead = null;
        myExpirationsTail = null;
        myLastExpiration = 0L;
    }

    /**
       Place an object in the collection.  It will be removed
       automatically when expire() is called with a value greater than
       expirationDate.

       @param key object used to identify this value.
       @param value object being stored.
       @param expirationDate when to discard this mapping.
       @return the object previously stored under this key, or null if none.
    */
    public Object put(Object key, Object value, long expirationDate) {
        ExpirationEntry entry = (ExpirationEntry)myKeyMapping.remove(key);
        Object ret = null;
        
        if (entry == null) {
            entry = new ExpirationEntry(key, value, expirationDate);
        }
        else {
            ret = entry.value;
            deleteEntry(entry);
            entry.value = value;
            entry.expirationDate = expirationDate;
        }
        if (expirationDate > myLastExpiration) {
            insertEntry(entry, expirationDate);
            myKeyMapping.put(key, entry);
        }
        return ret;
    }

    /**
       Retrieve an object from the collection.  Note that you need to
       be prepared for the object to vanish from the collection if
       it's expirationDate has passed.

       @param key which object to search for.
       @return the object stored under this key, or null if none.
    */
    public Object get(Object key) {
        ExpirationEntry entry = (ExpirationEntry)myKeyMapping.get(key);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }

    /**
       Remove an object from the collection.

       @param key which object to remove.
       @return the object previously stored under this key, or null if none.
    */
    public Object remove(Object key) {
        ExpirationEntry entry = (ExpirationEntry)myKeyMapping.remove(key);
        Object ret = null;
        
        if (entry != null) {
            ret = entry.value;
            deleteEntry(entry);
        }
        return ret;
    }

    /**
       Returns the number of elements in the ExpireCollection.
    */
    public int size() {
        return myKeyMapping.size();
    }

    /**
       Remove all objects from the collection whose expirationDate's
       are less than date.
    */
    public void expire(long date) {
        ExpirationEntry e = myExpirationsHead ;
        while (e != null && e.expirationDate <= date) {
            myKeyMapping.remove(e.key);
            e = e.next;
        }
        if (e == null) { // expired everything
            myExpirationsHead = null;
            myExpirationsTail = null;
        }
        else { // expired up to e
            myExpirationsHead = e;
            e.prev = null;
        }
        myLastExpiration = date;
    }

    /**
       Snapshot the ExpireCollection, returning an Enumeration of it.
       Entries will be of type ExpireCollectionEnumeratorEntry, and
       will be returned in the order in which they will be expired.
    */
    public Enumeration enumerate() {
        return new ExpireCollectionEnumerator(myExpirationsHead);
    }

    /**
       Insert an ExpirationEntry into the sorted list of entries.
       Searches from the end of the list under the assumption that
       most entries will be inserted with a constant offset from the
       current time (hence at the end of the list).  If this is not
       true, this routine will perform poorly, and should be recoded.
    */
    private void insertEntry(ExpirationEntry entry, long date) {
        // assume insertions are at or near end of list
        ExpirationEntry e = myExpirationsTail ;
        while (e != null && e.expirationDate > date) {
            e = e.prev;
        }
        if (e == null) { // insert at beginning of list
            entry.prev = null; // should be superfluous
            entry.next = myExpirationsHead ;
            myExpirationsHead = entry;
        }
        else { // insert after element e
            entry.prev = e ;
            entry.next = e.next ;
            e.next = entry;
        }
        if (entry.next == null) {
            myExpirationsTail = entry;
        }
        else {
            entry.next.prev = entry;
        }
    }

    /**
       Delete an ExpirationEntry from the sorted list of entries.
       Just performs the removal from a doubly linked list.
    */
    private void deleteEntry(ExpirationEntry entry) {
        if (entry.prev == null) {
            myExpirationsHead = entry.next;
        }
        else {
            entry.prev.next = entry.next;
        }
        if (entry.next == null) {
            myExpirationsTail = entry.prev;
        }
        else {
            entry.next.prev = entry.prev;
        }
        entry.prev = entry.next = null;
    }

    public boolean consistancyCheck(PrintStream err) {
        boolean ret = true;
        Enumeration en = enumerate();
        Hashtable fromEnumeration = new Hashtable(myKeyMapping.size()+1);
        while (en.hasMoreElements()) {
            long expDate = Long.MIN_VALUE;
            ExpireCollectionEnumeratorEntry entry = null;
            try {
                entry = (ExpireCollectionEnumeratorEntry)en.nextElement();
            }
            catch (Exception e) {
                err.println("exception getting next element from enumerator: " + e);
            }
            if (entry == null) {
                ret = false;
                err.println("enumerator.nextElement failed");
                break;
            }
            fromEnumeration.put(entry.key, entry);
            ExpirationEntry hashEntry = (ExpirationEntry)myKeyMapping.get(entry.key);
            if (hashEntry == null) {
                ret = false;
                err.println("enumeration entry " + entry + " not found in myKeyMapping");
                continue;
            }
            if (hashEntry.key != entry.key ||
                hashEntry.value != entry.value ||
                hashEntry.expirationDate != entry.expirationDate) {
                ret = false;
                err.println("enumeration entry " + entry + " does not match hashEntry " + hashEntry);
                continue;
            }
            if (entry.expirationDate < expDate) {
                ret = false;
                err.println("enumeration entry " + entry + " out of order.  expDate = " + expDate);
                continue;
            }
            expDate = entry.expirationDate;
        }
        if (fromEnumeration.size() != myKeyMapping.size()) {
            ret = false;
            err.println("fromEnumeration.size() " + fromEnumeration.size() + " != myKeyMapping.size() " + myKeyMapping.size());
        }
        en = myKeyMapping.keys();
        while (en.hasMoreElements()) {
            Object k = en.nextElement();
            ExpirationEntry hashEntry = (ExpirationEntry)myKeyMapping.get(k);
            if (hashEntry.key != k) {
                ret = false;
                err.println("hashEntry.key(" + hashEntry + ") != key in myKeyMapping (" + k + ")");
                continue;
            }
            ExpireCollectionEnumeratorEntry entry = (ExpireCollectionEnumeratorEntry)fromEnumeration.get(k);
            if (entry == null) {
                ret = false;
                err.println("hashEntry " + hashEntry + " not in enumeration");
                continue;
            }
            if (hashEntry.key != entry.key ||
                hashEntry.value != entry.value ||
                hashEntry.expirationDate != entry.expirationDate) {
                ret = false;
                err.println("[2]enumeration entry " + entry + " does not match hashEntry " + hashEntry);
                continue;
            }
            fromEnumeration.remove(k);
        }
        if (fromEnumeration.size() > 0) {
            ret = false;
            err.println("fromEnumeration not reduced to zero size " + fromEnumeration.size());
        }
        err.println("ExpireCollection.consistancyCheck returning " + ret);
        return ret;
    }
}

class ExpirationEntry 
{
    Object key;
    Object value;
    long expirationDate;
    ExpirationEntry next;
    ExpirationEntry prev;

    ExpirationEntry(Object key, Object value, long expirationDate) {
        this.key = key;
        this.value = value;
        this.expirationDate = expirationDate;
        next = null;
        prev = null;
    }

    public String toString() {
        return "ExpirationEntry(" + key + ", " + value + ", " + expirationDate + ")" ;
    }
}

    
class ExpireCollectionEnumerator implements Enumeration {
    ExpireCollectionEnumeratorEntry next = null;

    ExpireCollectionEnumerator(ExpirationEntry e) {
        ExpireCollectionEnumeratorEntry prev = null;
        ExpireCollectionEnumeratorEntry curr = null;
        while (e != null) {
            curr = new ExpireCollectionEnumeratorEntry(e.key, e.value, e.expirationDate);
            if (prev == null) {
                next = curr;
            }
            else {
                prev.next = curr;
            }
            prev = curr;
            e = e.next;
        }
        
    }
    
    public boolean hasMoreElements() {
        return (next != null);
    }

    public Object nextElement() {
        if (next != null) {
            ExpireCollectionEnumeratorEntry e = next;
            next = e.next;
            return e;
        }
        throw new NoSuchElementException("ExpireCollectionEnumerator");
    }
}
