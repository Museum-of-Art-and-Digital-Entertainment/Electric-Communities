
/* =====================================================================
 *    FILE:  NotificationManager.java
 *
 *    AUTHOR: claire griffin
 *
 *    CREATED: 8/5/97 20:18:30
 *     
 *    DESCRIPTION: support classes for allowing una to specify a visual
 *      effect or cursor change in a neutral manner.
 *      SceneEffect: interface which must be implemented when an effect is
 *          desired
 *      NotificationManager: interface must be implemented by a notification
 *          manager
 *
 *    HISTORY:    
 *    
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 *
 * =====================================================================
 */

/* package */
package ec.e.run;

/* imports */
import java.awt.Cursor;

/* public interfaces */

public interface Notification  {
    static final public int LONG_DELAY = 1;
    static final public int DELAY_MESSAGE = 2;
    static final public int PROGRESS_MESSAGE = 3;
    
    /* the current supported cursors */
    static final public int CURRENT_CURSOR = -1;
    static final public int DEFAULT_CURSOR = Cursor.DEFAULT_CURSOR;
    static final public int CROSSHAIR_CURSOR = Cursor.CROSSHAIR_CURSOR;
    static final public int TEXT_CURSOR = Cursor.TEXT_CURSOR;
    static final public int WAIT_CURSOR = Cursor.WAIT_CURSOR;
    static final public int HAND_CURSOR = Cursor.HAND_CURSOR;
    static final public int MOVE_CURSOR = Cursor.MOVE_CURSOR;
    
    static final public String NO_MESSAGE = "";
    /**
     * start the scene effect
     */
    public void start();
    
    /**
     * stop the scene effect
     */
    public void stop(); 
    
    public void setIdentifier(Object id);

}

public interface UpdateableNotification extends Notification  {
    /**
     * update a message in a message dialog
     * @param message; the new message string
     */
    public void update(String message);
}

public interface ProgressNotification extends Notification  {
    /**
     * update a message in a progress message dialog
     * @param message; the new message string
     * @param progress; the current percent done
     */
    public void update(String message, int progress);
}

public interface NotificationManager  {
    public Object beginNotification(Notification effect);
    public void endNotification(Object identifier);
    public void updateNotification(Object identifier, String message);
    public void updateNotification(Object identifier, String message, int progress);
    public void killAllNotifications();
    
    /* mechanism for getting common notificatin types */
    public Notification getMessageNotification(String message);    
    public Notification getCursorNotification(String message, int cursor_type);    
    public Notification getTransitionNotification();    
    public Notification getUpdateableNotification(String message, int cursor_type);
    public Notification getProgressNotification(String message, int begin, int end);    
    
}

/* public classes */

