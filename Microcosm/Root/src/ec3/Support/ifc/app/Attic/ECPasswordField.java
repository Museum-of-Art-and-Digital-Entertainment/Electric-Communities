
// ECPasswordField.java
// adapted from the IFC sample code (fixed some bugs)
// Copyright 1997 Electric Communities. All rights reserved.

package ec.ifc.app;

import netscape.application.*;
import netscape.util.*;

/** A TextField subclass that conceals its "password" by displaying a string
  * of the same length consisting of the same character.
  */
public class ECPasswordField extends ECTextField {
    /** The PasswordField's "password." */
    private String  password = "";

    /** Character used in place of the ECPasswordField's real contents. */
    private char    drawableCharacter = '*';

    /** Indicates whether or not the ECPasswordField's password has been
      * loaded.  If so, no drawing occurs.
      */
    private boolean passwordLoaded = false;

    /** Constructs a ECPasswordField with origin (<b>0</b>, <b>0</b>) and zero
      * width and height.
      */
    public ECPasswordField() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ECPasswordField with bounds <B>rect</B>.
      */
    public ECPasswordField(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ECPasswordField with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public ECPasswordField(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /** Sets the character displayed in place of the ECPasswordField's password.
     */
    public void setDrawableCharacter(char aChar) {
        drawableCharacter = aChar;
        super.replaceRangeWithString(new Range(0,charCount()),
                                     drawableStringForString(password));
        setDirty(true);
    }

    /** Returns the character displayed in place of the ECPasswordField's
      * password.
      * @see #setDrawableCharacter
      */
    public char drawableCharacter() {
        return drawableCharacter;
    }

    /** Overridden to make ECPasswordField edit its password.
      */
    public void keyDown(KeyEvent anEvent) {
        if( loadPassword() ) {
            disableDrawing();
            super.keyDown(anEvent);
            reenableDrawing();
            unloadPassword();
        } else
            super.keyDown(anEvent);
    }

    /** Overridden to do nothing when the password has been loaded into the
      * ECPasswordField.
      */
    public void drawViewStringAt (Graphics g, String aString, int x, int y) {
        // this causes the text to flash while the user is typing
        if (passwordLoaded)
            return;

        super.drawViewStringAt(g, aString, x, y);
    }

    /** Overridden to store the ECPasswordField's password in a different
      * location. <b>unloadPassword()</b> computes the string the ECPasswordField
      * should display instead.
      * @see #unloadPassword
      */
    public void setStringValue(String aString) {
        if (loadPassword()) {
            disableDrawing();
            super.setStringValue(aString);
            reenableDrawing();
            unloadPassword();
        } else {
            super.setStringValue(aString);
        }
    }

    /** Overridden to return the ECPasswordField's password.
      */
    public String stringValue() {
        String result;

        if (loadPassword()) {
            result = super.stringValue();
            unloadPassword();
        } else
            result = super.stringValue();
        return result;
    }

    /** Overridden to return the ECPasswordField's password.
     */
    public void replaceRangeWithString(Range aRange, String aString) {
        if (loadPassword()) {
            super.replaceRangeWithString(aRange, aString);
            unloadPassword();
        } else {
            super.replaceRangeWithString(aRange, aString);
        }
    }

    /** Overridden to return the ECPasswordField's password.
      */
    public String stringForRange(Range aRange) {
        String result;

        if (loadPassword()) {
            result = super.stringForRange(aRange);
            unloadPassword();
        } else
            result = super.stringForRange(aRange);
        return result;
    }

    /** Overridden to return the ECPasswordField's password.
      */
    public String selectedStringValue() {
        String result;

        if (loadPassword()) {
            result = super.selectedStringValue();
            unloadPassword();
        } else
            result = super.selectedStringValue();
        return result;
    }

    /** Overridden to return the ECPasswordField's password.
      */
    public void setIntValue(int anInt) {
        if (loadPassword()) {
            super.setIntValue(anInt);
            unloadPassword();
        } else
            super.setIntValue(anInt);
    }

    /** Overridden to return the ECPasswordField's password.
      */
    public int intValue() {
        int result;

        if (loadPassword()) {
            result = super.intValue();
            unloadPassword();
        } else
            result = super.intValue();
        return result;
    }

    /** Overridden to load the drawable string before computing the position,
      * then reload the ECPasswordField's password.
      */
    public int xPositionOfCharacter(int charNumber) {
        int result;

        if (passwordLoaded) {
            Range r = new Range(0, charCount());

            password = super.stringForRange(r);
            super.replaceRangeWithString(r, drawableStringForString(password));
            result = super.xPositionOfCharacter(charNumber);
            super.replaceRangeWithString(r,password);
        } else
            result = super.xPositionOfCharacter(charNumber);
        return result;
    }

    /** Overridden to load the drawable string before computing the character
      * number, then reload the ECPasswordField's password.
      */
    public int charNumberForPoint(int x) {
        int result;

        if (passwordLoaded) {
            Range r = new Range(0, charCount());

            password = super.stringForRange(r);
            super.replaceRangeWithString(r, drawableStringForString(password));
            result = super.charNumberForPoint(x);
            super.replaceRangeWithString(r, password);
        } else
            result = super.charNumberForPoint(x);
        return result;
    }

    /** Overridden to ensure that <b>selecteRange()</b> isn't called with an
      * equal range.  Selecting the same range might cause the ECPasswordField to
      * forget where the selection started.
      */
    public void selectRange(Range aRange) {
        Range currentSelection = selectedRange();

        if (!currentSelection.equals(aRange))
            super.selectRange(aRange);
    }


    /** Returns the string that should be displayed in place of <b>aString</b>.
      */
    private String drawableStringForString(String aString) {
        if (aString.length() > 0) {
            char buf[] = new char[aString.length()];
            int i,c;

            for(i = 0, c = aString.length(); i < c ; i++)
                buf[i] = drawableCharacter;

            return new String(buf);
        } else
            return "";
    }

    /** Loads the password into the ECPasswordField. Returns <b>true</b> if the
      * password was not already loaded.
      */
    private boolean loadPassword() {
        if (passwordLoaded)
            return false;
        else {
            passwordLoaded = true;
            super.replaceRangeWithString(new Range(0, charCount()), password);
            return true;
        }
    }

    /** Unloads the password from the ECPasswordField and places the new value in
      * the password instance variable. Does nothing if the ECPasswordField does
      * not contain the password.
     */
    private void unloadPassword() {
        if (passwordLoaded) {
            Range r = new Range(0, charCount());

            password = super.stringForRange(r);
            super.replaceRangeWithString(r, drawableStringForString(password));
            setDirty(true);
            passwordLoaded = false;
        }
    }
}
