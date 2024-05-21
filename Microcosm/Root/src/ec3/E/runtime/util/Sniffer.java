package ec.util;

import java.util.*;

public class Sniffer {
    /*
      referencers[0] = thread stack references
      referencers[1] = frame references
      referencers[2] = class references
      referencers[3] = object references
    */
    Vector referencers[];
    Vector objectsBeingSkipped = null;
    Hashtable scannedObjects;

    public Sniffer() {
        referencers = new Vector[4];
    }

    public native void sniffReferencers(Hashtable scannedObjects,
                                        Vector referencers[], Object sniff);

    static {
        System.loadLibrary("util");
    }

    public Vector getObjectsToIgnore() {
        return objectsBeingSkipped;
    }

    public void setObjectsToIgnore(Vector objects) {
        objectsBeingSkipped = objects;
    }

    public Vector[] scanForReferencers(Object objectToScanFor) {
        if (scannedObjects != null)
            scannedObjects.clear();
        for (int i = 0; i < referencers.length; i++) {
            if (referencers[i] != null) {
                referencers[i].removeAllElements();
            }
        }
        referencers[0] = new Vector(5,5);
        referencers[1] = new Vector(5,5);
        referencers[2] = new Vector(5,5);
        referencers[3] = new Vector(5,5);

        scannedObjects = new Hashtable();

        System.gc();

        /* Stick the hashtable and vectors into scannedObjects so that we don't
           bother searching through any of them. */
        scannedObjects.put(referencers, referencers);
        scannedObjects.put(referencers[0], referencers[0]);
        scannedObjects.put(referencers[1], referencers[1]);
        scannedObjects.put(referencers[2], referencers[2]);
        scannedObjects.put(referencers[3], referencers[3]);
        scannedObjects.put(scannedObjects, scannedObjects);

        if (objectsBeingSkipped != null) {
            int total = objectsBeingSkipped.size();
            for (int i = 0; i < total; i++) {
                Object obj = objectsBeingSkipped.elementAt(i);
                scannedObjects.put(obj, obj);
            }
            scannedObjects.put(objectsBeingSkipped, objectsBeingSkipped);
        }

        scannedObjects.put(this, this);

        /* Call our native code method which will call into the gc system */
        sniffReferencers(scannedObjects, referencers, objectToScanFor);
        return referencers;
    }

    public Vector getObjectReferencers() {
        int total = referencers[3].size();
        Vector objectReferenceVector = new Vector(total/2, total/2);
        for (int count = 0; count < total; count+=2) {
            objectReferenceVector.
                addElement(referencers[3].elementAt(count+1));
        }
        return objectReferenceVector;
    }

    public void printResults() {
        if ((referencers[0] != null) && (referencers[0].size() > 0)) {
            System.out.println("Thread stack referencers are: ");
            printVector(referencers[0]);
        }
        if ((referencers[1] != null) && (referencers[1].size() > 0)) {
            System.out.println("Frame referencers are: ");
            printVector(referencers[1]);
        }
        if ((referencers[2] != null) && (referencers[2].size() > 0)) {
            System.out.println("Class referencers are: ");
            printVector(referencers[2]);
        }
        if ((referencers[3] != null) && (referencers[3].size() > 0)) {
            int total = referencers[3].size();
            System.out.println("Object referencers (" + (total/2) + ") are: ");
            printObjectVector(referencers[3], total);
        }
    }

    void printVector(Vector vector) {
        int total = vector.size();

        for (int count = 0; count < total; count++) {
            System.out.println("\t" + vector.elementAt(count).toString());
        }
    }

    void printObjectVector(Vector vector, int total) {
        for (int count = 0; count < total; count+=2) {
            System.out.print("\t" + vector.elementAt(count).toString());
            System.out.println(vector.elementAt(count+1).toString());
        }
    }
}
