/*
  jiCapabilityRecouper.java -- Facet capability recouper classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Arturo Bejar
  Sunday December 7 1997
*/

package ec.plubar;

import ec.e.run.EObject;

/**
 * Interface to be implemented by the CapabilityRecouper that is instantiated
 * by the facet when it is serialized.
 *
 * @author Arturo Bejar
 * @version 1.0
 */
public interface jiCapabilityRecouper {
    public EObject getCapability();
}
