package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;

/**
 * Subclass of ECTabItem that applies the stone-texture look in a way
 * where the selected tab looks pressed in and the other tabs look
 * raised like buttons. This is used by SLButtonTabView.
 * @see SLButtonTabView
 * @see SLLayeredTabItem
 */
public class SLButtonTabItem extends ECTabItem {

    /** Overridden to draw stone-look bezel inside tab bounding rect. */
    protected void drawBackground(Graphics g, Rect boundsRect) {
        
        // First draw a bezel as if this were a button. The selected
        // tab is lowered, others are raised. The lowered tab is drawn
        // longer on the side that abuts the content so that when it is
        // clipped its edge lines will end abruptly rather than showing
        // the corners. This makes it blend in better with the lowered
        // bezel of the content rect that the tabs are resting against.
        // Then cover up the edge of the tab that abuts the content. The
        // selected tab "opens up" to the background texture. The other
        // tabs have a solid edge at the bottom.

        boolean raised = !mySelected;
        int bezelThickness = StoneLook.bezelThickness(raised);
        
        Rect bezelRect, contentEdge;
        
        if (raised) {
            // bezel will be drawn in specified bounds rect in this case
            bezelRect = boundsRect;
            
            // we're going to overlay a texture on the edge of the bezel
            // abutting the content. We'll start with the bounds rect
            // and modify appropriately based on location
            contentEdge = new Rect(boundsRect);
        } else {
            // we're going to modify the bezelRect in this case, so make a new
            // Rect to avoid changing the one passed in
            bezelRect = new Rect(boundsRect);
            
            // contentEdge isn't used for the lowered case
            contentEdge = null;
        }
        
        Image bezelEdgeImage = null;
        switch (myTabView.getLocation()) {
            case ECTabView.ABOVE_CONTENT:
                if (raised) {
                    // compute rect for bottom edge
                    contentEdge.y = contentEdge.maxY() - bezelThickness;
                    contentEdge.height = bezelThickness;
                    
                    bezelEdgeImage = StoneLook.bezelImageS(raised);             
                }
                else {
                    // push bottom edge of lowered tab down out of clip rect
                    bezelRect.height += bezelThickness;
                }
                break;
            case ECTabView.BELOW_CONTENT:
                if (raised) {
                    // compute rect for top edge
                    contentEdge.y = 0;
                    contentEdge.height = bezelThickness;
                    
                    bezelEdgeImage = StoneLook.bezelImageN(raised);
                }
                else {
                    // push top edge of lowered tab up out of clip rect
                    bezelRect.y -= bezelThickness;
                    bezelRect.height += bezelThickness;
                }
                break;
            case ECTabView.LEFT_OF_CONTENT:
                if (raised) {
                    // compute rect for right edge
                    contentEdge.x = contentEdge.maxX() - bezelThickness;
                    contentEdge.width = bezelThickness;

                    bezelEdgeImage = StoneLook.bezelImageE(raised);
                }
                else {
                    // push right edge of lowered tab right out of clip rect
                    bezelRect.width += bezelThickness;
                }
                break;
            case ECTabView.RIGHT_OF_CONTENT:
                if (raised) {
                    // compute rect for left edge
                    contentEdge.width = bezelThickness;

                    bezelEdgeImage = StoneLook.bezelImageW(raised);
                }
                else {
                    // push left edge of lowered tab left out of clip rect
                    bezelRect.x -= bezelThickness;
                    bezelRect.width += bezelThickness;
                }
                break;
        }

        // first draw the bezeled border as if this were a button
        // Note that this rect isn't exactly the original rect in
        // the lowered case 
        SLBezelBorder.drawBezel(g, bezelRect.x, bezelRect.y, 
                                bezelRect.width, bezelRect.height,
                                raised, true);

        // fill inside of the bordered rectangle with the background texture
        Image texture = StoneLook.backgroundTexture();
        texture.drawTiled(g, bezelRect.x + bezelThickness,
                          bezelRect.y + bezelThickness,
                          bezelRect.width - 2*bezelThickness,
                          bezelRect.height - 2*bezelThickness);
        
        // lay a texture over the edge of the tab abutting the content.
        // We do this except for the lowered tab, in which case
        // the image will not have been specified.
        if (bezelEdgeImage != null) {
            bezelEdgeImage.drawTiled(g, contentEdge);       
        }
    }
    
}
