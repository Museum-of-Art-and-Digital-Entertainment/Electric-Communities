
package ec.tests.util;

import java.lang.*;
import java.util.*;

import ec.util.*;

class ObjKeyHashTest
{
    final static int howMany = 15;
    final static int noSuchKey = -13;
    static ObjKeyTable table = new ObjKeyTable(noSuchKey, howMany/2);
    static Integer[howMany] ints = new Integer[howMany];
    static Double[howMany] dubs = new Double[howMany];
    static Object dobj;
    static int dummy;
    static boolean inThere = false;
    static Enumeration elems;

    public static void main (String args[]) {
	System.out.println("ObjKeyHashTest test...");
	for (int i = 0; i < howMany; i++) {
	    ints[i] = new Integer(howMany-i);
	    dubs[i] = new Double(2.0*(i)-(0.15*i));
	    dummy = table.put(ints[i], 2*i);
	    dummy = table.put(dubs[i], 2*i+1);
	    if (dummy != noSuchKey)
		System.out.println("Didn't put #" + i + "!!!!");
	}
	System.out.println("  Final table has " + table.size() + " entries.");
	dummy = table.remove(dubs[howMany/2]);
	if (dummy == noSuchKey)
	    System.out.println("Didn't remove #" + howMany/2 + "!!!!");
	else
	    System.out.println("Removed " + dummy + "!!!!");
	System.out.println("  Removed table has " + table.size() + " entries.");
	for (int i = 0; i < howMany; i++) {
	    dummy = table.get(ints[i]);
	    if (dummy == noSuchKey)
		System.out.println("Didn't get #" + i + "!!!!");
	    inThere = table.contains(i);
	    if (!inThere)
		System.out.println("Object (" + ints[i] + ") not in there!!!!");
	    inThere = table.containsKey(ints[i]);
	    if (!inThere)
		System.out.println("Object #" + i + " not in there!!!!");
	}
	if (!table.isEmpty())
	    System.out.println("Table is not empty.");
	dummy = table.remove(dubs[howMany/2]);
	if (dummy == noSuchKey)
	    System.out.println("Didn't remove #" + howMany/2 + "!!!!");
	else
	    System.out.println("Removed " + dummy + "!!!!");
	elems = table.elements();
	System.out.println("  Table contains (in reverse):");
	try {
	    while ((dobj = elems.nextElement()) != null) {
		System.out.print("  " + dobj);
	    }
	} catch (NoSuchElementException e) {
	    System.out.println("");
	}
	System.out.println("  Table is " + table.toString() + ".");
	table.clear();
	System.out.println("  Cleared table has " + table.size() + " entries.");
	if (table.isEmpty())
	    System.out.println("Table is empty.");
    }
}

