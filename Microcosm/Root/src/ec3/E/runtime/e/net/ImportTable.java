/*
  ImportTable.java
  
  Arturo Bejar

  Copyright 1997 Electric Communities, all rights reserved worldwide.
  
  Notes:
  This table holds (weak) pointers to the proxies that get imported into this
  machine, if the current state of the garbage collection is in effect then we
  hold on to the RRNotify.
*/

package ec.e.net;

import ec.trace.Trace;
import ec.util.NestedException;
import java.util.Hashtable;
import java.util.Enumeration;
import ec.e.file.EStdio;
import ec.e.net.Proxy;

/**
 * Maintains an import table: a mapping from ID numbers to imported objects for
 * some connection.
 */
class ImportTable {
    private static final Trace tr = new Trace("ec.e.net.ImportTable");
    
    private Hashtable myIDToObjectTable = new Hashtable();
    private EConnection myConnection;

    /**
     * Construct a new import table for a connection.
     *
     * @param connection The connection whose import table this will be.
     */
    ImportTable(EConnection connection) {
        myConnection = connection;
    }

    /**
     * Checks to see if the object is already in the table.
     *
     * @param anObject The (proxy to the) object of interest.
     * @returns The object's import ID if present, or 0 if absent
     */
    long alreadyThere(Object anObject) {
        Proxy proxy = Proxy.getProxyTarget(anObject);
        if (proxy != null) {
            return proxy.getIdForConnection(myConnection);
        }

        return 0;
    }

    /**
     * Get the proxy object corresponding to a particular import ID, touching
     * it unconditionally.
     *
     * @param importID The import ID of the object proxy desired.
     * @returns The proxy object.
     */
    EObject get(long importID) {
        return get(importID, true);
    }

    /**
     * Get the proxy object corresponding to a particular import ID.
     *
     * @param importID The import ID of the object proxy desired.
     * @param touch Flag to touch the object for DGC purposes
     * @returns The proxy object
     */
    EObject get(long importID, boolean touch) {
        Long longId = new Long(importID);

        if (importID > 2) {
            Object anObject = myIDToObjectTable.get(longId);

            if (anObject == null) {
                return null;
            }

            if (anObject instanceof RtWeakCell) {
                RtWeakCell cell = (RtWeakCell)anObject;
                Proxy proxy = (Proxy)cell.get();
                if (touch) {
                    proxy.touch();
                }
                return proxy.getPrimeDeflector();
            } else if (anObject instanceof ProxyResurrectionInfo) { /* need to remake */
// NO NO NO!!!  BAD spam!  BAD!  Use the trace system and do not put this
// sort of thing into the mainline output.  EVER.  PLEASE.  Talk to RobJ if
// this seems like the wrong thing to you.            
//                EStdio.out().println("Making new Proxy for class " + anObject);
                ProxyResurrectionInfo info = (ProxyResurrectionInfo)anObject;
                Proxy proxy = registerProxy(info.proxyClass, importID, false);
                proxy.setReferenceCount(info.referenceCount + 1);
                return proxy.getPrimeDeflector();
            } else {
                /* Error! Invariant failed */
                throw new RtRuntimeException(
                    "Import table contains object which isn't proxy or class: "
                    + anObject);
            }
        }
        /* If we get here we've got one of the permanently imported objects */
        return (EObject) myIDToObjectTable.get(longId);
    }

    boolean consistancyCheck() {
        Enumeration en = myIDToObjectTable.keys();
        Object key;
        Long id;
        Proxy proxy;
        boolean ret = true;
        
        while (en.hasMoreElements()) {
            key = en.nextElement();
            try {
                id = (Long)key;
            }
            catch (ClassCastException e) {
                tr.$("myIDToObjectTable contains non Long: " + key);
                ret = false;
                continue;
            }
            Object value = myIDToObjectTable.get(id);
            long idval = id.longValue();
            String idvalstr = Long.toHexString(idval);
            
            if (value == null) {
                tr.$("myIDToObjectTable id " + idvalstr + " contains null pointer");
                ret = false;
                continue;
            }
            if (idval < 2) {
                try {
                    proxy = (Proxy)value;
                }
                catch (ClassCastException e) {
                    tr.$("myIDToObjectTable special id " + idvalstr + " contains non Proxy: " + value);
                    ret = false;
                    continue;
                }
            }
            else {
                if (value instanceof RtWeakCell) {
                    Object contents = ((RtWeakCell)value).get();
                    if (contents == null) {
                        tr.$("myIDToObjectTable id " + idvalstr + " contains weak cell to null");
                        ret = false;
                        continue;
                    }
                    try {
                        proxy = (Proxy)contents;
                    }
                    catch (ClassCastException e) {
                        Class claz = contents.getClass();
                        if (claz == null) {
                            tr.$("myIDToObjectTable id " + idvalstr + " contains weak cell to thing of null class");
                            ret = false;
                            continue;
                        }
                        
                        tr.$("myIDToObjectTable id " + idvalstr + " contains weak cell to non Proxy: " + contents);
                        ret = false;
                        continue;
                    }
                }
                else if (value instanceof ProxyResurrectionInfo) {
                    // assume it's cool
                    continue;
                }
                else {
                    tr.$("myIDToObjectTable id " + idvalstr + " contains non RtWeakCell: " + value);
                    ret = false;
                    continue;
                }
            }
            
            if (proxy.getIdForConnection(myConnection) != idval) {
                tr.$("myIDToObjectTable id " + idvalstr + " contains proxy with different id: " + proxy);
                ret = false;
                continue;
            }
        }
        tr.$("consistancyCheck returning " + ret);
        return ret;
    }

