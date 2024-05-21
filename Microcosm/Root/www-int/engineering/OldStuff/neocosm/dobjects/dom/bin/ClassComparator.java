/*
 * @(#)ClassComparator.java 1.4 98/03/18
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import sun.tools.javadoc.*;
import java.util.*;

public class ClassComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        return (((ClassDoc)o1).name())
            .compareTo(((ClassDoc)o2).name());
    }
}
