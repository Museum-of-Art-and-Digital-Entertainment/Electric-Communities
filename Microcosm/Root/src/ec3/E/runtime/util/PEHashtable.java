/*
 * Copyright (c) 1993-1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * The Java source code is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.

 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
/*
 * @(#)PEHashtable.java 1.33 96/10/15  
 *
 */

package ec.util;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import ec.tables.SimTable;
import ec.tables.Table;


/**
 * PEHashtable class. Maps keys to values. Any object can be used 
 * as a key and/or value.<p>
 *
 * To sucessfully store and retrieve objects from a hash table, the
 * object used as the key must implement the hashCode() methods.<p>
 *
 * Unlike Hashtable, this uses pointer equality for keys and values.
 *
 */
public class PEHashtable extends SimTable {

    private PEHashtable(Table table) {
        super(table);
    }

    public PEHashtable(int initialCapacity, float loadFactor) {
        super(true, initialCapacity, loadFactor);
    }
    

    /**
     * Constructs a new, empty hashtable with the specified initial 
     * capacity.
     * @param initialCapacity the initial number of buckets
     */
    public PEHashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    
    /**
     * Constructs a new, empty hashtable. A default capacity and load factor
     * is used. Note that the hashtable will automatically grow when it gets
     * full.
     */
    public PEHashtable() {
        this(101, 0.75f);
    }
    
    
    /**
     * Creates a clone of the hashtable. A shallow copy is made,
     * the keys and elements themselves are NOT cloned. This is a
     * relatively expensive operation.
     */
    public synchronized Object clone() {
        return new PEHashtable((Table)myTable.clone());
    }
}
