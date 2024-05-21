
// SLPasswordField.java
// By John Sullivan
// Copyright 1997 Electric Communities.  All rights reserved.

package ec.ifc.stonelook;

import ec.ifc.app.ECPasswordField;
import netscape.application.*;

/** An ECTextField subclass that applies the stone look. */
public class SLPasswordField extends ECPasswordField {

    /** Constructs a SLPasswordField with origin (<b>0</b>, <b>0</b>) and zero
      * width and height.
      */
    public SLPasswordField() {
        super();
        setup();
    }

    /** Constructs a SLPasswordField with bounds <B>rect</B>.
      */
    public SLPasswordField(Rect rect) {
        super(rect);
        setup();
    }

    /** Constructs a SLPasswordField with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public SLPasswordField(int x, int y, int width, int height) {
        super(x, y, width, height);
        setup();
    }

    /** apply the stone look */
    private void setup() {
        setBackgroundColor(StoneLook.editableTextFillColor());
        setSelectionColor(StoneLook.editableTextSelectionColor());
        setFont(StoneLook.standardFontBold());
        setBorder(new SLBezelBorder(BezelBorder.LOWERED));
    }
}
