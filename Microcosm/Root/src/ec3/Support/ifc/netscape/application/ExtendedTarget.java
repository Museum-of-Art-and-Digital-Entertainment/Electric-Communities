// ExtendedTarget.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Interface enabling a Target to declare the commands it can
  * perform. For example, a TextView implements the ExtendedTarget
  * interface and returns <b>true</b> when asked if it can perform the
  * ExtendedTarget.SET_FONT command. The static strings defined by this
  * interface have special meaning within the IFC library.
  * @see Target
  * @note 1.0 defined cut/copy/paste commands
  */


public interface ExtendedTarget extends Target {
    /** Returns <b>true</b> if the object can perform <b>command</b>.
      */
    public boolean canPerformCommand(String command);

    /** Command to change font to the one passed as the "data" argument. */
    public static final String SET_FONT = "setFont";

    /** Command to set the current font in the Font Chooser. */
    public static final String NEW_FONT_SELECTION = "newFontSelection";

    /** Command to make the FontChooser visible. */
    public static final String SHOW_FONT_CHOOSER = "showFontChooser";

    /** Command to make the ColorChooser visible. */
    public static final String SHOW_COLOR_CHOOSER = "showColorChooser";

    /** Command to copy
      *
      */
    public final static String COPY = "copy";

    /** Command to cut
      *
      */
    public final static String CUT = "cut";

    /** Command to paste
      *
      */
    public final static String PASTE = "paste";
}


