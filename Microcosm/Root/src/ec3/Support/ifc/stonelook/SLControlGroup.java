/* =====================================================================
 *    FILE: SLControlGroup.java
 *    AUTHOR: John Sullivan
 *    CREATED: June 26, 1997
 *    Copyright (c) 1997 Electric Communities  All Rights Reserved.
 * =====================================================================
 */

package ec.ifc.stonelook;

import ec.ifc.app.ECControlGroup;
import java.util.Vector;
import netscape.application.ContainerView;

/**
 * Subclass of ECControlGroup that adds a stonelook background
 */
public class SLControlGroup extends ECControlGroup {

    //
    // constructors
    //

    /** Creates a new SLControlGroup given an SLScrollGroup */
    public SLControlGroup(SLScrollGroup scrollGroup) {
        super(scrollGroup);
    }

    //
    // protected methods
    //  
    
    /** Overridden to return a borderless SLContainerView */
    protected ContainerView createContainer() {
        ContainerView result = new SLContainerView();
        result.setBorder(null);
        result.setBackgroundColor(StoneLook.dataBackgroundColor());
        return result;
    }
}
