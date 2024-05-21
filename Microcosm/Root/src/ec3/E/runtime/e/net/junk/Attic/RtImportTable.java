/*
  RtImportTable.java
  v. 0.1
  
  Arturo Bejar
  February, 1996
  Copyright (c) 1996 Electric Communities, All Rights Reserved.
  
  Notes:
  This table holds (weak) pointers to the proxies that get imported into this
  machine, if the current state of the garbage collection is in effect then we
  hold on to the RRNotify.
  
  Change History:
  Feb 1997 - (Gordie) removed cyclic DGC to use unmodified Java runtime
  also added acyclic reference counting directly to Proxy
*/

package ec.e.net;

import java.util.Hashtable;

public class RtImportTable {
    private Hashtable myIdToObjectTable = new Hashtable();
    private RtConnection myConnection;

    static Trace tr = new Trace(false, "[RtImportTable]");

    RtImportTable(RtConnection connection) {
        myConnection = connection;
    }

    synchronized void unregisterAll () {
        myIdToObjectTable.clear();
    }

    public boolean validId(long thisId) {
        Long longId = new Long(thisId);
        return(myIdToObjectTable.containsKey(longId));
    }

    /** Checks to see if the object is already in the table */
    public synchronized long alreadyThere(Object thisObject) {
        Long returnId;

        if (thisObject instanceof EProxy_$_Impl) {
            EProxy_$_Impl thisProxy = (EProxy_$_Impl)thisObject;
            return(thisProxy.getIdForConnection(myConnection));
        } else {
            return(0);
        }
    }

    /* XXX - (GJF) I'm hoping to get rid of this but have to do a make clean
       first to make sure it is no longer used anywhere ... */
    /*
    public synchronized void register (Object thisObject, long thisId) {
        Long longId = new Long(thisId);
        myIdToObjectTable.put(longId, new RtWeakCell(thisObject));
    }
    */

    public synchronized EProxy_$_Impl registerProxy(Class proxyClass,
                                                    long thisId) {
        Long longId = new Long(thisId);
        EProxy_$_Impl theProxy = null;
        try {
            if (tr.tracing)
                tr.$("proxyClass is " + proxyClass.getName());
            theProxy = (EProxy_$_Impl)proxyClass.newInstance();
            theProxy.setConnection(myConnection, thisId);
            if (tr.tracing)
                tr.$("Registering proxy under " + longId + ", " +
                     theProxy.getClass().getName());
            myIdToObjectTable.put(longId, new RtWeakCell(theProxy));
        } catch (Exception e){
            /* XXX bad exception usage -- fix */
            System.out.println(
                "RtImportTable.register: Could not instantiate proxy");
            e.printStackTrace();
        }
        return(theProxy);
    }

    public synchronized void unRegister(long thisId) {
        /* from wire, final removal */
        Long longId = new Long(thisId);
        if (myIdToObjectTable.containsKey(longId)) { /* It exists */
            Object tempObject = myIdToObjectTable.remove(longId);
        }
    }

    public EProxy_$_Impl get(long thisId) {
        return(get(thisId, true));
    }

    public synchronized EProxy_$_Impl get(long thisId, boolean touch) {
        Long longId = new Long(thisId);
        EProxy_$_Impl proxy = null;

        if (thisId > 2) {
            if (tr.tracing)
                tr.$("About to get " + thisId);
            Object theObj = myIdToObjectTable.get(longId);

            if (theObj == null)
                return(null);

            if (theObj instanceof RtWeakCell) {
                RtWeakCell cell = (RtWeakCell)theObj;
                proxy = (EProxy_$_Impl)cell.get();
                if (touch)
                    proxy.touch();
                if (tr.tracing) {
                    String str = tr.eclassString(proxy);
                    tr.$("Got " + str);
                    tr.nl();
                }
                return(proxy);
            } else if (theObj instanceof Class) { /* need to remake */
                if (tr.tracing) tr.$("Had to instantiate a new proxy");
                registerProxy((Class)theObj, thisId);
                return(get(thisId)); /* Must call this for correct ref count */
            } else {
                /* Error! Invariant failed */
                throw new RtRuntimeException(
                    "Import table contains object which isn't proxy or class: "
                    + theObj);
            }
        }
        /* If we get here we've got one of the permanently imported objects */
        return((EProxy_$_Impl)myIdToObjectTable.get(longId));
    }

    public synchronized void registerDirectory(EProxy thisDirectory) {
        Long id = new Long(1);
        myIdToObjectTable.put(id, thisDirectory);
    }

    public synchronized void registerConnectionKeeper(EProxy keeper) {
        Long id = new Long(2);
        myIdToObjectTable.put(id, keeper);
    }

    public synchronized void gotFinalized(long thisId) {
        Long longId = new Long(thisId);
        Object proxy = myIdToObjectTable.remove(longId);
        myIdToObjectTable.put(longId, proxy.getClass());
    }
}
