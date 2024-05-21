package ec.e.net;

/*
  Trust manager certification needs to be set up so that
  only the EC trusted Computing Base can reference this
  class.
  */
public class RtConnectionHook {
    public static void askForNotification(RtConnection connection,
                                          ENotificationHandler h) {
        connection.askForNotification(h);
    }

    public static void unAskForNotification(RtConnection connection,
                                            ENotificationHandler h) {
        connection.unAskForNotification(h);
    }
}
