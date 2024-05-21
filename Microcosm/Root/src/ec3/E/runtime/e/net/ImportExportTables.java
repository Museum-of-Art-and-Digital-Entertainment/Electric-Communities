package ec.e.net;

import java.util.Hashtable;
import ec.e.net.steward.Proxy;

/**
 * Holder for all the tables concerned with remote object import and export.
 */
class ImportExportTables {
    private ImportTable myImports;
    private ExportTable myExports;
    private Hashtable myUniqueImports;
    private Hashtable myUniqueExports;
    private Hashtable myUniqueImportsByID;
    private Hashtable myUniqueExportsByID;
    private int myUniqueIDs;

    private EConnection myConnection;

    private Proxy myRemoteSturdyRefFollowerProxy;

    /**
     * Construct a new set of tables for a connection.
     *
     * @param connection The connections whose tables these will be
     */
    ImportExportTables(EConnection connection) {
        myConnection = connection;
        myImports = new ImportTable(connection);
        myExports = new ExportTable(connection);
        myUniqueImports = new Hashtable();
        myUniqueExports = new Hashtable();
        myUniqueImportsByID = new Hashtable();
        myUniqueExportsByID = new Hashtable();
        myUniqueIDs = 0;

        myRemoteSturdyRefFollowerProxy = Proxy.construct(
            "ec.e.net.SturdyRefFollower", connection, 1L);
        myExports.registerFollower(
            (EObject)(connection.localRegistrar().getSturdyRefFollower()));
        myImports.registerFollower(
            myRemoteSturdyRefFollowerProxy.getPrimeDeflector());
    }

    /**
     * Return the export table.
     */
    ExportTable exports() {
        return myExports;
    }

    /**
     * Return the import table.
     */
    ImportTable imports() {
        return myImports;
    }

    /**
     * Return the next unique object ID for unique export.
     */
    int nextUniqueID() {
        return ++myUniqueIDs;
    }

    /**
     * Return the unique exports table.
     */
    Hashtable uniqueExports() {
        return myUniqueExports;
    }

    /**
     * Return the unique exports (by ID) table
     */
    Hashtable uniqueExportsByID() {
        return myUniqueExportsByID;
    }

    /**
     * Return the unique imports table.
     */
    Hashtable uniqueImports() {
        return myUniqueImports;
    }

    /**
     * Return the unique imports (by ID) table
     */
    Hashtable uniqueImportsByID() {
        return myUniqueImportsByID;
    }

    /**
     * Return the remote directory server that knows IDs at the other end of
     * this connection.
     */
    SturdyRefFollower remoteSturdyRefFollower() {
        return (SturdyRefFollower) 
            myRemoteSturdyRefFollowerProxy.getPrimeDeflector();
    }
}
