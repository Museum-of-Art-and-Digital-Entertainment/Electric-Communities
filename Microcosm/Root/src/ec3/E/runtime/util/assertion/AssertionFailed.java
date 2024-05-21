/*
 *  Java/E assertions.  Written by Brian Marick,
 *  June 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */
package ec.util.assertion;

/**
 * Instances of this class are thrown by failing assertions.
 * @see Assertion
 */
public class AssertionFailed extends Error {
  public AssertionFailed() {
    super();
  }
  public AssertionFailed (String msg) {
    super(msg);
  }
}

