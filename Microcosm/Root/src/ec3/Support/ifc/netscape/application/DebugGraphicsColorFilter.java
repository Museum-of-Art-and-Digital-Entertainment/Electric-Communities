// DebugGraphicsColorFilter.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.awt.image.RGBImageFilter;

class DebugGraphicsColorFilter extends RGBImageFilter {
    Color color;

    DebugGraphicsColorFilter(Color c) {
        canFilterIndexColorModel = true;
        color = c;
    }

    public int filterRGB(int x, int y, int rgb) {
        return color.rgb() | (rgb & 0xFF000000);
    }
}
