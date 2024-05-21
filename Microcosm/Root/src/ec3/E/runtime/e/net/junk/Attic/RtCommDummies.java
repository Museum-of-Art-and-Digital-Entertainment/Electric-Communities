package ec.e.net;

public class RtConnection extends Thread {
    public static final String DeadConnection = "DeadConnection";
    public void sendEnvelope (long told, RtEnvelope env) {}
    public void dgcWRemoveMe (long id) {}    
    public void dgcSuspectTrash (long id, int referenceCount) {}
}

public interface ENotificationHandler {
}

public class RtConnectionHook {
    public static void askForNotification(RtConnection connection,
                                          ENotificationHandler h) {
    }
    
    public static void unAskForNotification(RtConnection connection,
                                            ENotificationHandler h) {
    }
}

