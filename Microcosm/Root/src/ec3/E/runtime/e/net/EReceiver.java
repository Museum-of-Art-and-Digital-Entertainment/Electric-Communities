package ec.e.net;

/**
 * Object that will receive network communications, receiving them at the
 * E level and placing them into the run queue for delivery.
 */
class EReceiver {
    private EConnection myConnection;
    private ConnectionsManager myConnectionsManager;

    private EReceiver() {}
    
    /**
     * Construct a new EReceiver.
     */
    EReceiver(EConnection connection, ConnectionsManager connectionsManager) {
        myConnection = connection;
        myConnectionsManager = connectionsManager;
    }

    /**
     * Return my connection.
     */
    EConnection connection() {
        return myConnection;
    }

    /**
     * Return my connection's import/export tables.
     */
    ImportExportTables importExportTables() {
        return myConnection.importExportTables();
    }

    /**
     * Receive an E message from the comm system and place it on the run queue.
     *
     * @param toObject The object the message is being sent to.
     * @param message The message to deliver onto the queue.
     * @param exceptionEnv The sender's E exception environment
     */
    void receive(Exportable toObject, RtEnvelope message) {
        myConnectionsManager.updateMessageSerialNumber(myConnection);
        RtRun.alwaysEnqueue(toObject, message);
    }

    /**
     * Receive a raw message packet from the comm system and give it to the
     * connection object to process appropriately.
     */
    void receivePacket(byte packetBytes[]) {
        myConnection.processPacket(packetBytes);
    }
}
