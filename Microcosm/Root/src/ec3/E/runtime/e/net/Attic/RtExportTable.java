/*
  RtExportTable.java
  v. 0.1

  Arturo Bejar
  February, 1996
  Copyright (c) 1996 Electric Communities, All Rights Reserved.

  Notes:
  This table holds pointers to the EObjects that have been exported by
    reference.
  This is the only object that can assign over the wire id numbers, it starts
    from 3 onwards because 1 and 2 will always be special objects known to the
    Connection in any machine.

  Change History:
  Feb 1997 - (Gordie) removed cyclic DGC to use unmodified Java runtime
  also added acyclic reference counting directly to Proxy
*/

package ec.e.net;

import java.util.Hashtable;

public class RtExportTable {
    private RtConnection myConnection;
    private Hashtable myObjectToIdTable = new Hashtable();
    private Hashtable myIdToObjectTable = new Hashtable();

    static Trace tr = new Trace(false, "[RtExportTable]");

    RtExportTable(RtConnection connection) {
        myConnection = connection;
    }

    synchronized void unregisterAll() {
        myObjectToIdTable.clear();
        myIdToObjectTable.clear();
    }

    /** Checks if the object that you want to send is already in this table,
      updates touch count in EObjectExport if so */
    public long alreadyThere(Object thisObject) {
        if (myObjectToIdTable.containsKey(thisObject)) {
            Long returnId = (Long)myObjectToIdTable.get(thisObject);
            long ret = returnId.longValue();
            if (ret > 2) {
                EObjectExport export =
                    (EObjectExport)myIdToObjectTable.get(returnId);
                if (export != null)
                    export.touch();
            }
            return(ret);
        } else {
            return(0);
        }
    }

    public boolean validId(long thisId) {
        Long longId = new Long(thisId);
        return(myIdToObjectTable.containsKey(longId));
    }

    /* XXX - This should be typed to take EObject_$_Impl?
       Or at least EObject ... */
    public long register(Object thisObject) {
        long theIdentity;
        EObject_$_Impl implObject = (EObject_$_Impl)thisObject;
        theIdentity = implObject.getIdentity();
        Long longId = new Long(theIdentity);

        EObjectExport export = new EObjectExport(implObject, myConnection);

        if (tr.tracing) {
            String st = tr.eclassString((EObject)thisObject);
            tr.$("Storing " + st + " under " + longId);
        }

        myIdToObjectTable.put(longId, export);
        myObjectToIdTable.put(export, longId);

        return(theIdentity);
    }


    public boolean unRegister(long thisId) {
        Long longId = new Long(thisId);
        if (myIdToObjectTable.containsKey(longId)) { /* It exists */

            Object tempObject = myIdToObjectTable.get(longId);
            myIdToObjectTable.remove(longId);
            myObjectToIdTable.remove(tempObject);
            if (tr.tracing)
                tr.$("Removing from ExportTable " + longId);

            return(true);
        }
        return(false);
    }

    public EObject_$_Impl get(long thisId) {
        Long longId = new Long(thisId);

        if (thisId > 2) {
            if (tr.tracing)
                tr.$(" Trying to get " + longId);
            EObjectExport export =
                (EObjectExport)myIdToObjectTable.get(longId);
            if (export == null)
                return(null);
            return(export.getObject());
        } else {
            return(( EObject_$_Impl)myIdToObjectTable.get(longId));
        }
    }

    public void registerDirectory(EObject thisDirectory) {
        Long id = new Long(1);
        myIdToObjectTable.put(id, thisDirectory);
        myObjectToIdTable.put(thisDirectory, id);
    }

    public void registerConnectionKeeper(EObject keeper) {
        Long id = new Long(2);
        myIdToObjectTable.put(id, keeper);
        myObjectToIdTable.put(keeper, id);
    }

    public void remove(long id) {
        Long longId = new Long(id);
        EObjectExport export = (EObjectExport) myIdToObjectTable.get(longId);
        if (tr.tracing)
            tr.$("Removing export for " + id);
        myObjectToIdTable.remove(export);
        myIdToObjectTable.remove(longId);
    }

    public void dgcSuspectTrash(long thisId, int referenceCount) {
        if (tr.tracing)
            tr.$("Received id " + thisId + ", reference count " +
                 referenceCount);
        Long longId = new Long(thisId);
        EObjectExport export = (EObjectExport) myIdToObjectTable.get(longId);
        if (export != null) {
            export.dgcSuspectTrash(referenceCount);
        } else {
            if (tr.tracing)
                tr.$("Received suspect trash for nonexistant object");
        }
    }
}
