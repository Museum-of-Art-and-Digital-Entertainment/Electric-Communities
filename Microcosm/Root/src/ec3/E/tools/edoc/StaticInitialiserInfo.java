/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

class StaticInitialiserInfo extends MemberInfo {
    /** Constructor for StaticInitialiserInfo matches MemberInfo
     *  @see ec.edoc.MemberInfo#MemberInfo */
    StaticInitialiserInfo(ClassInterfaceInfo containingClass) {
        super("", null, 0, "", containingClass);
    }
}

