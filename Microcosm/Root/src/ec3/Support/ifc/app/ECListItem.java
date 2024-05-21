
/**
 * ECListItem.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 *  980302  agm     added style to allow the image to be scaled or centered, etc.
 *  971116  agm     added multi-columns
 */

package ec.ifc.app;

import java.lang.Math;

import ec.util.assertion.Assertion;

import netscape.application.Font;
import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.ListItem;
import netscape.application.Rect;
import java.util.Vector;

/**
 * Subclass of ListItem
 * Overrides some ListItem's methods to allow some extra 
 * functionality
 */
public class ECListItem extends ListItem {
    
    private int mySpacing = 2;
    private int myIndentation = 0;
    private Vector myTabStops = null;


    /**
     *  the height of the box to display image in
     */
    private int myImageHeight;
    
    /**
     *  the style to use for drawing image.  Can be
     *  Image.CENTERED or Image.SCALED.
     *  At this time Image.TILED doesn't work as we don't see a need for it.
     */
    private int myImageStyle = Image.CENTERED;

    /**
     *  the width of the box to display image in
     */
    private int myImageWidth;
    
    
    /**
     *  Set the indentation from the left hand side where the
     *  ListItem begins to be drawn.
     */
     
     public void setIndentation(int indentation) {
        myIndentation = indentation;
    }

    /**
     *  The indentation from the left hand side where the
     *  ListItem begins to be drawn.
     */

    public int indentation() {
        return myIndentation;
    }
 
    /**
     *  Sets the Size for the image.
     *  Only relevant if the Style is Scaled or Tiled.
     */
 
    public void setImageSize(int width, int height) {
        myImageWidth = width;
        myImageHeight = height;
    }

    /**
     *  Sets the style for the display of the image.
     *  Either Image.CENTERED or Image.SCALED.
     *  At this time Image.TILED doesn't work as we don't see a need for it.
     */
 
    public void setImageStyle(int style) {
        Assertion.test(style == Image.CENTERED || style == Image.SCALED);
        myImageStyle = style;
    }
 
 
    /**
     *  Sets the spacing between the image and the text
     */
 
    public void setSpacing(int spacing) {
        mySpacing = spacing;
    }

    /**
     *  The spacing between the image and the text
     */

    public int spacing() {
        return mySpacing;
    }

    /**
     *  Sets the tab stops for each column
     */
 
    public void setTabStops(Vector tabStops) {
        myTabStops = tabStops;
    }

    /**
     *  The spacing between the image and the text
     */

    public Vector tabStops() {
        return myTabStops;
    }
 
    /** 
      * Returns the minimum width required to display the ListItem's title
      * and Image, if any.  Overridden to correctly deal with two-liners,
      * spacing and indentation.
      */
    public int minWidth() {
        Font    font;
        int     width = 0;

        if ((myImageWidth != 0) && (myImageStyle == Image.SCALED)) {
            width = myImageWidth;
        } else {
            if (image() != null)
                width = image().width();

            if (selectedImage() != null)
                if (selectedImage().width() > width)
                    width = selectedImage().width();
        }
        
        width += myIndentation + mySpacing;

        font = font();
        if (font != null) 
        {
            int newLinePosition = title().indexOf("\n");
            if (newLinePosition > 0) {
                // whichever of the two lines is longer
                width += java.lang.Math.max(font.fontMetrics().stringWidth(title().substring(0, newLinePosition)),
                    font.fontMetrics().stringWidth(title().substring(newLinePosition + 1, title().length())));
            } else {
                // length of the text
                width += font.fontMetrics().stringWidth(title());
            }
        }
        
        // Beware of magic constant!  ALERT!
        // Inherited from the IFC source code(!)
        if (width > 0) {
            width += 3;
        }

        return width;
    }

