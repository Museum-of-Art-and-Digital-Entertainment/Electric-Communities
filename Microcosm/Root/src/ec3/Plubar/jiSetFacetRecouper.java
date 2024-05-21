/*
  jiSetFacetRecouper.java -- Facet capability recouper classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Arturo Bejar
  Sunday December 7 1997
*/

package ec.plubar;

/**
 * Interface implemented by classes that will allow the capability group to set
 * their facet recoupers, in particular applied to whatever we end up calling
 * the unumrouter/deflector for.
 *
 * @author Arturo Bejar
 * @version 1.0
 */
interface jiSetFacetRecouper {
    void setFacetRecouper(jFacetRecouper facetRecouper);
}
