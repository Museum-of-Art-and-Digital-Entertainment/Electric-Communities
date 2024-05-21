package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of PopupItem that applies the stone-texture look. SLPopup uses
 * this by default, so you generally won't need to explicitly instantiate
 * this class.
 * @see SLPopup
 */
public class SLPopupItem extends PopupItem {
    
    //
    // constructors
    //
    
    public SLPopupItem() {
        setSelectedColor(StoneLook.listItemSelectedColor());
    }
    
    //
    // public methods
    //


/**
 * Overridden to draw selected item with colored text.
 */
    protected void drawStringInRect(Graphics g, String title,
                                 Font titleFont, Rect textBounds,
                                 int justification) {

        if (!isSelected()) {
            super.drawStringInRect(g, title, titleFont, textBounds, justification);
            return;
        }

        g.setColor(StoneLook.listItemSelectedTitleColor());
        g.setFont(titleFont);
        g.drawStringInRect(title, textBounds, justification);
    }

/**
 * Overridden to draw the popup image only if the popup is not popped up.
 * How much pop could a popup pop up if a popup could pop up pop?
 */
    public void drawInRect(Graphics g, Rect boundsRect) {
        Popup popup = popup();

        // Trick PopupItem.drawInRect into doing nothing other than calling
        // ListItem.drawInRect by temporarily disconnecting the popup. If
        // we don't do this, PopupItem.drawInRect wants to draw the image
        // whether or not the popup is popped up, and it also fills an
        // ugly rectangle behind the image.
        if (popup != null) {
            setPopup(null);
        }

        super.drawInRect(g, boundsRect);

        if (popup == null)
            return;

        setPopup(popup);

        Image popupImage = popup.popupImage();
        if (popupImage == null)
            return;

        // only draw popup's image if the popup is not currently expanded
        if (popup.popupList().isInViewHierarchy())
            return;

        // only draw popup's image on selected item
        if (popup.selectedItem() != this)
            return;
            
        popupImage.drawAt(g, boundsRect.x + boundsRect.width - popupImage.width(),
                             boundsRect.y + (boundsRect.height - popupImage.height())/2);
    }   
}