    /**
     * Overridden to be able to draw two-liners
     */
    public void drawInRect(Graphics g, Rect boundsRect) {
        String title = title();

        if (title == null || title.length() == 0) {
            super.drawInRect(g, boundsRect);
            return;
        }

        int newLinePosition = title.indexOf("\n");
        
        Image   theImage;
        int     width, height;

        drawBackground(g, boundsRect);


        Image image = image();
        Font  font = font();
        Image selectedImage = selectedImage();

        if (isSelected()) {
            theImage = selectedImage();
        } 
        else {
            theImage = image();
        }

        width = height = 0;
        if (image != null) {
            width = image.width();
            height = image.height();
        }
        if (selectedImage != null) {
            if (selectedImage.width() > width) {
                width = selectedImage.width();
            }
            if (selectedImage.height() > height) {
                height = selectedImage.height();
            }
        }

        int theImageBoxWidth = width;

        // force size
        if (myImageHeight != 0 && myImageWidth != 0) {
            theImageBoxWidth = myImageWidth;

            // will hold a proportionally scaled image
            if ((myImageStyle == Image.SCALED) && (width > myImageWidth || height > myImageHeight)) {
                theImageBoxWidth = myImageWidth;
                float scaleFactor = Math.min((float)myImageWidth/(float)width, 
                                             (float)myImageHeight/(float)height);
                width = (int)(scaleFactor*width);
                height = (int)(scaleFactor*height);
            } 
        }
        
        if (theImage != null) {
            theImage.drawWithStyle(g, boundsRect.x + myIndentation + (width - myImageWidth)/2,
                            boundsRect.y + (boundsRect.height - height) / 2, width, height, myImageStyle);
        }
        
        if (myTabStops != null) {
            int numberOfTabs = myTabStops.size();
            int i = 0;
            int columnIndex = 0;
            int nextColumnIndex;
            Rect tmpRect = new Rect();
            tmpRect.y = boundsRect.y;
            tmpRect.height = boundsRect.height;
                
            // we are sure we want to do this at least once.
            while ((columnIndex > 0) || (i == 0)) {
            
                // special cases
                if (i == 0) {
                    tmpRect.x = boundsRect.x + myIndentation + mySpacing + theImageBoxWidth; 
                    tmpRect.width = ((Long) myTabStops.elementAt(0)).intValue();
                } else if (i == numberOfTabs) {
                    tmpRect.x = boundsRect.x + ((Long) myTabStops.elementAt(i - 1)).intValue();
                    tmpRect.width = boundsRect.maxX() - ((Long) myTabStops.elementAt(i - 1)).intValue();
                } else {
                    tmpRect.x = boundsRect.x + ((Long) myTabStops.elementAt(i - 1)).intValue();
                    tmpRect.width = ((Long) myTabStops.elementAt(i)).intValue() - ((Long) myTabStops.elementAt(i - 1)).intValue();
                }
                
                nextColumnIndex = title.indexOf("\t", columnIndex) + 1;
                
                g.pushState();
                g.setClipRect(tmpRect, true);
                if (nextColumnIndex > 0) {  
                        drawStringInRect(g, 
                                 title.substring(columnIndex, nextColumnIndex - 1), 
                                 font, 
                                 tmpRect, 
                                 Graphics.LEFT_JUSTIFIED);
                    
                } else {
                    drawStringInRect(g, 
                                     title.substring(columnIndex, title.length()), 
                                     font, 
                                     tmpRect, 
                                     Graphics.LEFT_JUSTIFIED);
                }
                g.popState();
                
                i++;
                columnIndex = nextColumnIndex;
            }
        
        
        } else {
            if (newLinePosition > 0) {
                Rect tmpRect = new Rect(boundsRect.x + myIndentation + mySpacing + theImageBoxWidth, 
                                        boundsRect.y,
                                        boundsRect.width - mySpacing - theImageBoxWidth,
                                        boundsRect.height / 2);
                drawStringInRect(g, 
                                 title.substring(0, newLinePosition), 
                                 font, 
                                 tmpRect, 
                                 Graphics.LEFT_JUSTIFIED);

                tmpRect.y += tmpRect.height;
                drawStringInRect(g, 
                                 title.substring(newLinePosition + 1, title.length()),
                                 font, 
                                 tmpRect, 
                                 Graphics.LEFT_JUSTIFIED);
            }
            else {
                Rect tmpRect = new Rect(boundsRect.x + myIndentation + mySpacing + theImageBoxWidth, 
                                        boundsRect.y,
                                        boundsRect.width - mySpacing - theImageBoxWidth,
                                        boundsRect.height);

                drawStringInRect(g, 
                                 title, 
                                 font, 
                                 tmpRect, 
                                 Graphics.LEFT_JUSTIFIED);

            }
        }
    }
}
