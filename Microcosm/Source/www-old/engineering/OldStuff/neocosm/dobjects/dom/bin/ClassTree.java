/*
 * @(#)ClassTree.java   1.4 98/03/18
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
 * Build Class Hierarchy for all the Classes. This class builds the Class
 * Tree and the Interface Tree separately.
 *
 * @see java.util.HashMap
 * @see java.util.List
 * @see sun.tools.javadoc.Type
 * @see sun.tools.javadoc.ClassDoc
 * @author Atul M Dambalkar
 */
public class ClassTree {

    /**
     * List of baseclasses. Contains only java.lang.Object. Can be used to get
     * the mapped listing of sub-classes.
     */
    private List baseclasses = new ArrayList();

    /**
    * Mapping for each Class with their SubClasses
    */
    private Map subclasses = new HashMap();

    /**
     * List of base-interfaces. Contains list of all the interfaces who do not
     * have super-interfaces. Can be used to get the mapped listing of
     * sub-interfaces.
     */
    private List baseinterfaces = new ArrayList();

    /**
    * Mapping for each Interface with their SubInterfaces
    */
    private Map subinterfaces = new HashMap();

    /**
    * Mapping for each Interface with classes who implement it.
    */
    private Map implementingclasses = new HashMap();

    /**
     * Constructor. Build the Tree using the Root of this Javadoc.
     *
     * @param root Root of the Document.
     */
    public ClassTree(Root root) {
        buildTree(root.classes());
    }

    /**
     * Constructor. Build the tree for the given array of classes.
     *
     * @param classes Array of classes.
     */
    public ClassTree(ClassDoc[] classes) {
        buildTree(classes);
    }

    /**
     * Generate mapping for the sub-classes for every class in this run.
     * Return the sub-class list for java.lang.Object which will be having
     * sub-class listing for itself and also for each sub-class itself will
     * have their own sub-class lists.
     *
     * @param classes all the classes in this run.
     */
    private void buildTree(ClassDoc[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isClass()) {
                processClass(classes[i]);
            } else {    // this is an interface.
                processInterface(classes[i]);
                List list  = (List)implementingclasses.get(classes[i]);
                if (list != null) {
                    Collections.sort(list);
                }
            }
        }

        Collections.sort(baseinterfaces);
        for (Iterator it = subinterfaces.values().iterator(); it.hasNext(); ) {
            Collections.sort((List)it.next());
        }
        for (Iterator it = subclasses.values().iterator(); it.hasNext(); ) {
            Collections.sort((List)it.next());
        }
    }

    /**
     * For the class passed map it to it's own sub-class listing.
     * For the Class passed, get the super class,
     * if superclass is non null, (it is not "java.lang.Object")
     *    get the "value" from the hashmap for this key Class
     *    if entry not found create one and get that.
     *    add this Class as a sub class in the list
     *    Recurse till hits java.lang.Object Null SuperClass.
     *
     * @param cd class for which sub-class mapping to be generated.
     */
    private void processClass(ClassDoc cd) {
        ClassDoc superclass = cd.superclass();
        if (superclass != null) {
            if (!add(subclasses, superclass, cd)) {
                return;
            } else {
                processClass(superclass);      // Recurse
            }
        } else {     // cd is java.lang.Object, add it once to the list
            if (!baseclasses.contains(cd)) {
                baseclasses.add(cd);
            }
        }
        ClassDoc[] intfacs = cd.implementedInterfaces();
        for (int i = 0; i < intfacs.length; i++) {
            add(implementingclasses, intfacs[i], cd);
        }
    }

    /**
     * For the interface passed get the interfaces which it extends, and then
     * put this interface in the sub-interface list of those interfaces. Do it
     * recursively. If a interface doesn't have super-interface just attach
     * that interface in the list of all the baseinterfaces.
     *
     * @param cd Interface under consideration.
     */
    private void processInterface(ClassDoc cd) {
        ClassDoc[] intfacs = cd.implementedInterfaces();
        if (intfacs.length > 0) {
            for (int i = 0; i < intfacs.length; i++) {
                if (!add(subinterfaces, intfacs[i], cd)) {
                    return;
                } else {
                    processInterface(intfacs[i]);   // Recurse
                }
            }
        } else {
            // we need to add all the interfaces who do not have
            // super-interfaces to baseinterfaces list to traverse them
            if (!baseinterfaces.contains(cd)) {
                baseinterfaces.add(cd);
            }
        }
    }

    /**
     * Adjust the Class Tree. Add the class interface  in to it's super-class'
     * or super-interface's sub-interface list.
     *
     * @param map the entire map.
     * @param superclass java.lang.Object or the super-interface.
     * @param cd sub-interface to be mapped.
     * @returns boolean true if class added, false if class already processed.
     */
    private boolean add(Map map, ClassDoc superclass, ClassDoc cd) {
        List list = (List)map.get(superclass);
        if (list == null) {
            list = new ArrayList();
            map.put(superclass, list);
        }
        if (list.contains(cd)) {
            return false;
        } else {
            list.add(cd);
        }
        return true;
    }

    /**
     * From the map return the list of sub-classes or sub-interfaces. If list
     * is null create a new one and return it.
     *
     * @param map The entire map.
     * @param cd class for which the sub-class list is requested.
     * @returns List Sub-Class list for the class passed.
     */
    private List get(Map map, ClassDoc cd) {
        List list = (List)map.get(cd);
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    /**
     *  Return the sub-class list for the class passed.
     */
    public List subclasses(ClassDoc cd) {
        return get(subclasses, cd);
    }

    /**
     *  Return the sub-interface list for the interface passed.
     */
    public List subinterfaces(ClassDoc cd) {
        return get(subinterfaces, cd);
    }

    /**
     *  Return the list of classes which implement the interface passed.
     */
    public List implementingclasses(ClassDoc cd) {
        return get(implementingclasses, cd);
    }

    /**
     *  Return the sub-class/interface list for the class/interface passed.
     */
    public List subs(ClassDoc cd) {
        return get(cd.isInterface()? subinterfaces: subclasses, cd);
    }

    /**
     *  Return the base-classes list. This will have only one element namely
     *  classdoc for java.lang.Object, since this is the base class for all the
     *  classes.
     */
    public List baseclasses() {
        return baseclasses;
    }

    /**
     *  Return the list for the base interfaces. This is the list of interfaces
     *  which do not have super-interface.
     */
    public List baseinterfaces() {
        return baseinterfaces;
    }
}
