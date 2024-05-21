/**
 * ECStringUtilities.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * started by John Sullivan
 *
 * A collection of String-related utilities used by other classes
 * in this package and elsewhere.
 */
package ec.ifc.app;

import netscape.application.Font;
import netscape.application.FontMetrics;

public class ECStringUtilities {

    /**
     * Returns a copy of string with a trailing ellipsis and enough
     * trailing characters removed so that the result fits into
     * <b>width</b> using <b>font</b>
     */
    public static String ellipsizedString(String string, int width, Font font) {
        FontMetrics fm = font.fontMetrics();
        boolean     trimmed = false;
        String      ellipsisString = "...";
        
        width -= fm.stringWidth(ellipsisString);
        while (fm.stringWidth(string) > width) {
            trimmed = true;
            // be careful not to shrink the poor string below zero-length
            int stringLength = string.length();
            if (stringLength <= 0) {
                break;
            }
            string = string.substring(0, stringLength - 1);
        }

        if (trimmed) {
            return string + ellipsisString;
        }
        else {
            return string;
        }       
    }
}

