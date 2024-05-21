
package ec.tests.util;

import java.lang.*;
import java.util.*;

import ec.util.*;

class IntKeyHashTest
{
    final static int howMany = 15;
    static IntKeyTable table = new IntKeyTable(howMany/2);
    static Integer[howMany] ints = new Integer[howMany];
    static Double[howMany] dubs = new Double[howMany];
    static Object dummy = new Object();
    static boolean inThere = false;
    static Enumeration elems;

    public static void main (String args[]) {
	System.out.println("IntKeyHashTest test...");
	for (int i = 0; i < howMany; i++) {
	    ints[i] = new Integer(howMany-i);
	    dubs[i] = new Double(2.0*(i)-(0.15*i));
	    dummy = table.put(2*i, ints[i]);
	    dummy = table.put(2*i+1, dubs[i]);
	    if (dummy != null)
		System.out.println("Didn't put #" + i + "!!!!");
	}
	System.out.println("  Final table has " + table.size() + " entries.");
	dummy = table.remove(howMany/2);
	if (dummy == null)
	    System.out.println("Didn't remove #" + howMany/2 + "!!!!");
	else
	    System.out.println("Removed " + dummy + "!!!!");
	System.out.println("  Removed table has " + table.size() + " entries.");
	for (int i = 0; i < howMany; i++) {
	    dummy = table.get(i);
	    if (dummy == null)
		System.out.println("Didn't get #" + i + "!!!!");
	    inThere = table.contains(ints[i]);
	    if (!inThere)
		System.out.println("Object (" + ints[i] + ") not in there!!!!");
	    inThere = table.containsKey(i);
	    if (!inThere)
		System.out.println("Object #" + i + " not in there!!!!");
	}
	if (!table.isEmpty())
	    System.out.println("Table is not empty.");
	dummy = table.remove(howMany/2);
	if (dummy == null)
	    System.out.println("Didn't remove #" + howMany/2 + "!!!!");
	else
	    System.out.println("Removed " + dummy + "!!!!");
	elems = table.elements();
	System.out.println("  Table contains (in reverse):");
	try {
	    while ((dummy = elems.nextElement()) != null) {
		System.out.print("  " + dummy);
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

