package ec.ui;

import netscape.application.*;

/**
 * Interface ECChecklist uses to notify its owner when the user clicks on an item. 
 * An object interested in this must implement this interface and make itself the ECChecklist's owner,
 * using the setOwner() method. 
 */
public interface ECChecklistOwner  {

/**
 * Sent by <b>checklist</b> when the user clicks on a row of the check list.
 */
	public void itemWasToggled(ECChecklist checklist, int index, boolean newValue);
}
