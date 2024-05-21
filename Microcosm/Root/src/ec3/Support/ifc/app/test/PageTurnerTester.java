package ec.ifc.app.test;

import netscape.application.*;
import ec.ifc.app.ECPageTurner;

public class PageTurnerTester extends Application
{
    public static void main (String args[])  {
        PageTurnerTester application = new PageTurnerTester();
        PageTurnerSample sample = new PageTurnerSample(args, application);
        application.run();
    }
}

class PageTurnerSample implements Target, WindowOwner  {

    private static PageTurnerTester myApplication;
    private ExternalWindow myWindow;
    
    // layout constants
    private static final int PAGE_CORNER_HEIGHT = 32;
    private static final int PAGE_CORNER_WIDTH = 32;
    
    // command constants
    private static final String commandQuitApplication = "quit application";
    private static final String commandPageForward = "page forward";
    private static final String commandPageBackward = "page backward";
    
    //
    // constructors
    //
        
    public PageTurnerSample (String args[], PageTurnerTester application)  {
        myApplication = application;
        createAndShowWindow();
    }
    
    //
    // public methods
    //
    
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command)) {
            System.exit(0);
        }
        else if (commandPageForward.equals(command)) {
            System.out.println("received page forward command");
        }
        else if (commandPageBackward.equals(command)) {
            System.out.println("received page backward command");
        }
    }

    public void windowDidBecomeMain(Window window) {
    }
        
    public void windowDidHide(Window window) {
        performCommand(commandQuitApplication, this);
    }
        
    public void windowDidResignMain(Window window){
    }
    
    public void windowDidShow(Window window){
    }

    public boolean windowWillHide(Window window){
        return(true);
    }

    public boolean windowWillShow(Window window){
        return(true);
    }

    public void windowWillSizeBy(Window window, Size size){
    }

    //
    // private methods
    //

    /** set up all the viewables */
    private void createAndShowWindow()  {

        // create and set up window
        myWindow = new ExternalWindow();
        myWindow.setTitle("Page Turner test");
        myWindow.setOwner(this);
        myWindow.rootView().setColor(Color.lightGray);
        myWindow.rootView().setBuffered(true);
//      myWindow.setResizable(false);
        
        Size windowSize = myWindow.windowSizeForContentSize(400, 300);
        System.out.println("setting bounds to 360, 10, " + windowSize.width
            + ", " + windowSize.height);
        myWindow.setBounds(360, 10, windowSize.width, windowSize.height);
        // see if bounds have changed (1.1 bug with non-resizable windows)
        System.out.println("bounds are now " + myWindow.bounds());
        Size contentSize = myWindow.contentSize();
        
        // create and set up page turner
        ECPageTurner pageTurner = new ECPageTurner(
            contentSize.width - PAGE_CORNER_WIDTH,
            0,
            PAGE_CORNER_WIDTH,
            PAGE_CORNER_HEIGHT);
            
        pageTurner.setTarget(this);
        pageTurner.setPageForwardCommand(commandPageForward);
        pageTurner.setPageBackwardCommand(commandPageBackward);
        myWindow.addSubview(pageTurner);
        
        myWindow.show();
        // see if bounds have changed after showing (another 1.1 bug with non-resizable windows)
        System.out.println("just after showing, bounds are " + myWindow.bounds());
        System.out.println("calling bounds() again, bounds are " + myWindow.bounds());
    }
}

