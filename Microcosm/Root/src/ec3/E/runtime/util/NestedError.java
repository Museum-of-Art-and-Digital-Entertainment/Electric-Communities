package ec.util;

import java.io.IOException;
import java.util.Vector;

public class NestedError extends Error implements NestedThrowable
{
    private Throwable myContainedThrowable;

    public NestedError(String msg) {
        super(msg);
    }
    
    public NestedError(String msg, Throwable t) {
        super(msg);
        myContainedThrowable = t;
    }

    public Throwable getNestedThrowable() {
        return myContainedThrowable;
    }

    public void encodeThrowableState(RtEncoder stream) throws IOException {
        stream.encodeObject(myContainedThrowable);
    }

    public void decodeThrowableState(RtDecoder stream) throws IOException {
        myContainedThrowable = (Throwable) stream.decodeObject();
    }
}

public class NestedException extends RuntimeException implements NestedThrowable
{
    private Throwable myContainedThrowable;
    
    public NestedException(String msg) {
        super(msg);
    }

    public NestedException(String msg, Throwable t) {
        super(msg);
        myContainedThrowable = t;
    }

    public Throwable getNestedThrowable() {
        return myContainedThrowable;
    }

    public void encodeThrowableState(RtEncoder stream) throws IOException {
        stream.encodeObject(myContainedThrowable);
    }

    public void decodeThrowableState(RtDecoder stream) throws IOException {
        myContainedThrowable = (Throwable) stream.decodeObject();
    }
}

public class NestedIOException extends IOException implements NestedThrowable
{
    private Throwable myContainedThrowable;
    
    public NestedIOException(String msg) {
        super(msg);
    }

    public NestedIOException(String msg, Throwable t) {
        super(msg);
        myContainedThrowable = t;
    }

    public Throwable getNestedThrowable() {
        return myContainedThrowable;
    }

    public void encodeThrowableState(RtEncoder stream) throws IOException {
        stream.encodeObject(myContainedThrowable);
    }

    public void decodeThrowableState(RtDecoder stream) throws IOException {
        myContainedThrowable = (Throwable) stream.decodeObject();
    }
}


public class NestedExceptionVector extends Exception implements NestedThrowableVector
{
    private Vector myLabels = new Vector();
    private Vector myThrowables = new Vector();
    
    public NestedExceptionVector(String msg) {
        super(msg);
    }

    public void addThrowable(String label, Throwable t) {
        myLabels.addElement(label);
        myThrowables.addElement(t);
    }
    
    public int size() {
        if (myLabels == null) {
            return 0;
        }
        return myLabels.size();
    }
    
    public String getLabel(int i) {
        return (String)myLabels.elementAt(i);
    }
    
    public Throwable getThrowable(int i) {
        return (Throwable)myThrowables.elementAt(i);
    }

    public String toString() {
        String ret = super.toString();
        for (int i=0; i<myLabels.size(); i++) {
            ret += myLabels.elementAt(i) + ": " + myThrowables.elementAt(i) + "\n" ;
        }
        return ret;
    }

    public void encodeThrowableState(RtEncoder stream) throws IOException {
        stream.encodeObject(myLabels);
        stream.encodeObject(myThrowables);
    }

    public void decodeThrowableState(RtDecoder stream) throws IOException {
        myLabels = (Vector) stream.decodeObject();
        myThrowables = (Vector) stream.decodeObject();
    }
}
