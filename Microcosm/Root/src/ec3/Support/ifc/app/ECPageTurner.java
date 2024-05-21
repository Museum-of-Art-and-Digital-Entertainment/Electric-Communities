
package ec.ifc.app;

import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Target;
import netscape.application.View;

/**
 * ECPageTurner is a button-like view that lets the user click to go to
 * "the next page" or "the previous page". The meanings of those terms are
 * up to the implementer, but should generally mean that the user interface
 * is presenting a metaphor of a stack of pages where ordering has some
 * meaning.
 */
public class ECPageTurner extends View {

    //
    // constants
    //
    /** state for setPageState when user can't get to an earlier page */
    public static final int ON_FIRST_PAGE = 0;
    /** state for setPageState when user can't get to a later page */
    public static final int ON_LAST_PAGE = 1;
    /** state for setPageState when user can get to earlier and later pages */
    public static final int ON_MIDDLE_PAGE = 2;

    //
    // state
    //
    /** command sent to target when bottom-left of page turner is clicked */
    protected String myPageBackwardCommand;
    /** command sent to target when top-right of page turner is clicked */
    protected String myPageForwardCommand;
    /** one of ON_FIRST_PAGE, ON_LAST_PAGE, or ON_MIDDLE_PAGE */
    protected int myPageState = ON_MIDDLE_PAGE;
    /** object to which paging commands are sent */
    protected Target myTarget;
    
    //
    // constructors
    //
        
    /** returns a new ECPageTurner with empty bounds */
    public ECPageTurner() {
        this(0, 0, 0, 0);
    }
    
    /** returns a new ECPageTurner with the given bounds */
    public ECPageTurner(Rect bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    /** returns a new ECPageTurner with the given bounds */
    public ECPageTurner(int x, int y, int width, int height) {
        super(x, y, width, height);

        // page turners only look right in upper-right corner       
        setHorizResizeInstruction(LEFT_MARGIN_CAN_CHANGE);
        setVertResizeInstruction(BOTTOM_MARGIN_CAN_CHANGE);
    }
    
    /**
     * Draws a black-outlined gray rectangle with a diagonal line running from
     * upper-left to lower-right and some shading. Meant to look like a
     * folded-over page corner that is placed in the upper right corner of the
     * page area.
     */
    public void drawView(Graphics g) {
        g.setColor(Color.lightGray);
        g.fillRect(bounds);
        g.setColor(Color.black);
        g.drawLine(0, 0, 0, bounds.height);
        g.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);
        g.drawLine(0, 0, bounds.width, bounds.height);
    }
    
    /**
     * Returns true if mouse point is in the part of the PageTurner that
     * turns to the next page, otherwise returns false. Mouse event must
     * be in local coordinates.
     */
    public boolean inPageForwardArea(int x, int y) {
        return x > y;
    }
    
    /**
     * Sends one of the page-turning commands to the target. If the pageState
     * is ON_FIRST_PAGE, the previousPageCommand will not be sent. If the
     * pageState is ON_LAST_PAGE, the nextPageCommand will not be sent.
     */
    public boolean mouseDown(MouseEvent event) {
        boolean pageForward = inPageForwardArea(event.x, event.y);
    
        String commandToPerform = null;
        if (myTarget != null) {
            if (pageForward) {
                if (myPageState != ON_LAST_PAGE) {
                    commandToPerform = myPageForwardCommand;
                }
            }
            else {
                if (myPageState != ON_FIRST_PAGE) {
                    commandToPerform = myPageBackwardCommand;
                }
            }
            if (commandToPerform != null) {
                myTarget.performCommand(commandToPerform, this);
            }
        }
        
        // we're finished with this mouse click; don't care about mouse up etc.
        return false;
    }
    
    /**
     * Returns the command sent to the target when the user clicks
     * in the lower-left half of this ECPageTurner.
     * @see #mouseDown
     */
    public String pageBackwardCommand() {
        return myPageBackwardCommand;
    }

    /**
     * Returns the command sent to the target when the user clicks
     * in the upper-right half of this ECPageTurner.
     * @see #mouseDown
     */
    public String pageForwardCommand() {
        return myPageBackwardCommand;
    }

    /**
     * Returns one of ON_FIRST_PAGE, ON_LAST_PAGE, or ON_MIDDLE_PAGE.
     * This setting determines whether a command is sent in <b>mouseDown</b>
     * and how the ECPageTurner is drawn.
     * @see #mouseDown
     */
    public int pageState() {
        return myPageState;
    }   
    
    /**
     * Sets the current page state. You only need to call this if you don't
     * want the user to be able to click in the page forward area when on
     * the last page, or click in the page backward area when on the first
     * page. The three allowable values are ON_FIRST_PAGE, ON_LAST_PAGE,
     * or ON_MIDDLE_PAGE. If you never set this, it will be left in its
     * default state of ON_MIDDLE_PAGE, and the page turner will appear and
     * act like there are always earlier and later pages that can be reached.
     * @see #pageState
     * @see #mouseDown
     */
    public void setPageState(int newState) {
        if (newState != ON_FIRST_PAGE && newState != ON_LAST_PAGE
                && newState != ON_MIDDLE_PAGE) {
            throw new IllegalArgumentException(
                "unknown page state " + newState);
        }
        
        if (newState == myPageState) {
            return;
        }
        
        myPageState = newState;
        // redraw since we might look different at extremes
        setDirty(true);
    }

    /**
     * Sets the page backward command.
     * @see #pageBackwardCommand
     */
    public void setPageBackwardCommand(String newCommand) {
        myPageBackwardCommand = newCommand;
    }

    /**
     * Sets the page backward command.
     * @see #pageForwardCommand
     */
    public void setPageForwardCommand(String newCommand) {
        myPageForwardCommand = newCommand;
    }
    
    /**
     * Sets the object to which pageForward and pageBackward commands are sent.
     */
    public void setTarget(Target newTarget) {
        myTarget = newTarget;
    }

    /**
     * Returns the object to which pageForward and pageBackward commands
     * are sent.
     */
    public Target target() {
        return myTarget;
    }
}

