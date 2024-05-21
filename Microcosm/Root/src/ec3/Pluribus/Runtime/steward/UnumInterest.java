/*
  UnumInterest.java -- Classes having to do with parties concerned about unum
                       death.

  XXX CLEANUP: This file should be renamed, as the class it was named after
  no longer exists.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Interface for classes to implement if they want to be notified about an unum
 * being killed.
 */
public interface UnumKillHandler
{
    /**
     * Called by the runtime when the unum that asked for the call was killed
     */
    void noteUnumKilled();
}   

