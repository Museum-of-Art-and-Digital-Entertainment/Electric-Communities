package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;

/**
 * Subclass of ECTabItem that applies the stone-texture look in a way
 * where the selected tab appears like it's part of the front surface
 * and the other tabs appear to be behind it. This is used
 * by SLLayeredTabView.
 * @see SLLayeredTabView
 * @see SLButtonTabItem
 */
public class SLLayeredTabItem extends ECTabItem {

    /** Overridden to draw stone-look bezel inside tab bounding rect. */
    protected void drawBackground(Graphics g, Rect boundsRect) {
        
        // First draw a bezel as if this were a button. The selected tab
        // is drawn longer on the side that abuts the content so that when
        // it is clipped its edge lines will end abruptly rather than showing
        // the corners. This makes it blend in better with the raised
        // bezel of the content rect that the tabs are resting against.
        // Then cover up the edge of the tab that abuts the content. The
        // selected tab "opens up" to the background texture. The other
        // tabs have a solid edge at the bottom.

        int bezelThickness = StoneLook.bezelThickness(true);
        
        Rect bezelRect, contentEdge;
        
        if (mySelected) {
            // we'll modify this rect before drawing, but we don't want to
            // modify the rect passed in
            bezelRect = new Rect(boundsRect);
            // we won't use contentEdge in this case
            contentEdge = null;
        } else {
            // we'll just use boundsRect for the bezel, so no point making
            // a new one
            bezelRect = boundsRect;
            // we'll draw over the content edge, so we'll start with the
            // bounds and then compute the edge rect based on location
            contentEdge = new Rect(boundsRect);
        }
        
        Image bezelEdgeImage = null;
        switch (myTabView.getLocation()) {
            case ECTabView.ABOVE_CONTENT:
                if (mySelected) {
                    // push bottom edge of lowered tab down out of clip rect
                    bezelRect.height += bezelThickness;
                }
                else {
                    // compute rect for bottom edge
                    contentEdge.y = contentEdge.maxY() - bezelThickness;
                    contentEdge.height = bezelThickness;
                    
                    bezelEdgeImage = StoneLook.bezelImageN(true);               
                }
                break;
            case ECTabView.BELOW_CONTENT:
                if (mySelected) {
                    // push top edge of lowered tab up out of clip rect
                    bezelRect.y -= bezelThickness;
                    bezelRect.height += bezelThickness;
                }
                else {
                    // compute rect for top edge
                    contentEdge.y = 0;
                    contentEdge.height = bezelThickness;
                    
                    bezelEdgeImage = StoneLook.bezelImageS(true);
                }
                break;
            case ECTabView.LEFT_OF_CONTENT:
                if (mySelected) {
                    // push right edge of lowered tab right out of clip rect
                    bezelRect.width += bezelThickness;
                }
                else {
                    // compute rect for right edge
                    contentEdge.x = contentEdge.maxX() - bezelThickness;
                    contentEdge.width = bezelThickness;

                    bezelEdgeImage = StoneLook.bezelImageW(true);
                }
                break;
            case ECTabView.RIGHT_OF_CONTENT:
                if (mySelected) {
                    // push left edge of lowered tab left out of clip rect
                    bezelRect.x -= bezelThickness;
                    bezelRect.width += bezelThickness;
                }
                else {
                    // compute rect for left edge
                    contentEdge.width = bezelThickness;

                    bezelEdgeImage = StoneLook.bezelImageE(true);
                }
                break;
        }

        // first draw the bezeled border as if this were a button
        // Note that this rect isn't exactly the original rect in
        // the lowered case 
        SLBezelBorder.drawBezel(g, bezelRect.x, bezelRect.y, 
                                bezelRect.width, bezelRect.height,
                                true, true);

        // fill inside of the bordered rectangle with the background texture.
        // Use clip rect and then draw texture in entire bounds so texture
        // won't shift between selected and unselected version of same tab.
        Image texture = StoneLook.backgroundTexture();
        g.pushState();
        g.setClipRect(new Rect(bezelRect.x + bezelThickness,
                          bezelRect.y + bezelThickness,
                          bezelRect.width - 2*bezelThickness,
                          bezelRect.height - 2*bezelThickness));
        texture.drawTiled(g, boundsRect);
        g.popState();
        
        // lay a texture over the edge of the tab abutting the content.
        // We do this except for the lowered tab, in which case
        // the image will not have been specified.
        if (bezelEdgeImage != null) {
            bezelEdgeImage.drawTiled(g, contentEdge);       
        }
    }
    
}
