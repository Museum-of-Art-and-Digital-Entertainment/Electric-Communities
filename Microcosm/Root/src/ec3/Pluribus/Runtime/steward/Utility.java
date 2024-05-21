/*
  Utility.java -- Pluribus runtime utility classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Exception to indicate security problems in the Pluribus runtime
 */
class UnumSecurityViolationException extends RuntimeException
{
    /**
     * The standard exception constructor
     */
    UnumSecurityViolationException(String msg) {
        super(msg);
    }
}
