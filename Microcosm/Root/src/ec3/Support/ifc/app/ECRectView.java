/* =====================================================================
 *    FILE: ECRectView.java
 *    AUTHOR: Alex McKale
 *    CREATED: Oct 21, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 *
 *  971201  agm     Add proper cursor if movable
 *  971023  agm     Add the resize functionality
 * =====================================================================
 */

package ec.ifc.app;

import java.util.Vector;
import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Target;
import netscape.application.View;

/**
 * RectView class
 */

public class ECRectView extends View {

    private Color color;
    private boolean myIsMovableX = false;
    private boolean myIsMovableY = false;
    private int deltaX, deltaY; 
    private int initialX, initialY; 
    private int myMinX, myMinY; 
    private int myMaxX, myMaxY; 
    Target      myTarget;
    String      myResizedCommand;
   
    /** Constructs a ECRectView.  
      */
    public ECRectView() {
        super();
    }

    public ECRectView(int x, int y, int width, int height, Color aColor) {
        super(x, y, width, height);
        color = aColor;
    }

    /** Draws the ECRectView within the given Rect.
      */
    public void drawView(Graphics g) {
        g.setColor(color);
        g.drawRect(0, 0, bounds.width, bounds.height);
    }
   
    public boolean mouseDown(MouseEvent event) {
        deltaX = event.x;
        deltaY = event.y;
        initialX = x();
        initialY = y();
        return true;
    }
   
    public void mouseDragged(MouseEvent event) {
        if ((myIsMovableX) && (myIsMovableY)) {
            int newX = event.x + deltaX + initialX;
            int newY = event.y + deltaY + initialY;
            if (newX < myMinX) {
                newX = myMinX;
            } else if (newX > myMaxX) {
                newX = myMaxX;
            }
            if (newY < myMinY) {
                newY = myMinY;
            } else if (newY > myMaxY) {
                newY = myMaxY;
            }
            moveTo(newX, newY);
            initialX = x();
            initialY = y();
            superview().draw();
            draw();
        } else if (myIsMovableX) {
            if (event.x + deltaX != x()) {
                int newX = event.x + deltaX + initialX;
                if (newX < myMinX) {
                    newX = myMinX;
                } else if (newX > myMaxX) {
                    newX = myMaxX;
                }
                moveTo(newX, y());
                initialX = x();
                superview().draw();
                draw();
            }
        } else if (myIsMovableY) {
            if (event.y + deltaY != y()) {
                int newY = event.y + deltaY + initialY;
                if (newY < myMinY) {
                    newY = myMinY;
                } else if (newY > myMaxY) {
                    newY = myMaxY;
                }
                moveTo(x(), newY);
                initialY = y();
                superview().draw();
                draw();
            }
        }
    }
   
    public void mouseUp(MouseEvent event) {
        if (myTarget != null) {
            myTarget.performCommand(myResizedCommand, this);
        }
    }
    
    /** Sets the RectView's Target, the object that will receive a
      * <b>performCommand()</b> message when the user clicks the RectView.
      */
    public void setTarget(Target aTarget) {
        myTarget = aTarget;
    }

    /** Returns the RectView's Target.
      * @see #setTarget
      */
    public Target target() {
        return myTarget;
    }

    /** Sets the RectView's command.
      * @see #setTarget
      */
    public void setResizedCommand(String command) {
        myResizedCommand = command;
    }

    /** Returns the RectView's command.
      * @see #setResizedCommand
      */
    public String resizedCommand() {
        return myResizedCommand;
    }

    public void setIsMovableX(boolean isMovable) {
        myIsMovableX = isMovable;
        myMinX = superview().bounds().x;
        myMaxX = superview().bounds().maxX();
    } 

    public void setIsMovableY(boolean isMovable) {
        myIsMovableY = isMovable;
        if (superview() != null) {
            myMinY = superview().bounds().y;
            myMaxY = superview().bounds().maxY();
        }
    } 

    public void setIsMovableX(boolean isMovable, int minX, int maxX) {
        myIsMovableX = isMovable;
        myMinX = minX;
        myMaxX = maxX;
    } 

    public void setIsMovableY(boolean isMovable, int minY, int maxY) {
        myIsMovableY = isMovable;
        myMinY = minY;
        myMaxY = maxY;
    } 

    public int cursorForPoint(int x, int y) {
        if (myIsMovableX && myIsMovableY)
            return View.NE_RESIZE_CURSOR;
        else if (myIsMovableX) 
            return View.W_RESIZE_CURSOR;
        else if (myIsMovableY)
            return View.N_RESIZE_CURSOR;
        return super.cursorForPoint(x,y);
    }
}
