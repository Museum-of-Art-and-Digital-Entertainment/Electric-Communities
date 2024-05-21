package ec.ifc.app;

/**
 * interface used by views that sometimes hand off tab
 * key presses to another object.
 */
public interface ECTabKeyDispenser {

    /** 
     * Returns object that tab keys are handed off to.
     * If null, the dispenser will get the tab key normally.
     */
    public ECTabKeyHandler tabKeyHandler();
    
    /** Sets object that tab keys are handed off to. */
    public void setTabKeyHandler(ECTabKeyHandler newHandler);
}

