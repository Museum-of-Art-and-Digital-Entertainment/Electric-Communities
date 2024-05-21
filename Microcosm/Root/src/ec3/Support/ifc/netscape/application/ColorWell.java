// ColorWell.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** DragWell subclass that accepts and vends drag-and-dropped Colors.  When the
  * user drops a Color onto a ColorWell, the ColorWell sets its color to that
  * dropped color and sends a command to its Target.
  * @note 1.0 Changes for keyboard IU, added Target interface
  */

public class ColorWell extends DragWell implements DragDestination,Target {
    Target      target;
    String      command;
    int         origX, origY;

    final static String TARGET_KEY = "target", COMMAND_KEY = "command";

    /** Sending this command to a ColorWell will show the color chooser
      *
      */
    public static final String SHOW_COLOR_CHOOSER = "showColorChooser";

    /* constructors */

    /** Constructs a ColorWell with origin (0, 0) and zero
      * width and height.
      */
    public ColorWell() {
        this(0, 0, 0, 0);

    }
    /** Constructs a ColorWell with bounds <B>rect</B>.
      */
    public ColorWell(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ColorWell with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public ColorWell(int x, int y, int width, int height) {
        super(x, y, width, height);

        setColor(Color.blue);
        _setupKeyboard();
    }

    /** Sets the ColorWell's color and then calls <B>draw()</B> to redraw it.
      * If you do not want to immediately redraw the ColorWell, you should
      * first call its <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setColor(Color aColor) {
        if (aColor != null) {
            setData(aColor);
            draw();
        }
    }

    /** Returns the ColorWell's color.
      * @see #setColor
      */
    public Color color() {
        return (Color)data();
    }

    /** Overridden to enforce setting of Colors only.
      * @see #setColor
      */
    public void setData(Object anObject) {
        if (!(anObject instanceof Color)) {
            throw new InconsistencyException(
                                    "ColorWells can only contain colors");
        } else {
            super.setData(anObject);
        }
    }

    /** Returns an Image containing the current Color, suitable for dragging.
      */
    public Image image() {
        Bitmap colorBitmap = new Bitmap(12, 12);
        Graphics g = new Graphics(colorBitmap);

        g.setColor(Color.black);
        g.drawRect(0, 0, 12, 12);

        g.setColor((Color)data);
        g.fillRect(1, 1, 10, 10);
        g.dispose();

        colorBitmap.setTransparent(false);
        return colorBitmap;
    }

    /** Overriden to throw an error.  ColorWells always return an Image
      * representing the current color.
      */
    public void setImage(Image image) {
        throw new InconsistencyException("Can't set image on ColorWell");
    }

    /** Overriden to throw an error.  ColorWells always return a drag type
      * of Color.COLOR_TYPE.
      */
    public void setDataType(String dataType) {
        throw new InconsistencyException("Can't set data type on ColorWell");
    }

    /** Returns Color.COLOR_TYPE.
      */
    public String dataType() {
        return Color.COLOR_TYPE;
    }

    /** Sets the ColorWell's Target, the object the ColorWell notifies when
      * the user drops a Color onto it.
      * @see Target#performCommand
      */
    public void setTarget(Target aTarget) {
        target = aTarget;
    }

    /** Returns the ColorWell's Target.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Sets the command sent by the ColorWell to its Target when the user
      * drops a Color into it.
      * @see #setTarget
      */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Returns the command sent by the ColorWell to its Target.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Draws the ColorWell by drawing its Border, filled with the ColorWell's
      * color.
      * @see DragWell#setBorder
      */
    public void drawView(Graphics g) {
        g.setColor(color());
        g.fillRect(border.leftMargin(), border.topMargin(),
                   bounds.width - border.widthMargin(),
                   bounds.height - border.heightMargin());

        border.drawInRect(g, 0, 0, bounds.width, bounds.height);
    }

    /** Initiates a drag-session where the user can drag the ColorWell's color,
      * unless the ColorWell is disabled (in which case nothing happens).  A
      * double-click in the well invokes the ColorChooser.
      * @see DragWell#setEnabled
      * @see ColorChooser
      * @return <b>false</b> if disabled, <b>true</b> otherwise.
      */
    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled()) {
            return false;
        }