    /**
     * Clean up import table when a proxy gets garbage collected.
     *
     * @param importID The import ID of the garbage collected proxy.
     */
    void gotFinalized(long importID) {
        Long longId = new Long(importID);
        Object cell = myIDToObjectTable.get(longId);
        if (cell instanceof ProxyResurrectionInfo) {
            EStdio.out().println("Import table error in gotFinalized, id " + importID + ", cell " + cell);
            // XXX - Need to find out why this happens, and do the *right thing* (TM)
            // Leaving it in the table, as somebody might reference it later
            return;
        }
        // Remove it from the table if we got this far
        // If it was valid, we'll replace it with its class to resurrect if needed
        myIDToObjectTable.remove(longId);
        if (importID >= 2 && cell != null) {
            // Connection death handling will clear the table,
            // so a previously queued finalize will run into the
            // harmless race of weakCell being null.
            RtWeakCell weakCell = (RtWeakCell)cell;
            Proxy proxy = (Proxy)weakCell.get();
            if (proxy != null) {
                myIDToObjectTable.put(longId, new ProxyResurrectionInfo(proxy.getReferenceCount(), ((Object)(proxy.getPrimeDeflector())).getClass()));
            }
        }
    }

    /**
     * Register the SturdyRefFollower for the remote end of this connection.
     * The SturdyRefFollower has a special, pre-destined import ID number (1).
     *
     * @param follower The proxy to the SturdyRefFollower to be registered.
     *
     * The follower may be any EObject, but typically will be an
     * instance of (or rather, a proxy to an instance of) SturdyRefFollower.
     */
    void registerFollower(EObject follower) {
        Long id = new Long(1);
        myIDToObjectTable.put(id, follower);
    }

    /**
     * Add a new entry to the import table.
     *
     * @param deflClass The deflector class for the imported object
     * @param importID The import ID by which the object will be known.
     * @returns A new proxy for the imported object
     */
    Proxy registerProxy(Class deflClass, long importID) {
        return registerProxy(deflClass, importID, true);
    }
    
    /**
     * Add a new entry to the import table.
     *
     * @param deflClass The deflector class for the imported object
     * @param importID The import ID by which the object will be known.
     * @param checkDuplicate If true, checks to see if the id already in use
     * @returns A new proxy for the imported object
     */
    private Proxy registerProxy(Class deflClass, long importID, boolean checkDuplicate) {
        Long longId = new Long(importID);
        Proxy proxy = null;
        // See if other side is messing with our head or has
        // a bogus Swiss number generator
        if (checkDuplicate && (myIDToObjectTable.get(longId) != null)) {
            String errorString = "ImportTable registerProxy: Proxy with ID " + importID + " already registered";
            EStdio.err().println(errorString);
            throw new RtEErrorException(errorString);
        }
        try {
            proxy = Proxy.construct(deflClass, myConnection, importID);
            myIDToObjectTable.put(longId, new RtWeakCell(proxy));
        } catch (Exception e) {
            throw new NestedException(
                "ImportTable.register: Could not instantiate proxy to " + deflClass, e);
        }
        return proxy;
    }

    /**
     * Remove an entry from the import table.
     *
     * @param importID The import ID of the object to be removed
     */
    void unregister(long importID) {
        /* from wire, final removal */
        Long longId = new Long(importID);
        if (myIDToObjectTable.containsKey(longId)) {
            myIDToObjectTable.remove(longId);
        }
    }

    /**
     * Clear out all mappings.
     */
    void unregisterAll() {
        myIDToObjectTable.clear();
    }
}

class ProxyResurrectionInfo
{
    int referenceCount;
    Class proxyClass;
    
    ProxyResurrectionInfo (int referenceCount, Class proxyClass) {
        this.referenceCount = referenceCount;
        this.proxyClass = proxyClass;
    }
    
    public String toString () {
        return super.toString() + " Reference Count = " + referenceCount + " " + proxyClass;
    }
}
