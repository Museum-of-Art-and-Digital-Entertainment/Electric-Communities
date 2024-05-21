// FoundationApplet.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.applet.Applet;

/** java.applet.Applet subclass that attaches a FoundationPanel instance
  * to the Java Applet. Each Application has a FoundationApplet
  * instance, although you will never access it directly.
  */

public class FoundationApplet extends Applet implements Runnable {
    static Hashtable            groupToApplet = new Hashtable(1);
    Application                 application;
    FoundationPanel             panel;
    boolean                     startedRun, appletStarted;
    private static Vector       applicationStack;   /// ddk - ADDED FOR JAVASCRIPT SUPPORT

    /** Constructs a new FoundationApplet. */
    public FoundationApplet() {
        super();
    }

    static void setAppletForGroup(FoundationApplet applet) {
        groupToApplet.put(Thread.currentThread().getThreadGroup(), applet);
    }

    static FoundationApplet applet() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();

        return (FoundationApplet)groupToApplet.get(group);
    }

    /** Sets the Applet's Application.
      */
    public void setApplication(Application application) {
        this.application = application;
    }

    /** Returns the Applet's Application.
      * @see #setApplication
      */
    public Application application() {
        return application;
    }

    /** Initializes the FoundationApplet instance. Also creates the
      * Application object and starts a new thread for it to run in. You
      * should never call this method.
      */
    public void init() {
        Thread appThread = new Thread(this, "Main Application Thread");

        super.init();
        setAppletForGroup(this);
        // This lock is used to make sure that we don't return before the
        // application is set up enough to allow the processing of AWT events
        // that come in to the applet frame.  run() unlocks it.
        appThread.start();
        synchronized (this) {
            while (!startedRun) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    /** Creates the Application in the main Application thread.
      * You should never call this method.
      */
    public void run() {
        String param = getParameter("ApplicationClass");

        if (param != null && !param.equals("")) {
            Object newObject = instantiateObjectOfClass(param);

            if (newObject instanceof Application) {
                application = (Application)newObject;
            } else {
                throw new InconsistencyException("ApplicationClass " + param +
            " must be a subclass of netscape.application.Application");
            }
        } else {
            throw new InconsistencyException("An ApplicationClass parameter must be specified in the <applet> tag.  For example:\n<applet code=\"netscape.application.FoundationApplet\" width=320 height=200>\n    <param name=\"ApplicationClass\" value=\"MyApplication\">\n</applet>\n");
        }
        synchronized (this) {
            startedRun = true;
            // This notify will cause the applet's init routine to return.
            notifyAll();
            // We don't run the event loop until the start() comes in.
            // This means we can draw to the applet's Graphics context.
            while (!appletStarted) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        application.run();
    }

    /** Starts the Applet. */
    public void start() {
        synchronized (this) {
            ApplicationEvent event = new ApplicationEvent();

            if (!appletStarted) {
                // If this is the first start for the applet, unlock the
                // run() call.
                appletStarted = true;
                notifyAll();
            }
            event.type = ApplicationEvent.APPLET_STARTED;
            event.processor = application;
            application.eventLoop.addEvent(event);
        }
    }

    /** Stops the Applet. */
    public void stop() {
        ApplicationEvent event = new ApplicationEvent();

        event.type = ApplicationEvent.APPLET_STOPPED;
        event.processor = application;
        application.eventLoop.addEvent(event);
    }

    /** Destroys the Applet. */
    public void destroy() {
        /* AWT Thread */
        application.stopRunningForAWT();
        super.destroy();
    }

    void destroyFromIFC() {
        super.destroy();
    }

    /** Cleanup resources associated with the Applet.
      * this is always called by the ifc main thread
      */
    void cleanup() {
        // It would be nice to be able to post some application cleanup
        // notification to interested parties here.  ALERT!
        Enumeration groups;
        FoundationApplet        applet;
        ThreadGroup             group;

        /* it would be nice to be able to post some application destroy
         * notification to interested parties here.  ALERT!
         */
        groups = groupToApplet.keys();

        removeApplication(application); /// ddk - ADDED FOR JAVASCRIPT SUPPORT

        while (groups.hasMoreElements()) {
            group = (ThreadGroup)groups.nextElement();
            applet = (FoundationApplet)groupToApplet.get(group);

            if (applet == this) {
                groupToApplet.remove(group);
                break;
            }
        }
    }

    /* Installs the FoundationCanvas in the Applet with the necessary
     * RootView.
     */
    void setupCanvas(Application app) {
        int width, height;
        RootView rootView;

        application = app;
        width = size().width;
        height = size().height;
        panel = createPanel();
        application.setMainRootView(panel.rootView());

        panel.reshape(0, 0, width, height);
        add(panel);

        // Eventually we will support using a custom view as an alternative
        // to a custom application. Place a custom view (if specified) on the
        // rootView for the user.
        // if (defaultView != null) {
        //     defaultView.init(0, 0, width, height);
        //     rootView.addSubview(defaultView);
        // }

        // Eventually we will want to load an interface file here.  ALERT!
        // loadInterface(getParameter("velocity"));
    }

    /** Overridden to properly layout the Applet. */
    public void layout() {
        if (panel != null) {
            java.awt.Dimension size = size();
            java.awt.Insets insets = insets();
            int x = insets.left, y = insets.top,
                w = size.width - (insets.left + insets.right),
                h = size.height - (insets.top + insets.bottom);

            if (w > 0 && h > 0) {
                panel.reshape(x, y, w, h);
            }
        }
    }

    Object instantiateObjectOfClass(String className) {
        Class newClass;
        Object newObject;

        try {
            newClass = classForName(className);
            newObject = newClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new InconsistencyException("Unable to find class \"" + className + "\"");
        } catch (InstantiationException e) {
            throw new InconsistencyException("Unable to instantiate class \"" + className + "\" -- " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InconsistencyException("Unable to instantiate class \"" + className + "\" -- " + e.getMessage());
        }

        return newObject;
    }

    /** This method must be implemented by the Applet developer because
      * there is no way in the standard Java API for system classes (such as,
      * netscape.application) to look up an Applet's class by name. The
      * static method <b>Class.forName()</b> simply looks up one level in the
      * stack and gets the ClassLoader associated with the method block of the
      * caller. When the netscape.application classes are installed as
      * system classes, the ClassLoader is <b>null</b>. Thus, when code in
      * netscape.application calls <b>Class.forName()</b> it can only find
      * other system classes.<p>
      * The solution is an API that allows code to
      * find the ClassLoader for an applet by URL, and public API on
      * ClassLoader to ask it to load classes by name. Until these
      * enhancements can be made and distributed to all the world's Java
      * systems, Applets must subclass FoundationApplet and
      * implement the following one-line method:
      * <pre>
      *     public abstract Class classForName(String className)
      *         throws ClassNotFoundException {
      *         return Class.forName(className);
      *     }
      * </pre>
      */
    public Class classForName(String className)
        throws ClassNotFoundException {
        return Class.forName(className);
    }

    /** @private */
    public void paint(java.awt.Graphics g) {
        super.paint(g);
    }

    /** Creates and returns the Applet's FoundationPanel.
      * FoundationApplet subclasses can override this method to provide a
      * custom FoundationPanel subclass.
      * @see FoundationPanel
      */
    protected FoundationPanel createPanel() {
        return new FoundationPanel();
    }

    /** Returns the FoundationPanel the Applet is using to display its
      * RootView.
      */
    public FoundationPanel panel() {
        return panel;
    }

    /* This method will try to figure out if we are running in the Mozilla
     * thread.  These values are valid for the Nav3.0/NavGold3.0 products.
     * Names appear valid on Sun and NT. This is also called in the EventLoop
     * before trying to setPriority to the thread.
     *
     * This method was removed to attempt to allow IE to call IFC stuff
     * properly. - ddk
     */
    static boolean isMozillaThread(Thread aThread)  {   /// ddk - ADDED FOR JAVASCRIPT SUPPORT
//        ThreadGroup aThreadG = aThread.getThreadGroup();
//
//        return (aThread.getName().equals("main")
//                && aThreadG.getName().equals("main")
//                && aThreadG.getParent().getName().equals("system")
//                && aThreadG.getParent().getParent() == null);
//
        return true;
    }

    /** Any JavaScript function that sends message to an IFC-based Applet must
      * call this method before sending any messages. Returns the Application.
      * The JavaScript function must generate a corresponding
      * <b>popIFCContext()</b> call.
      * This method places the Application object on a stack, so the IFC can
      * properly determine
      * which Application is running when the JavaScript method executes.
      * Failure to call <b>popIFCContext()</b> after the JavaScript finishes
      * may result in the Application not being properly garbage collected.
      * Here's a simple JavaScript example:
      * <PRE>
      *  &ltSCRIPT&gt
      *   function sendToFront() {
      *     SimpleDrawApp = document.applets[0].pushIFCContext();
      *     SimpleDrawApp.drawController.moveToFront();
      *     document.applets[0].popIFCContext();
      *   }
      *  &lt/SCRIPT&gt
      * </PRE>
      * <i><b>Note:</b> This method can only be called from JavaScript
      * code.</i>
      * @see #popIFCContext
      */
    public Application pushIFCContext()   {  /// ddk - ADDED FOR JAVASCRIPT SUPPORT
        if(application == null) {
            return null;
        }

        if(!isMozillaThread(Thread.currentThread()))    {
            throw new InconsistencyException(
                        "pushIFCContext() must be called from JavaScript.");
        }

        if(applicationStack == null)    {
           applicationStack = new Vector();
        }

        applicationStack.addElement(application);
        return application;
    }

    /** Any JavaScript function that sends message to an IFC-based Applet must
      * call this method after sending all messages, to match the previous
      * <b>pushIFCContext()</b> call.<p>
      * <i><b>Note:</b> This method can only be called from JavaScript
      * code.</i>
      * @see #pushIFCContext
      */
    public void popIFCContext()   {    /// ddk - ADDED FOR JAVASCRIPT SUPPORT

        if(!isMozillaThread(Thread.currentThread()))    {
            throw new InconsistencyException(
                        "popIFCContext() must be called from JavaScript.");
        }

        if(applicationStack == null)    {
            throw new InconsistencyException(
            "popIFCContext() called without ever calling pushIFCContext()");
        }

        if(applicationStack.lastElement() != application)   {
            throw new InconsistencyException("popIFCContext() attempted to pop " + applicationStack.lastElement() + " which was not itself:" + application);
        }

        if(applicationStack.removeLastElement() == null)    {
            throw new InconsistencyException("extraneous popIFCContext() called without corresponding pushIFCContext()");
        }
    }

    static Application currentApplication() {   /// ddk - ADDED FOR JAVASCRIPT SUPPORT
        if(applicationStack == null)    {
           return null;
        }

        return (Application)applicationStack.lastElement();
    }

    static void removeApplication(Application app) {   /// ddk - ADDED FOR JAVASCRIPT SUPPORT
        if(applicationStack == null)    {
           return;
        }

        if(app == null) {
            return;
        }
        applicationStack.removeAll((Object)app);
    }
}
