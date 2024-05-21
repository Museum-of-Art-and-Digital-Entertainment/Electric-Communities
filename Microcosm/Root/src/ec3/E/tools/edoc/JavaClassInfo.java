/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** JavaClassInfo is a class used to represent pertinent information about
 *  a normal java class.
 *  @see ec.edoc.EClassInfo
 *  @see ec.edoc.ClassInfo
 */
class JavaClassInfo extends ClassInfo {
    /** Constructor for JavaClassInfo matches Info
     *  @see ec.edoc.Info#Info */
    JavaClassInfo(String name, Comment comment, int modifiers) {
        super(name, comment, modifiers);
    }

    /** Constructor for JavaClassInfo matches ClassInterfaceInfo
     *  @see ec.edoc.ClassInterfaceInfo#ClassInterfaceInfo */
    JavaClassInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super(n, t);
    }

    /* we don't need to handle any special Members in java classes
     * which aren't handled by ClassInfo, hence
     * we don't override addMember, but we might in the future. */
}
