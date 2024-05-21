// TextField.java
// By Ned Etcode
// Copyright 1995, 1997 Netscape Communications Corp. All rights reserved.
package netscape.application;

import netscape.util.*;

/** A view subclass to implement a label. A label is a small string, used to
  * indicate the purpose of a control. A label has a command binded with a key.
  * When the key is pressed, the label send the command to a view. The view is
  * usualy the view labeled by the label. A label is always transparent.
  *
  */
public class Label extends View implements Target {
    TextField label;
    Target    target;
    String    command;
    Rect      underlineRect = null;
    int       key;

    static final String LABEL_KEY =  "label";
    static final String TARGET_KEY = "target";
    static final String COMMAND_KEY = "command";
    static final String KEY_KEY = "labelKey";

    final int    UNDERLINE_SIZE = 0;
    final String SEND_COMMAND   = "sendCommand";

    /** Empty constructor, used for unarchiving. **/
    public Label() {
        this("",null);
    }

    /** Create a new label displaying <b>title</b>. The new label
      * will have the minimum size to fit <b>title</b> with <b>font</b>.
      * <b>font</b> can be null. In this case <b>Font.defaultFont()</b>
      * will be used.
      */
    public Label(String title, Font aFont) {
        super();

        label = new TextField(0, 0, 0,0);
        label.setBorder(null);
        label.setStringValue(title);
        label.setFont(aFont);
        label.setTransparent(true);
        label.setEditable(false);
        label.setSelectable(false);
        label.setJustification(Graphics.RIGHT_JUSTIFIED);
        addSubview(label);
        sizeToMinSize();
    }

    /** Set the label justification. <b>aJustification</b> can
      * be Graphics.LEFT_JUSTIFIED, Graphics.CENTERED or
      * Graphics.RIGHT_JUSTIFIED. The default value is
      * Graphics.LEFT_JUSTIFIED
      */
    public void setJustification(int aJustification) {
        label.setJustification(aJustification);
    }

    /** Return the justification for this label **/
    public int justification() {
        return label.justification();
    }

    /** Set the label title **/
    public void setTitle(String aTitle) {
        label.setStringValue(aTitle);
        invalidateUnderlineRect();
    }

    /** Return the label title **/
    public String title() {
        return label.stringValue();
    }

    /** Set the label font **/
    public void setFont(Font aFont) {
        label.setFont(aFont);
        invalidateUnderlineRect();
    }

    /** Return the label font **/
    public Font font() {
        return label.font();
    }

    /** Returns the View's minimum size.
      * the minimum size is always the minimum size
      * to fit the label
      * @see #setMinSize
      */
    public Size minSize() {
        Font font = label.font();
        FontMetrics metrics;
        int width,height;

        metrics = font.fontMetrics();
        width  = metrics.stringWidth(label.stringValue());
        height = metrics.stringHeight();
        return new Size(width,height);
    }

    /**
      * Overriden to invalidate the underline rect
      * and resize the underline field
      */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        super.didSizeBy(deltaWidth,deltaHeight);
        invalidateUnderlineRect();
        label.setBounds(0,0,width(),height()-UNDERLINE_SIZE);
    }

    /** Set the label text color **/
    public void setColor(Color aColor) {
        label.setTextColor(aColor);
    }

    /** Return the label text color **/
    public Color color() {
        return label.textColor();
    }

    /** Set the label target. The target is the object that will receive
      * a command when the key associated with this label is pressed.
      */
    public void setTarget(Target aTarget) {
        target = aTarget;
    }

    /** Return the label target. **/
    public Target target() {
        return target;
    }

    /** Set the label command. The label command is sent to the target
      * when the key associated with this label is pressed.
      */
    public void setCommand(String aCommand) {
        command = aCommand;
    }

    /** Return the label command **/
    public String command() {
        return command;
    }

    /** Set the key that should be pressed for
      * this label to send its command. The letter matching <b>aKey</b>
      * will be underlined. Pressing the key when no view has the focus
      * will send the command. When a view has the focus, pressing ALT + key
      * will send the command.
     */
    public void setCommandKey(int aKey) {
        key = aKey;
        invalidateUnderlineRect();
        removeAllCommandsForKeys();
        setCommandForKey(SEND_COMMAND,null,aKey,KeyEvent.NO_MODIFIERS_MASK,View.WHEN_IN_MAIN_WINDOW);
        setCommandForKey(SEND_COMMAND,null,aKey,KeyEvent.CONTROL_MASK,View.WHEN_IN_MAIN_WINDOW);
        setDirty(true);
    }

    /** Returns the key that will fire the command **/
    public int commandKey() {
        return key;
    }

    /** Overridden to return <b>true</b>
      */
    public boolean isTransparent() {
        return true;
    }

    /** Target implementation.
      * @private
      */
    public void performCommand(String command, Object data) {
        if(SEND_COMMAND.equals(command)) {
            sendCommand();
        }
    }

    void invalidateUnderlineRect() {
        underlineRect = null;
    }

    /** This method is called by drawView() to discover the rect of
      * the black line used to underline the character matching the
      * key. Use this method if you want to draw the underline in
      * a different way.
      */
    public Rect underlineRect() {
        int index;

        if(underlineRect == null) {
            if(key != 0) {
                String s = label.stringValue();
                index = s.indexOf(key);
                if(index == -1) {
                    index = s.toUpperCase().indexOf(key);
                    if(index==-1)
                        index = s.toLowerCase().indexOf(key);
                }

                if(index != -1) {
                    Rect r = label.rectForRange(index,index + 1);

                    underlineRect = new Rect();
                    label.convertRectToView(this,r,underlineRect);
                    underlineRect.y = underlineRect.y + underlineRect.height + UNDERLINE_SIZE - 1;
                    underlineRect.height = 1;
                }
            }

            if(underlineRect == null)
                underlineRect = new Rect(0,0,0,0);
        }
        return underlineRect;
    }

    /** Overriden to underline the letter that matches the key **/
    public void drawView(Graphics g) {
        Rect underlineRect = underlineRect();

        if(underlineRect != null && underlineRect.intersects(g.clipRect())) {
            g.setColor(label.textColor());
            g.fillRect(underlineRect());
        }
    }

    void sendCommand() {
        if(target != null && command != null)
            target.performCommand(command,this);
    }

/* archiving */

    /** Describes the TextField class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.Label",1);
        info.addField(LABEL_KEY,OBJECT_TYPE);
        info.addField(TARGET_KEY,OBJECT_TYPE);
        info.addField(COMMAND_KEY,STRING_TYPE);
        info.addField(KEY_KEY,INT_TYPE);
    }

    /** Encodes the TextField instance.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(LABEL_KEY,label);
        encoder.encodeObject(TARGET_KEY,(Codable)target);
        encoder.encodeString(COMMAND_KEY,command);
        encoder.encodeInt(KEY_KEY,key);
    }

    /** Decodes the TextField instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        label   = (TextField) decoder.decodeObject(LABEL_KEY);
        target  = (Target)    decoder.decodeObject(TARGET_KEY);
        command = decoder.decodeString(COMMAND_KEY);
        key     = decoder.decodeInt(KEY_KEY);
    }
}
