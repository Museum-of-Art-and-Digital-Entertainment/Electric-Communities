/**
 * ECImageView.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */

package ec.ifc.app;

import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.View;

/**
 * This is a very light weight subclass of View designated
 * just to draw an image.  One can also use ContainerView
 * for that purpose.  The advantage of this class is that
 * ContainerView takes more memory, as it also maintains
 * text field, font, color, etc.
 */
public class ECImageView extends View {

    protected Image myImage;

    public ECImageView (Image image) {
        this(image, 0, 0);
    }

    public ECImageView (Image image, int x, int y) {
        myImage = image;
        setBoundsFromImageSize(x, y);
    }

    public void drawView(Graphics g) {
        if (myImage != null) {      
            myImage.drawAt(g, 0, 0);
        }
    }

    public Image image () {
        return myImage;
    }

    public void setImage (Image image) {
        myImage = image;
        setBoundsFromImageSize(x(), y());
        setDirty(true);
    }

    private void setBoundsFromImageSize(int x, int y) {
        int width, height;
        if (myImage == null) {
            width = 0;
            height = 0;
        } else {
            width = myImage.width();
            height = myImage.height();
        }
        setBounds(x, y, width, height);
    }
}

