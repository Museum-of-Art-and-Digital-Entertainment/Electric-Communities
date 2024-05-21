package ec.ifc.app;

import netscape.util.*;
import netscape.application.*;

/**
 * A subclass of ListItem that draws an image proportionally scaled
 * to fit in the rectangle. The label is drawn below the text.
 *
 * @author        Alex McKale
 *
 * Modification History:
 *
 *  971006  dima    Reworked drawInRect, added forced scaling
 *  970623  agm     Added swatch code.
 *  970618  agm     Added edit mode flag routines.
 *  970611  agm     Added justification routines.
 *                  Changed the scaling to not force square.
 */
public class ECIconItem extends ListItem {

    private int     myStringJustification = Graphics.CENTERED;
    private int     myIconJustification = Graphics.CENTERED;
    private boolean myInEditMode = false;
    private int     myNumLinesInTitle = 1;
    private int     myItemInset = 4;
    private boolean myForceScaling = false;
        
    private boolean myIsSwatch = true;    
    private Color   mySelectedTextColor = Color.black;
    // default to tan
    private Color   myBackgroundColor = new Color(254, 213, 161);
    
    /** Returns extra space on all four edges of grid rect that
     * isn't drawn into; this is the space between adjacent items.
     */
    public int itemInset() {
        return myItemInset;
    }
    
    /** returns color to draw title in when item is selected */
    public Color selectedTextColor() {
        return mySelectedTextColor;
    }
 
    /** sets flag for swatch addition.
      * hence should not be highlighted.
      */

    public void setIsSwatch (boolean isSwatch) {
        myIsSwatch = isSwatch;
    }

    /** Tracks if the icon is "already being edited" and
      * hence should not be highlighted.
      */
    public void setInEditMode(boolean inEditMode) {
        myInEditMode = inEditMode;
    }
    
    /**
     * Sets extra space on all four edges of grid rect that
     * isn't drawn into; provides space between adjacent
     * items.
     */
    public void setItemInset(int newInset) {
        myItemInset = newInset;
    }
 
    /** Returns the background color */
    public Color backgroundColor(Color color) {
        return myBackgroundColor;
    }

    /** Sets the background color
      */
    public void setBackgroundColor(Color color) {
        myBackgroundColor = color;
    }
    
    /** Sets the color used to draw text in selected item */
    public void setSelectedTextColor(Color newColor) {
        mySelectedTextColor = newColor;
    }

    /** Sets the justification (Graphics.LEFT_JUSTIFIED,
      * Graphics.CENTERED, Graphics.RIGHT_JUSTIFIED) the
      * ECIconItem uses to draw its text.
      */
    public void setJustification(int aJustification) {
        if (aJustification < Graphics.LEFT_JUSTIFIED ||
            aJustification > Graphics.RIGHT_JUSTIFIED) {
            return;
        }

        myStringJustification = aJustification;
    }
 
    /** Sets the justification (Graphics.LEFT_JUSTIFIED,
      * Graphics.CENTERED, Graphics.RIGHT_JUSTIFIED) the
      * ECIconItem uses to draw its icon.
      */
    public void setIconJustification(int aJustification) {
        if (aJustification < Graphics.LEFT_JUSTIFIED ||
            aJustification > Graphics.RIGHT_JUSTIFIED) {
            return;
        }

        myIconJustification = aJustification;
    }

    /** 
     * Sets the number of lines that title will be limited to
     * ATTN: more code should be here to actually enforce it!!! (=dima)
     */
    public void setNumLinesInTitle( int numLinesInTitle) {
        myNumLinesInTitle = numLinesInTitle;
    }

    /** Returns the minimum height required to display the ListItem's title
      * and Image, if any.
      */
    public void setForceScalingIcon(boolean toForceScaling) {
        myForceScaling = toForceScaling;
    }

    /** Returns the minimum height required to display the ListItem's title
      * and Image, if any.
      */
    public int minHeight() {

        // agm tentatively set the height of icons and title
        // to be a fixed value.
        
        return 64;
    }

