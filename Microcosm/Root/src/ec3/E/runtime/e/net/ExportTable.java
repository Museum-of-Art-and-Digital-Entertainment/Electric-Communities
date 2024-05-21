/*
  ExportTable.java

  Arturo Bejar
  February, 1996
  Copyright 1997 Electric Communities, all rights reserved worldwide.

  Notes:
  This table holds pointers to the EObjects that have been exported by
    reference.
  This is the only object that can assign over the wire id numbers, it starts
    from 3 onwards because 1 and 2 will always be special objects known to the
    Connection in any machine.
*/

package ec.e.net;

import java.util.Hashtable;
import java.util.Enumeration;
import ec.e.net.steward.EObjectExport;

/**
 * Maintains an export table: mappings between ID numbers and exported objects
 * for some connection.
 */
class ExportTable {
    static private final Trace tr = new Trace("ec.e.net.ExportTable");
    
    private EConnection myConnection;
    private Hashtable myObjectToIDTable = new Hashtable();
    private Hashtable myIDToObjectTable = new Hashtable();

    /**
     * Construct a new export table for a connection.
     *
     * @param connection The connection whose export table this will be.
     */
    ExportTable(EConnection connection) {
        myConnection = connection;
    }

    /**
     * Checks if the object that you want to send is already in this table,
     * updates touch count in EObjectExport if so.
     *
     * @param anObject The object of interest.
     * @returns The object's export ID if present, 0 if absent
     */
    long alreadyThere(Object anObject) {
        if (myObjectToIDTable.containsKey(anObject)) {
        if (tr.debug && Trace.ON) tr.debugm("Found " + anObject + " in export table");
            Long returnId = (Long)myObjectToIDTable.get(anObject);
            long ret = returnId.longValue();
            if (ret > 2) { // XXX (Gordie) should be NetIdentityMaker.MIN_ID
                EObjectExport export =
                    (EObjectExport)myIDToObjectTable.get(returnId);
                if (export != null)
                    export.touch();
            }
            return ret;
        } else {
        if (tr.debug && Trace.ON) tr.debugm("Didn't find " + anObject + " in export table");
            return 0;
        }
    }

    /**
     * Signal that an given object is suspected to be trash, for DGC purposes.
     *
     * @param exportID Export ID of the object in question
     * @param referenceCount Its reference count.
     */
    void dgcSuspectTrash(long exportID, int referenceCount) {
        Long longId = new Long(exportID);
        EObjectExport export = (EObjectExport) myIDToObjectTable.get(longId);
        if (export != null) {
            export.dgcSuspectTrash(referenceCount);
        }
    }

    /**
     * Return the object with a given export ID
     *
     * @param exportID The export ID of the desired object
     * @return The exported object itself.
     */
    Exportable get(long exportID) {
        if (tr.verbose && Trace.ON) tr.$("get(" + exportID + ")");
        
        Long longId = new Long(exportID);

        if (exportID > 2) {
            EObjectExport export =
                (EObjectExport)myIDToObjectTable.get(longId);
            if (export == null)
                return null;
            return export.getObject();
        } else {
            return (Exportable) myIDToObjectTable.get(longId);
        }
    }

    /**
     * Add a new object to the export table.
     *
     * @param anObject The object to add
     * @returns The new object's export ID number
     */
    long register(Exportable anObject) {
        /* XXX Should this be typed to take EObject_$_Impl? Or at least an
           EObject? */
        long exportID;
        exportID = anObject.getIdentity();
        Long longId = new Long(exportID);

        EObjectExport export = new EObjectExport(anObject, myConnection);

        myIDToObjectTable.put(longId, export);
        myObjectToIDTable.put(anObject, longId);

        return exportID;
    }

    /**
     * Register the SturdyRefFollower object for this connection. The SturdyRefFollower
     * has a special, pre-destined export ID number (1).
     *
     * @param follower The SturdyRefFollower object to be registered.
     *
     * The follower may be any EObject, but typically will be an
     * instance of SturdyRefFollower.
     */
    void registerFollower(EObject follower) {
        Long id = new Long(1);
        myIDToObjectTable.put(id, follower);
        myObjectToIDTable.put(follower, id);
    }

    /**
     * Remove an object from the export table blindly.
     *
     * @param exportID The export ID of the object to remove.
     */
    void remove(long exportID) {
    if (exportID > 2) {
            Long longId = new Long(exportID);
            EObjectExport export = (EObjectExport) myIDToObjectTable.get(longId);
        myIDToObjectTable.remove(longId);
        myObjectToIDTable.remove(export.getObject());
    }
    }

    /**
     * Remove an object from the export table, but only if it's there.
     *
     * @param exportID The export ID of the object to remove.
     * @returns true if removed, false if already not there
     */
    boolean unregister(long exportID) {
        Long longId = new Long(exportID);
        if ((exportID > 2) && myIDToObjectTable.containsKey(longId)) { /* It exists */
            EObjectExport export = (EObjectExport)myIDToObjectTable.get(longId);
            myIDToObjectTable.remove(longId);
            myObjectToIDTable.remove(export.getObject());
            return true;
        }
        return false;
    }

    /**
     * Clear out the whole set of mappings.
     */
    void unregisterAll(Throwable cause) {
        Enumeration en = myObjectToIDTable.keys();
        while (en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (obj instanceof RtExceptionEnv && obj != RtRun.NULL_EXCEPTION_ENV) {
                if (tr.debug && Trace.ON) tr.$("doing ethrow to exported exception environment: " + obj + ": " + cause);
                ((RtExceptionEnv)obj).doEThrow(cause);
            }
        }
        myObjectToIDTable.clear();
        myIDToObjectTable.clear();
    }
}
