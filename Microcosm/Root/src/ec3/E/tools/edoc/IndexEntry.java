/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */

package ec.edoc;
 
/** This class reprasents an entry into the index files */
class IndexEntry {
  
    /* This is chosen not to clash with the ClassMemberInfos types, which
     * form the non-null tag values. */
    private final static int NULL_TAG = 0;
    private final static int CLASS_INTERFACE = 1;
    private final static int FIELD = 2;
    private final static int METHOD = 3;
    private final static int CONSTRUCTOR = 4;
  
    private int myTag;
    private int myMods;
    private String myParent;
    private String myName;
    private String myType;
    private java.util.Vector myThrows;
 
    /** Accessor method so that we can see if this object is Nil, the
     *  end of list marker. */
    boolean isNil() {
        return (myTag == NULL_TAG);
    }
 
    boolean isClass() {
        return myTag == CLASS_INTERFACE;
    }
    
    boolean isField() {
        return myTag == FIELD;
    }
    
    boolean isConstructor() {
        return myTag == CONSTRUCTOR;
    }
    
    boolean isMethod() {
        return myTag == METHOD;
    }
    
    /** Accessor method so that we can construct index with appropriately 
     *  coloured bullets */
    int mods() {
        return myMods;
    }   
    /** Accessor method so that we can construct html links */
    String parent() {
        return myParent;
    }
    /** Accessor method so that we can construct html links */
    String name() {
        return myName;
    }
    /** Accessor method so that we can construct html links */
    String type() {
        return myType;
    }
 
    /** Builds an index entry from a ClassMember Info */
    IndexEntry(Info i) {

        if (i instanceof ClassInterfaceInfo) {
            construct((ClassInterfaceInfo)i);
            //System.out.println("new cls");
        } else if (i instanceof FieldInfo) {
            construct((FieldInfo)i);
            //System.out.println("new mem");
        } else if (i instanceof MethodInfo) {
            construct((MethodInfo)i);
            //System.out.println("new mem");
        } else {
            myTag = -1;
            /* if we don't catch any of them, then we're dead */
        }
    }
    
    private void construct(ClassInterfaceInfo cii) {
    
        myTag = CLASS_INTERFACE;
        myMods = cii.modifiers();
        myParent = cii.getExtends();
        myName = cii.name();
        myThrows = null;
    }
    
    private void construct(FieldInfo fi) {
    
        myTag = FIELD;
        myMods = fi.modifiers();
        myParent = fi.containingClass().name();
        myName = fi.name();
        myType = fi.internalType();
        myThrows = null;
    }
    
    private void construct(MethodInfo mi) {
    
        if (mi instanceof ConstructorInfo) {
            myTag = CONSTRUCTOR;
        } else {
            myTag = METHOD;
        }
        myMods = mi.modifiers();
        myParent = mi.containingClass().name();
        myName = mi.name();
        myType = mi.internalType();
        myThrows = mi.getThrows();
    }

    /** This is used only to create a Nil object. This can be used to mark 
     *  the end of a file / list. */
    IndexEntry() {
        myTag = NULL_TAG;
            //System.out.println("new nil");
    }
        
    /** Output this index entry so that it can be read later. */
    /** @param ba nullFatal */
    void dump(ByteArray ba) {   
        
        /* Static initialisers are purely background stuff, so we don't need 
         * to index them, so we'll just ignore this. */
        if (myTag == -1) {
            return;
        }
        
        /* if this is Nil, then we only need one byte to explain that */
        if (myTag == NULL_TAG) {
            //System.out.println("out nil");
            ba.addu1(myTag);
            return;
        }
        
        if (myTag == CLASS_INTERFACE) {
            //System.out.println("out cls");
            ba.addu1(myTag);
            ba.addu2(myMods);
            ba.addString(myParent);
            ba.addString(myName);
        } else {
            //System.out.println("out mem");
            int numThrows = (myThrows == null) ? 0 : myThrows.size();
        
            ba.addu1(myTag);
            ba.addu2(numThrows);
            ba.addu2(myMods);
            ba.addString(myParent);
            ba.addString(myName);
            ba.addString(myType);
            for (int i = 0; i < numThrows; i++) {
                ba.addString((String)myThrows.elementAt(i));
            }
        }
    }
        
    /** Builds an index entry from an input stream */
    /** @param d nullFatal trusted
     *  @exception java.io.IOException
     */
    IndexEntry(java.io.DataInputStream d) throws java.io.IOException {
    
        ////System.out.println("Reading indexEntry");
        myTag = d.readUnsignedByte();
        //System.out.println("myTag = " + myTag);
        
        if (myTag == NULL_TAG) {
            //System.out.println("in  nil");
            //System.out.println("  myTag is null. stop");
            return;
        }

        //System.out.println("myTag isn't null ");
        
        if (myTag == CLASS_INTERFACE) {
            //System.out.println("in  cls");
            myMods = d.readUnsignedShort();
            myParent = d.readUTF();
            myName = d.readUTF();
        } else {
            //System.out.println("in  mem");
        
            int numThrows = d.readUnsignedShort();
        
            myMods = d.readUnsignedShort();
            myParent = d.readUTF();
            myName = d.readUTF();
            myType = d.readUTF();
            for (int i = 0; i < numThrows; i++) {
                if (myThrows == null) {
                    myThrows = new java.util.Vector();
                }
                myThrows.addElement(d.readUTF());
            }
        }
    }
        
    /** This method takes a set of IndexEnties and sorts them. */
    /** @param v nullFatal trusted 
     */
    static void sort(java.util.Vector v) {
    
        int comparisons = 0;
    
        int[] H = {15, 7, 3, 1};
    
        int N = v.size();
        for (int s = H.length; s-- > 0; ) {

            int h = H[s];           
            if (h > N/2) {
                continue;
            }
            
            for (int j = h - 1; j < N; j++) {
        
                int i = j - h;
                IndexEntry R = (IndexEntry) v.elementAt(j);
                while (i >= 0) {
                    IndexEntry tmp = (IndexEntry) v.elementAt(i);
                    ////System.out.print('.');System.out.flush();
                    comparisons++;
                    if (R.isLessThan(tmp)) {
                        v.setElementAt(tmp, i+h);
                        i -= h;
                    } else {
                        break;
                    }
                }
                v.setElementAt(R, i+h);
            }
        }
        //System.out.println("Sorted " + v.size() + " with " + comparisons 
        //  + " comparisons.");
    }
    
    
    /** private helper method for sort */
    /* This sorts alphabetically, then by type of member, then by visibility */
    private boolean isLessThan(IndexEntry i) {
        
        /* this test puts Nil, if it occurs, at the end of the array */
        if (myTag == NULL_TAG) {
            return false;
        }
        
        if (myName == null || i.myName == null) {
            System.out.println("Names for compare = "+
                myName + " / " + i.myName);
        }
        int cmp = myName.toLowerCase().compareTo(i.myName.toLowerCase());
        if (cmp < 0) {
            return true;
        } else if (cmp > 0) {
            return false;
        }

        if (myTag < i.myTag) {
            return true;
        } else if (myTag > i.myTag) {
            return false;
        }
        
        /* public(=1) private(=2) and protected(=4) are not in the right
         * order for the coding standards. swap priv / prot round with ^ */
        if ( ((myMods^6)&7) < ((i.myMods^6)&7)) {
            return true;
        } else if ( ((myMods^6)&7) > ((i.myMods^6)&7)) {
            return false;
        }
        
        
        return false;
    }
}