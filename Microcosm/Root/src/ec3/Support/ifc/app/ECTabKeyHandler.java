package ec.ifc.app;

/**
 * interface used by objects that want to handle tab key
 * presses received by ECTabKeyDispensers.
 */
public interface ECTabKeyHandler {

    /**
     * Called when a tab key has been dispensed by dispenser. The boolean
     * indicates whether the tab should be considered a forward tab
     * or a backward tab.
     */
    public void handleTabKey(ECTabKeyDispenser dispenser, boolean forward);
}

