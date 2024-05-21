/**
 * ECBitmap.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */

package ec.ifc.app;

import java.awt.Image;
import java.io.ByteArrayInputStream;

import netscape.application.Bitmap;
import ec.misc.graphics.PPMLoader;
import ec.misc.graphics.BmpLoader;
import ec.misc.graphics.ImageLoader;

/**
 * Utility class to deal with images whose data and/or input formats aren't directly 
 * supported by IFC so that they become "IFC compatible"
 */
public class ECBitmap extends Bitmap {

    public static Bitmap bitmapFromByteArray(byte[] imageBytes) {

        Image awtImage = null;

        if ((imageBytes[0] == 'P') && ((imageBytes[1] == '6') || (imageBytes[1] == '3')) && (imageBytes[2] == '\n')) {
            // PPM image
            ByteArrayInputStream bs = new ByteArrayInputStream(imageBytes);
            ImageLoader          il = new PPMLoader();

            il.input(bs);
            awtImage = java.awt.Toolkit.getDefaultToolkit().createImage(il);
        } 
        else if ((imageBytes[0] == 'B') && (imageBytes[1] == 'M')) {
            // BMP image
            ByteArrayInputStream bs = new ByteArrayInputStream(imageBytes);
            ImageLoader          il = new BmpLoader();

            il.input(bs);
            awtImage = java.awt.Toolkit.getDefaultToolkit().createImage(il);
        }
        else {
            awtImage = java.awt.Toolkit.getDefaultToolkit().createImage(imageBytes);
        }

        return netscape.application.AWTCompatibility.bitmapForAWTImage(awtImage);
    }
}

