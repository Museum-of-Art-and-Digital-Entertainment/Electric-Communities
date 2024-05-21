/*
 * @(#)IndexBuilder.java    1.2 98/03/18
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
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
import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * Build the mapping of each Unicode character with it's member lists
 * containing members names starting with it. Also build a list for all the
 * Unicode characters which start a member name. Member name is
 * classkind/field/method/constructor name.
 *
 * @since JDK1.2
 * @see java.lang.Character
 * @author Atul M Dambalkar
 */
public class IndexBuilder {

    /**
     * Mapping of each Unicode Character with the member list containing
     * members with names starting with it.
     */
    private Map indexmap = new HashMap();

    // make ProgramElementDoc[] when new toArray is available
    protected Object[] elements;

    /**
     * Constructor. Build the index map.
     *
     * @param root Root of the Tree.
     */
    public IndexBuilder(Root root) {
        buildIndexMap(root);
        Set set = indexmap.keySet();
        elements =  set.toArray();
        Arrays.sort(elements);
    }

    /**
     * Sort the index map. Traverse the index map for all it's elements and
     * sort each element which is a list.
     */
    protected void sortIndexMap() {
        Comparator comparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Doc)o1).name().compareTo(((Doc)o2).name());
            }
        };
        for (Iterator it = indexmap.values().iterator(); it.hasNext(); ) {
            Collections.sort((List)it.next(), comparator);
        }
    }

    /**
     * Get all the members in all the Packages and all the Classes
     * given on the command line. Form separate list of those members depending
     * upon their names.
     *
     * @param root Root of the documemt.
     */
    protected void buildIndexMap(Root root)  {
        PackageDoc[] packages = root.specifiedPackages();
        ClassDoc[] classes = root.specifiedClasses();
        List list = new ArrayList();
        for (int i = 0; i < packages.length; i++) {
            ClassDoc[] classarr = packages[i].allClasses();
            adjustIndexMap(classarr);
            for (int j = 0; j < classarr.length; j++) {
                adjustIndexMap(classarr[j].fields());
                adjustIndexMap(classarr[j].methods());
                adjustIndexMap(classarr[j].constructors());
            }
        }
        adjustIndexMap(classes);
        sortIndexMap();
    }

    /**
     * Adjust list of members according to their names. Check the first
     * character in a member name, and then add the member to a list of members
     * for that particular unicode character.
     *
     * @param elements Array of members.
     */
    protected void adjustIndexMap(ProgramElementDoc[] elements) {
        for (int i = 0; i < elements.length; i++) {
            char ch = Character.toUpperCase(elements[i].name().charAt(0));
            Character unicode = new Character(ch);
            List list = (List)indexmap.get(unicode);
            if (list == null) {
                list = new ArrayList();
                indexmap.put(unicode, list);
            }
            list.add(elements[i]);
        }
    }

    /**
     * Return a map of all the individual member lists with Unicode character.
     *
     * @return Map index map.
     */
    public Map getIndexMap() {
        return indexmap;
    }

    /**
     * Return the sorted list of members, for passed Unicode Character.
     *
     * @param index index Unicode character.
     * @return List member list for specific Unicode character.
     */
    public List getMemberList(Character index) {
        return (List)indexmap.get(index);
    }
}
