package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;

/**
 * Subclass of ECTabItem that applies the stone-texture look. This is
 * used by SLTabView.
 * @see SLTabView
 */
public class SLTabItem extends ECTabItem {

    private static final Color SELECTED_HORIZ_TAB_HIGHLIGHT_COLOR
        = StoneLook.selectedTabEdgeColor(true); 

    //
    // constructors
    //
    public SLTabItem() {
        super();
        setFont(StoneLook.standardFontBold());
    }
    
    /** Overridden to set up colors based on tabview's orientation */
    protected void setTabView(ECTabView newTabView) {
        super.setTabView(newTabView);
        
        int location = newTabView.location();
        boolean isHorizontal = 
            location == ECTabView.ABOVE_CONTENT 
            || location == ECTabView.BELOW_CONTENT;

        myTitleColor = StoneLook.tabTitleColor(isHorizontal);
        mySelectedTitleColor = StoneLook.selectedTabTitleColor(isHorizontal);
        myBackgroundColor = StoneLook.tabFillColor(isHorizontal);
        mySelectedColor = StoneLook.selectedTabColor(isHorizontal);
        myEdgeColor = StoneLook.tabEdgeColor(isHorizontal);
        mySelectedEdgeColor = StoneLook.standardBorderColor();
    }
    

    //
    // public methods
    //

    /** Overridden to draw border inside tab bounding rect. */
    protected void drawBackground(Graphics g, Rect boundsRect) {
    
        boolean looksSelected = mySelected || myHilited;
        int location = myTabView.location();
        
        // First draw a border around the tab. The selected tab
        // is drawn longer on the side that abuts the content so that when
        // it is clipped its edge lines will end abruptly rather than
        // showing the corners. This makes it blend in better with the
        // border of the content rect that the tabs are resting against.
        // Then fill in the tab with a color. Both of these operations are
        // clipped so that the edge of the tab that abuts the content will
        // look right.
        boolean isHorizontal =
                (location == ECTabView.ABOVE_CONTENT
                 || location == ECTabView.BELOW_CONTENT);
        
        // horizontal tabs have a thinner edge than vertical tabs
        int borderThickness = isHorizontal
            ? StoneLook.bezelThickness()
            : SLSlabBorder.borderThickness();

        // we'll draw over the content edge, so we'll start with the
        // bounds and then compute the edge rect based on location
        Rect edgelessRect = new Rect(boundsRect);

        // we'll modify this rect before drawing, but we don't want to
        // modify the rect passed in
        Rect bezelRect = new Rect(boundsRect);
        
        switch (location) {
            case ECTabView.ABOVE_CONTENT:
                if (looksSelected) {
                    // push bottom edge of lowered tab down out of clip rect
                    bezelRect.height += borderThickness;
                } else {
                    edgelessRect.height -= borderThickness;
                }
                break;
            case ECTabView.BELOW_CONTENT:
                if (looksSelected) {
                    // push top edge of lowered tab up out of clip rect
                    bezelRect.y -= borderThickness;
                    bezelRect.height += borderThickness;
                } else {
                    edgelessRect.height -= borderThickness;
                    edgelessRect.y += borderThickness;
                }
                break;
            case ECTabView.LEFT_OF_CONTENT:
                if (looksSelected) {
                    // push right edge of lowered tab right out of clip rect
                    bezelRect.width += borderThickness;
                } else {
                    edgelessRect.width -= borderThickness;              
                }
                break;
            case ECTabView.RIGHT_OF_CONTENT:
                if (looksSelected) {
                    // push left edge of lowered tab left out of clip rect
                    bezelRect.x -= borderThickness;
                    bezelRect.width += borderThickness;
                } else {                    
                    edgelessRect.width -= borderThickness;
                    edgelessRect.x += borderThickness;
                }
                break;
        }

        g.pushState();
        g.setClipRect(edgelessRect);

        // draw edges of tab
        if (looksSelected) {
            if (isHorizontal) {
                SLBezelBorder.drawBezel(g, bezelRect.x, bezelRect.y,
                                        bezelRect.width, bezelRect.height,
                                        true,
                                        mySelectedEdgeColor,
                                        mySelectedColor,
                                        SELECTED_HORIZ_TAB_HIGHLIGHT_COLOR);
            } else {
                // draw the big thick slab border around selected vertical item
                SLSlabBorder.drawBorder(g, bezelRect.x, bezelRect.y,
                                        bezelRect.width, bezelRect.height);
                                        
                // draw the little angled bit where the border around the item
                // meshes with the border of the container that the tabs are
                // presumably overlapping neatly. This piece is not drawn on the
                // top or bottom-most pixels of the tab view, because there you
                // want the horizontal lines in the item's slab border to
                // meet up with the horizontal lines in the top or bottom of
                // the container that the tabs overlap.
                boolean isLeft = location == ECTabView.LEFT_OF_CONTENT;
                Color notchColors[] = isLeft
                    ? StoneLook.slabBorderHighlightColors()
                    : StoneLook.slabBorderShadowColors();
                int count = notchColors.length;
                int tabRight = edgelessRect.maxX();
                int tabLeft = edgelessRect.x;
                int tabBottom = edgelessRect.maxY();
                int tabTop = edgelessRect.y;
                boolean meetsTopEdge = tabTop == 0;
                boolean meetsBottomEdge = tabBottom == myTabView.height();
                
                for (int i = 0; i < count; i += 1) {
                    g.setColor(notchColors[i]);
                    int x = isLeft
                        ? tabRight - count + i
                        : tabLeft + count - 1 - i;
                                        
                    // top edge of tab
                    if (!meetsTopEdge) {                    
                        g.drawLine(x, tabTop, x, tabTop + i);
                    }

                    // bottom edge of tab
                    if (!meetsBottomEdge) {
                        g.drawLine(x, tabBottom, x, tabBottom - i);                     
                    }
                }
            }
        } else {
            SLBezelBorder.drawBezel(g, bezelRect.x, bezelRect.y, 
                                    bezelRect.width, bezelRect.height,
                                    true,
                                    myEdgeColor, myBackgroundColor, null);          
        }

        // fill inside of the tab
        
        int fillInset = looksSelected ? borderThickness : StoneLook.bezelThickness();
        g.setColor(looksSelected ? mySelectedColor : myBackgroundColor);
        g.fillRect(bezelRect.x + fillInset, bezelRect.y + fillInset,
                   bezelRect.width - 2*fillInset,
                   bezelRect.height - 2*fillInset);

        g.popState();
    }
    
    /** Overridden to draw hilited title like selected title */
    public void drawInRect(Graphics g, Rect boundsRect, boolean toDrawString) {
        drawBackground(g, boundsRect);
        boolean looksSelected = mySelected || myHilited;
        if (toDrawString) {
            drawStringInRect(g, myTitle, boundsRect,
                looksSelected ? mySelectedTitleColor : myTitleColor);
        }
    }
    
}
