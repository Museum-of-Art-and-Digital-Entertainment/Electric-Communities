package ec.ifc.app;

import netscape.application.*;

/**
 * A subclass of ListView that marks selected items with a checkmark image.
 * A mouse click on a row toggles the selection of that row. 
 *
 * @see            ECChecklistOwner
 * @author         John Sullivan
 */
public class ECChecklist extends ListView {
    private Image checkedImage = null;
    private Image uncheckedImage = null;
    private ECChecklistOwner owner = null;

/**
 * Constructs an ECChecklist with background color Color.lightGray that
 * allows multiple and empty selections, and that uses IFC's standard
 * checkmark image.
 */  
    public ECChecklist () {
        super();

        setBackgroundColor(Color.lightGray);
        setAllowsMultipleSelection(true);
        setAllowsEmptySelection(true);
        setCheckedImage(Bitmap.bitmapNamed("netscape/application/CheckMark.gif"));
        setUncheckedImage(null);
    }

    //
    // drawing
    //
/**
 * Keeps the prototype item's selected color the same as the background color,
 * so checked items are not drawn with a different color than unchecked items.
 * Does the same for all existing items.
 */
    public void setBackgroundColor(Color newColor)  {
        super.setBackgroundColor(newColor);
        // selected state just draws checkmark, needs to use same color as unselected
        prototypeItem().setSelectedColor(newColor);
        
        // update selected color on items already in list
        int count = count();
        for (int index = 0; index < count; ++index) {
            itemAt(index).setSelectedColor(newColor);
        }
    }

/**
 * Returns the image drawn next to checked items. By default this is
 * IFC's built-in check mark image.
 * @see #setCheckedImage
 */
    public Image getCheckedImage () {
        return checkedImage;
    }

/**
 * Returns the image drawn next to unchecked items. By default this is null.
 * @see #setUncheckedImage
 */
    public Image getUncheckedImage () {
        return uncheckedImage;
    }

/**
 *
 * Sets the image used to draw the check marks.
 * @see ECChecklist#getCheckedImage
 */
    public void setCheckedImage (Image newImage)  {
        checkedImage = newImage;
        prototypeItem().setSelectedImage(checkedImage);

        // update checkmark image on items already in list
        int count = count();
        for (int index = 0; index < count; ++index) {
            itemAt(index).setSelectedImage(checkedImage);
        }
    }

/**
 *
 * Sets the image drawn next to unchecked items.
 * @see ECChecklist#getUncheckedImage
 */
    public void setUncheckedImage (Image newImage)  {
        uncheckedImage = newImage;
        prototypeItem().setImage(uncheckedImage);

        // update image on items already in list
        int count = count();
        for (int index = 0; index < count; ++index) {
            itemAt(index).setImage(uncheckedImage);
        }
    }

    //
    // selecting items
    //
/**
 * If this checklist is disabled, does nothing. Otherwise, toggles the checked state
 * of the clicked item and, if this checklist has an owner, calls 
 * ECChecklistOwner.itemWasToggled on the owner.
 * @see ECChecklistOwner#itemWasToggled
 */
    public boolean mouseDown(MouseEvent event)  {
        if (isEnabled())
            toggleItem(itemForPoint(event.x, event.y));

        // don't bother with dragging or mouse-ups
        return (false);
    }

/**
 * Flip the selected state of the item at <b>index</b>, and
 * notify owner. Returns whether or not the item ends up selected.
 */
    public boolean toggleItemAt(int index) {
        return toggleItem(itemAt(index));
    }

/**
 * Flip the selected state of <b>item</b>, and notify owner.
 * Returns whether or not the item ends up selected.
 */
    public boolean toggleItem(ListItem item)  {
        boolean wasSelected = item.isSelected();
        if (wasSelected)
            deselectItem(item);
        else
            selectItem(item);

        // tell owner that this item changed state
        if (owner != null)
            owner.itemWasToggled(this, indexOfItem(item), !wasSelected);
    
        return (!wasSelected);
    }

    //
    // owner
    //
/**
 * Returns the object that gets notified when the user clicks on an item.
 * @see ECChecklist#setOwner
 */
    public ECChecklistOwner getOwner()  {
        return (owner);
    }

/**
 * Sets which object will get notified when the user clicks on an item.
 * @see ECChecklist#getOwner
 */
    public void setOwner(ECChecklistOwner newOwner)  {
        owner = newOwner;
    }
}

