/*
  UIFramework.java -- The UI as seen by una.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

import ec.e.start.EEnvironment;

/**
 * User interface framework interface. Implemented by steward.
 *
 * XXX CLEANUP: why is this part of the Pluribus runtime?
 */
public interface UIFramework
{
}

/**
 * User interface framework owner interface. Implemented by crew.
 *
 * XXX CLEANUP: why is this part of the Pluribus runtime?
 */
public interface UIFrameworkOwner
{
    UIFramework framework();
    Object innerFramework();
    void run();
    void initFramework();
}
