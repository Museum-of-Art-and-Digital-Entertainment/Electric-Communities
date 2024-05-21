package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECPopup;

/**
 * Subclass of Popup that applies stone-texture look.
 */
public class SLPopup extends ECPopup {

    /**
     * Creates a new SLPopup with the stone-texture look. Callers generally
     * won't have to modify the appearance further.  But they will have to set
     * bounds.
     */ 
    public SLPopup() {
        this(0, 0, 0, 0);
    }

    /**
     * Creates a new SLPopup with the stone-texture look. Callers generally
     * won't have to modify the appearance further.
     */ 
    public SLPopup(int x, int y, int width, int height) {

        ListView listView = new SLListView(true);
        listView.setTransparent(false);
        listView.setBackgroundColor(StoneLook.popupColor());
        setPopupList(listView);

        PopupItem prototype = new SLPopupItem();
        prototype.setFont(StoneLook.standardFontBold());
        // this next line should be handled insided Popup.setPrototypeItem, but isn't
        prototype.setPopup(this);
        setPrototypeItem(prototype);

        setBorder(new SLBezelBorder(BezelBorder.RAISED));
        setBounds(x, y, width, height);
        updatePopupAppearance();
    }

    /**
     * Overridden to fix bug causing superclass's method not to work
     * with partly-transparent borders.
     */
    public void drawView(Graphics g) {
        Border  border;
        Rect    itemRect;
        Color   color = null;

        border = border();

        ListItem selectedItem = selectedItem();
        ListView popupList = popupList();
                
        if (selectedItem == null && popupList.selectedItem() == null) {
            selectItem(popupList.itemAt(0));
            selectedItem = selectedItem();
        }

        if (selectedItem != null) {
            itemRect = new Rect(border.leftMargin(),
                                border.topMargin(),
                                bounds.width - border.widthMargin(),
                                bounds.height - border.heightMargin());
            g.pushState();
            g.setClipRect(itemRect);
            
            // this is the bug fix from IFC's Popup: they were filling the
            // entire rect rather than the inside-border rect, so the fill
            // color was sticking out past the rounded corners of our border
            if (!popupList.isTransparent() && selectedItem.isTransparent()) {
                g.setColor(popupList.backgroundColor());
                g.fillRect(itemRect);
            }
            
            if (!isEnabled()) {
                color = selectedItem.textColor();
                selectedItem.setTextColor(StoneLook.disabledStaticTextColor());
            }
            selectedItem.drawInRect(g, itemRect);
            if (!isEnabled()) {
                selectedItem.setTextColor(color);
            }
            g.popState();
        }

        border.drawInRect(g, 0, 0, width(), height());
    }
    
    /** Overridden to change the popup image and border appropriately */    
    public void setEnabled(boolean newValue) {
        if (newValue == isEnabled()) {
            return;
        }
        
        super.setEnabled(newValue);
        updatePopupAppearance();
    }
    
    //
    // private methods
    //
    
    private void updatePopupAppearance() {
        Color borderColor;
        Color insideColor;
        Image newImage;
        
        if (isEnabled()) {
            newImage = StoneLook.popupArrowImage();
            borderColor = StoneLook.editableBorderColor();
            insideColor = StoneLook.popupColor();
        } else {
            newImage = StoneLook.popupArrowDisabledImage();
            borderColor = StoneLook.disabledBorderColor();
            insideColor = StoneLook.disabledPopupColor();
        }
        
        setPopupImage(newImage);
        if (border() instanceof SLBezelBorder) {
            SLBezelBorder slbb = (SLBezelBorder)border();
            slbb.setLineColor(borderColor); 
            slbb.setInsideColor(insideColor);
        }
        
        setDirty(true);
    }
}