        origX = event.x;
        origY = event.y;

        return true;
    }

    /** Overridden to support creating a Color drag-session.
      * @see #mouseDown
      */
    public void mouseDragged(MouseEvent event) {
        DragSession     dragSession;
        Point           absPoint;

        if (Math.abs(event.x - origX) > 3 ||
            Math.abs(event.y - origY) > 3) {

            new DragSession(this, image(),
                            event.x - 6, event.y - 6,
                            event.x, event.y,
                            dataType(), data);
        }
    }

    /** Overridden to support creating a Color drag-session.
      * @see #mouseDown
      */
    public void mouseUp(MouseEvent event) {
        ColorChooser    colorChooser;

        colorChooser = rootView().colorChooser();
        colorChooser.setColor(color());
        rootView().showColorChooser();
    }

    /** Returns the ColorWell if the DragSession has a data type of
      * Color.COLOR_TYPE, <b>null</b> otherwise.
      * @see DragSession#dataType
      */
    public DragDestination acceptsDrag(DragSession session, int x, int y) {
        if (Color.COLOR_TYPE.equals(session.dataType())) {
            return this;
        } else {
            return null;
        }
    }

    /** DragDestination support method.
      * @see DragDestination#dragEntered
      */
    public boolean dragEntered(DragSession session) {
        return true;
    }

    /** DragDestination support method.
      * @see DragDestination#dragMoved
      */
    public boolean dragMoved(DragSession session) {
        return true;
    }

    /** DragDestination support method.
      * @see DragDestination#dragExited
      */
    public void dragExited(DragSession session) {
    }

    /** Drag-and-drop destination support method, called when the user
      * releases a DragSession's Image over the ColorWell. If the DragSession
      * represents a Color, the ColorWell calls its <B>setColor()</b> method
      * with the dropped Color and sends its command to its Target. If the
      * ColorWell is disabled, or the DragSession doesn't represent a Color,
      * the ColorWell refuses the object.  Returns <b>true</b> if the session
      * was accepted, <b>false</b> otherwise.
      * @see #setColor
      * @see DragWell#setEnabled
      * @see DragDestination#dragDropped
      */
    public boolean dragDropped(DragSession session) {
        Object          theItem;

        if (!isEnabled() || session.source() == this) {
            return false;
        }

        theItem = session.data();
        if (theItem == null || !(theItem instanceof Color)) {
            return false;
        }

        setColor((Color)theItem);

        sendCommand();

        return true;
    }

    /** Called by the ColorWell to send its command to its Target.
     * @see #setTarget
     * @see #setCommand
     */
    public void sendCommand() {
        if (target != null) {
            target.performCommand(command, this);
        }
    }


    /** Overriden to show the color chooser **/
    public void performCommand(String command, Object data) {
        if(SHOW_COLOR_CHOOSER.equals(command)) {
            if(rootView() != null)
                rootView().showColorChooser();
        }
    }

/* archiving */


    /** Describes the ColorWell class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ColorWell", 1);
        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(COMMAND_KEY, STRING_TYPE);
    }

    /** Encodes the ColorWell.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(TARGET_KEY, (Codable)target);
        encoder.encodeString(COMMAND_KEY, command);
    }

    /** Decodes the ColorWell.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        target = (Target)decoder.decodeObject(TARGET_KEY);
        command = decoder.decodeString(COMMAND_KEY);
    }


    void _setupKeyboard() {
        removeAllCommandsForKeys();
        setCommandForKey(SHOW_COLOR_CHOOSER,KeyEvent.RETURN_KEY,View.WHEN_SELECTED);
    }

    /** Overriden to return true
      *
      */
    public boolean canBecomeSelectedView() {
        return true;
    }
}
