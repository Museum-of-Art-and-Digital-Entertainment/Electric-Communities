// GraphicsState.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

class GraphicsState implements Cloneable {
    java.awt.Graphics   awtGraphics;
    Font                font;
    Color               color;
    Rect                clipRect;
    Rect                absoluteClipRect;
    int                 xOffset;
    int                 yOffset;
    Color               xorColor;
    int                 debugOptions;

    /* These are duplicated from DebugGraphics so that the class doesn't
       need to be brought in. */
    static final int     LOG_OPTION      = 1 << 0;
    static final int     FLASH_OPTION    = 1 << 1;
    static final int     BUFFERED_OPTION = 1 << 2;
    static final int     NONE_OPTION     = -1;


    public Object clone() {
        GraphicsState newState = null;

        try {
            newState = (GraphicsState) super.clone();
        } catch (Exception e) {
        }

        if (newState != null) {
            newState.clipRect = null;
            newState.absoluteClipRect = null;
            newState.awtGraphics = null;
        }

        return newState;
    }

    public String toString() {
        StringBuffer buf;

        buf = new StringBuffer();

        buf.append("Font: " + font + ", ");
        buf.append("Color: " + color + ", ");
        buf.append("Translation: (" + xOffset + ", " + yOffset + "), ");
        buf.append("xor: " + xorColor + ", ");
        buf.append("absoluteClipRect: " + absoluteClipRect + ", ");
        buf.append("debugOption: " + debugOptions);

        return buf.toString();
    }

    boolean debugLog() {
        return (debugOptions & LOG_OPTION) == LOG_OPTION;
    }

    boolean debugFlash() {
        return (debugOptions & FLASH_OPTION) == FLASH_OPTION;
    }

    boolean debugBuffered() {
        return (debugOptions & BUFFERED_OPTION) == BUFFERED_OPTION;
    }

}
