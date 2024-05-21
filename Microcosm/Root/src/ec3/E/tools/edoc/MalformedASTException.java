/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */

package ec.edoc;

/** This is the exception thrown by a node of the syntax tree if it is
 *  asked to perform an operation on a tree fragment which doesn't adhere
 *  to its expected structure. */
class MalformedASTException extends Exception {
}
