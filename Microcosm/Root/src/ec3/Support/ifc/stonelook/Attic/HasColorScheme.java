package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Interface that supports getting and setting a color scheme. Class
 * StoneLook supplies at least two color schemes: StoneLook.STANDARD_COLORS,
 * which by default is shades of blue, and StoneLook.ALTERNATE_COLORS, which
 * by default is shades of purple.
 * @see StoneLook 
 */
public interface HasColorScheme  {

    /** returns the current color scheme */
    public int getColorScheme();

    /** sets the color scheme to a new value */
    public void setColorScheme(int newColorScheme);
}
