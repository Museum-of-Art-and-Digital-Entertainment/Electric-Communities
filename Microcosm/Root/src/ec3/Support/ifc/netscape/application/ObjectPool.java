// ObjectPool.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.


package netscape.application;

class ObjectPool {
    Object freePool[];
    int    freePoolMaxLength;
    int    freePoolNextSlot;
    Class  objectClass;
    int    allocSaved;
    int    allocDone;
    int    maxCapacity;

    public ObjectPool(String className) {
        this(className,32);
    }

    public ObjectPool(String className,int aMaxCapacity) {
        super();
        freePool = new Object[1];
        freePoolMaxLength = 1;
        freePoolNextSlot  = 0;
        try {
            objectClass = Class.forName(className);
        } catch( ClassNotFoundException e ) {
            System.out.println("ObjectPool cannot find class " + className);
        }
        allocSaved=0;
        allocDone = 0;
        maxCapacity = aMaxCapacity;
    }

    public Object allocateObject() {
        Object result = null;

        synchronized( this ) {
            if( freePoolNextSlot > 0 ) {
                freePoolNextSlot--;
                result = freePool[freePoolNextSlot];
            }
        }

        if( result == null ) {
            allocDone++;
            try {
                result = objectClass.newInstance();
            } catch( InstantiationException e) {
                System.out.println("Cannot instantiate instance of class " + objectClass);
            } catch( IllegalAccessException e ) {
                System.out.println("Cannot instantiate instance of class. Illegal." + objectClass);
            }
        } else
            allocSaved++;
        return result;
    }

    public void recycleObject(Object anObject) {
        synchronized( this ) {
            if( freePoolMaxLength < maxCapacity ) {
                if( freePoolNextSlot == freePoolMaxLength ) {
                    Object newFreePool[] = new Object[freePoolMaxLength * 2];
                    System.arraycopy(freePool,0,newFreePool,0, freePoolMaxLength);
                    freePool = newFreePool;
                    freePoolMaxLength *= 2;
                }
                freePool[freePoolNextSlot++] = anObject;
            }
        }
    }

    protected void finalize() {
        int i;
        for(i=0 ; i < freePoolNextSlot ; i++)
            freePool[i] = null;
        freePool=null;
    }

    public String toString() {
        return "Object pool for class " + objectClass + " has " + freePoolNextSlot + " instances." +
            " " + allocSaved + " allocations avoided allocation performed:" + allocDone;
    }
}