    /**
     * Draws the scaled image according to the justification, scaled to fit.
     */
    public void drawInRect(Graphics g, Rect boundsRect)  {

        drawBackground(g, boundsRect);
        int titleHeight = titleHeight();

        // this will be the largest rectangle that the image can be drawn in.
        // The actual rectangle holding the image will almost always be smaller,
        // but will be always be contained by this rectangle
        Rect imageArea = new Rect(boundsRect);
        
        // enforce space around edges; SLIconItem uses this to draw a border
        imageArea.growBy(-myItemInset, -myItemInset);
        
        // Provide room for the text at the bottom of the icon.
        imageArea.height -= titleHeight;
        
        // imageRect is the rectangle into which the image will exactly fit
        Rect imageRect = new Rect();
            
        int imageWidth = image().width();
        int imageHeight = image().height();
        if (myForceScaling || imageHeight > imageArea.height 
                           || imageWidth > imageArea.width) {
            // compute the largest sub-rect of imageArea that
            // will hold a proportionally scaled image
            float scaleFactor = Math.min((float)imageArea.width/(float)imageWidth, 
                                         (float)imageArea.height/(float)imageHeight);
            imageRect.width = (int)(scaleFactor*imageWidth);
            imageRect.height = (int)(scaleFactor*imageHeight);
        } else {
            // make imageRect exactly the same size as the image
            imageRect.width = imageWidth;
            imageRect.height = imageHeight;
        }
        
        // position rect that will contain image within the total
        // area available to the image
        imageRect.y = imageArea.y + (imageArea.height - imageRect.height)/2;
        switch (myIconJustification) {
            case Graphics.CENTERED:
                imageRect.x = imageArea.x + (imageArea.width - imageRect.width)/2;
                break;
            case Graphics.LEFT_JUSTIFIED:
                imageRect.x = imageArea.x;
                break;
            case Graphics.RIGHT_JUSTIFIED:
                imageRect.x = imageArea.maxX() - imageRect.width;
                break;
        }

        // draw scaled in all cases. This will draw normal sized in the cases
        // where we don't need to scale, because imageRect will be exactly the
        // size of the image
        image().drawScaled(g, imageRect);

        if (myIsSwatch) {
            Polygon myBorder = new Polygon();
            addZigZagToPolygon(myBorder, imageRect);
            g.drawPolygon(myBorder);
            
            // add the outside border
            myBorder.addPoint(imageRect.x, imageRect.y);
            myBorder.addPoint(imageRect.x + imageRect.width, imageRect.y);
            myBorder.addPoint(imageRect.x + imageRect.width, imageRect.y + imageRect.height);
            myBorder.addPoint(imageRect.x, imageRect.y + imageRect.height);
            myBorder.addPoint(imageRect.x, imageRect.y);
            if (isSelected() && !myInEditMode) {
                g.setColor(selectedColor());
            } else {
                g.setColor(myBackgroundColor);
            }
            g.fillPolygon(myBorder);
        
            myBorder = new Polygon();
            addZigZagToPolygon(myBorder, imageRect);
            g.setColor(Color.black);
            g.drawPolygon(myBorder);
        }
 
        String title = title();
        if (title != null && title.length() > 0) {
            if (isSelected()) {
                g.setColor(selectedTextColor());
            } else {
                g.setColor(textColor());
            } 
            Font font = font();
            g.setFont(font);
            Rect titleRect = new Rect(boundsRect.x + myItemInset, 
                                    boundsRect.maxY() - myItemInset - titleHeight,
                                    boundsRect.width - 2*myItemInset,
                                    titleHeight);
            // truncate title if necessary to fit in title rect
            title = ECStringUtilities.ellipsizedString(title, titleRect.width, font);
            g.drawStringInRect(title, titleRect, myStringJustification);
        }

    }

    /** 
     * Returns the height of the title
     */
    public int titleHeight() {
        return myNumLinesInTitle * font().fontMetrics().charHeight();
    }

    /** 
     *  Adds the points to the polygon to represent the "swatch"
     */
    private void addZigZagToPolygon(Polygon myBorder, Rect imageRect) { 

        // now do the inside border
        int x = 3; int y = 0;
        if ((imageRect.width % 3) == 1)
            x = 2;
        else
            x = 3;
        while (x < imageRect.width) {
            myBorder.addPoint(imageRect.x + x, imageRect.y + y);
            if (y == 3)
                y = 0;
            else 
                y = 3;
            x += 3;
        }
        
        // right edge
        y += (imageRect.width - x);
        x = imageRect.width;
        while (y < imageRect.height) {
            myBorder.addPoint(imageRect.x + x, imageRect.y + y);
            if (x == imageRect.width)
                x = imageRect.width - 3;
            else 
                x = imageRect.width;
            y += 3;
        }
        
        // bottom edge
        x -= (imageRect.height - y);
        y = imageRect.height;
        while (x > 0) {
            myBorder.addPoint(imageRect.x + x, imageRect.y + y);
            if (y == imageRect.height)
                y = imageRect.height - 3;
            else 
                y = imageRect.height;
            x -= 3;
        }
        // left edge
        y -= (x);
        x = 0;
        while (y > 0) {
            myBorder.addPoint(imageRect.x + x, imageRect.y + y);
            if (x == 3)
                x = 0;
            else 
                x = 3;
            y -= 3;
        }

        if ((imageRect.width % 3) == 1)
            x = 2;
        else
            x = 3;
        myBorder.addPoint(imageRect.x + x, imageRect.y + 0);
        
    }


}

