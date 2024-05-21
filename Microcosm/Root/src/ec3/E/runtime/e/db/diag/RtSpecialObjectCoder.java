package ec.e.db;

import ec.util.EThreadGroup;

import java.lang.Class;
import java.lang.Object;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import ec.util.NestedException;

public interface RtSpecialObjectCoder {
    void encodeSpecialObject (Object object, Class theClass, Object arg,
                              RtStandardEncoder stream);
    Object decodeSpecialObject (Class theClass, Object arg, RtDecoder stream,
                                int index);
}

/** RtSpecialCoder handles certain classes and data types, namely
  String, Class, and ObjectArray, that cannot be encoded the normal
  way since they are used in the encoding process. Like all other
  classes, objects of these three classes are represented by their
  index in the typetable, but these three indices are known
  beforehand, by construction, since this coder encodes these three
  first.  */

final class RtSpecialCoder implements RtSpecialObjectCoder {
    private static Trace tr = new Trace("ec.e.db.RtSpecialCoder");

    static final int kStringClass = 1;
    static final int kClassClass = 2;
    static final int kObjectArrayClass = 3;
    static final int kObjectHashtableClass = 4;
    static final int kObjectVectorClass = 5;
    static final int kObjectThrowableClass = 6;
    static final int kObjectPropertiesClass = 7;
    private static boolean initialized = false;

    static RtSpecialCoder theSpecialCoder = new RtSpecialCoder();
    private RtSpecialCoder () {
    }

    /* This method is used, rather than the standard static initializer to
       avoid bootstrapping issues with the rest of the decoding system. */
    static void initialize () {
        if (initialized == true)
            return;
        initialized = true;
        Object[] objs = new Object[1];

        try {
            RtCodingSystem.registerSpecialCodingClass(String.class, theSpecialCoder,
                                                      new Integer(kStringClass));
            RtCodingSystem.registerSpecialCodingClass(Class.class, theSpecialCoder,
                                                      new Integer(kClassClass));
            RtCodingSystem.registerSpecialCodingClass(new Object[0].getClass(), theSpecialCoder,
                                                      new Integer(kObjectArrayClass));
            RtCodingSystem.registerSpecialCodingClass(Hashtable.class, theSpecialCoder,
                                                      new Integer(kObjectHashtableClass));
            RtCodingSystem.registerSpecialCodingClass(Vector.class, theSpecialCoder,
                                                      new Integer(kObjectVectorClass));
            RtCodingSystem.registerSpecialCodingClass(Proprties.class, theSpecialCoder,
                                                      new Integer(kObjectPropertiesClass));
        } catch (Exception e) {
            System.out.println(
                               "SpecialCoder couldn't get class(es) using forName");
        }
    }

    public void encodeSpecialObject (Object object, Class theClass,
                                     Object arg, RtStandardEncoder stream) {
        int i;
        int size;
        long startPosition = stream.getPosition(); // COMMSPAM

        int type = ((Integer)arg).intValue();
        switch (type) {
        case kClassClass:
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode Class " + object);
            }
            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Class: " + object.toString());
            }
            try {
                /* XXX (GJF) - Need to hook in the class hash as well when we
                   start considering that over the net ... */
                stream.writeUTF(((Class)object).getName());
            } catch (IOException e) {
                throw new NestedException("Couldn't writeUTF for Class name", e);
            }
            break;

        case kStringClass:
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode String " + object);
            }

            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                String s = ((String)object).replace('\n', '#');
                stream.commStack.push(">\"" + s + "\"");
            }

            try {
                stream.writeUTF((String)object);
            } catch (IOException e) {
                throw new NestedException("Couldn't writeUTF for String", e);
            }
            break;

        case kObjectArrayClass:
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode Object Array of size " + ((Object[])object).length);
            }

            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Array [" +
                           ((Object[])object).length + "]");
            }

            try {
                Object[] array = (Object[])object;
                stream.writeInt(array.length);
                for (i = 0; i < array.length; i++) {
                    stream.encodeObject(array[i]);
                }
            } catch (Exception e) {
                throw new NestedException("Couldn't write array", e);
            }
            break;

        case kObjectHashtableClass:
            Hashtable table = (Hashtable)object;
            Enumeration elements = table.elements();
            Enumeration keys = table.keys();
            size = table.size();
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode Hashtable of size " +
                                       size);
            }
            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Hashtable [" + size + "]");
            }
            try {
                stream.writeInt(size);
                while (elements.hasMoreElements()) {
                    stream.encodeObject(keys.nextElement());
                    stream.encodeObject(elements.nextElement());
                }
            } catch (IOException e) {
                throw new NestedException("Couldn't write hashtable", e);
            }
            break;

        case kObjectPropertiesClass:
            Properties properties = (Properties)object;
            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Properties");
            }

            try {
                Enumeration names = properties.propertyNames();
                Vector nameVector = new Vector();
                while (names.hasMoreElements()) {
                    nameVector.addElement(names.nextElement());
                }
                size = nameVector.size();
                stream.writeInt(size);
                for (i = 0; i < size; i++) {
                    String key = (String)nameVector.elementAt(i);
                    stream.writeUTF(key);
                    stream.writeUTF(properties.getProperty(key));
                }
            } catch (IOException e) {
                throw new NestedException("Couldn't save properties", e);
            }
            break;

        case kObjectVectorClass:
            Vector vector = (Vector)object;
            size = vector.size();
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode Vector of size " +
                                       size);
            }
            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Vector [" + size + "]");
            }

            try {
                stream.writeInt(size);
                for (i = 0; i < size; i++) {
                    stream.encodeObject(vector.elementAt(i));
                }
            } catch (IOException e) {
                throw new NestedException("Couldn't write Vector", e);
            }
            break;

        case kObjectThrowableClass:
            if (tr.debug && Trace.ON) {
                tr.$("Called to encode a Throwable");
            }

            if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
                stream.commStack.push("Throwable");
            }

            try {
                // verify that there's enough constructability to decode
                // it on the other side
                {
                    Class[] argTypes = { "".getClass() };
                    Constructor con = null;
                    try {
                        con = theClass.getConstructor(argTypes);
                    } catch (NoSuchMethodError e) {
                        // fall through
                    } catch (Exception e) {
                        // fall through
                    }
                    if (con == null) {
                        Class[] argTypes0 = new Class[0];
                        try {
                            con = theClass.getConstructor(argTypes0);
                        } catch (NoSuchMethodError e) {
                            // fall through
                        } catch (Exception e) {
                            // fall through
                        }
                        if (con == null) {
                            tr.errorm("Cannot encode throwable " + theClass +
                                " which has neither a public string-arg " +
                                "constructor nor a public no-arg " +
                                "constructor.");
                            throw new IOException("Unencodable throwable " +
                                theClass);
                        }
                        tr.warningm("Encoding throwable " + theClass +
                            " lacking a public string-arg constructor");
                    }
                }
                stream.encodeObject(theClass);
                Throwable t = (Throwable) object;
                // ByteArrayOutputStream automatically grows, so no bounds problem -emm
                ByteArrayOutputStream bs = new ByteArrayOutputStream(1000);
                PrintStream ps = new PrintStream(bs);
                ps.println("Remote exception:");
                EThreadGroup.printStackTrace(t, ps);
                stream.writeUTF(bs.toString());
                if (t instanceof RtCodeableThrowable) {
                    ((RtCodeableThrowable)t).encodeThrowableState(stream);
                }
            } catch (IOException e) {
                throw new NestedException("Couldn't write Throwable", e);
            }
            break;

        default:
            if (tr.error) tr.$("Error, encodeSpecialObject of unknown type: " + type);
        }
        if (stream.isSpamming && stream.trSpam.debug && Trace.ON) {
            stream.commBytes((stream.getPosition() - startPosition));
        }
    }

    public Object decodeSpecialObject (Class theClass, Object arg,
                                       RtDecoder stream, int index) {
        int i;
        int size;
        String string = null;
        int type = ((Integer)arg).intValue();
        switch (type) {
        case kClassClass:
            Class c = null;
            if (tr.debug && Trace.ON)
                tr.$("Called to decode Class");
            try {
                string = stream.readUTF();
                if (tr.debug && Trace.ON)
                    tr.$("Decoded Class name " + string);
                /* XXX (GJF) - Might need to put hook in the decoder stream to
                   find the class, so it can get it from the decoding manager,
                   which might have stashed the class (i.e.)
                   RemoteClassManager cache for remotely loaded classes that
                   class.forName() won't find. */
                c = ClassCache.forName(string);
            } catch (Exception e) {
                if (tr.error) {
                    if (string != null)
                        throw new NestedException("Error finding class " + string, e);
                    else
                        throw new NestedException("Error decoding class name", e);
                }
            }
            if (tr.debug && Trace.ON)
                tr.$("Decoded Class " + c);
            return(c);

        case kStringClass:
            if (tr.debug && Trace.ON)
                tr.$("Called to decode String");
            try {
                /* boolean isInterned = stream.readBoolean(); */
                string = stream.readUTF();
                /* if (isInterned) string = string.intern(); */
            } catch (IOException e) {
                tr.errorReportException(e, "Couldn't read String");
                string = "";
            }
            if (tr.debug && Trace.ON)
                tr.$("Decoded String " + string);
            return(string);

        case kObjectArrayClass:
            if (tr.debug && Trace.ON)
                tr.$("Called to decode Object Array");
            Object[] array = null;
            try {
                size = stream.readInt();
                array = new Object[size];
                if (tr.debug && Trace.ON) tr.$("Will insert object " + array.getClass() + " with index " + index);
                stream.insertObjectInTable(index, array);
                for (i = 0; i < size; i++) {
                    array[i] = stream.decodeObject();
                }
            } catch (IOException e) {
                tr.errorReportException(e, "Couldn't read Object Array");
                array = new Object[0];
            }
            return(array);

        case kObjectHashtableClass:
            Hashtable table = null;
            if (tr.debug && Trace.ON)
                tr.$("Called to decode Hashtable");
            try {
                size = stream.readInt();
                if (size > 0) {
                    table = new Hashtable(size);
                    for (i = 0; i < size; i++) {
                        Object key = stream.decodeObject();
                        Object value = stream.decodeObject();
                        // Since the encode of key or value
                        // could have stuck nul into the stream,
                        // we avoid blowing up in that case
                        if ((key != null) && (value != null))  {
                            table.put(key, value);
                        }
                    }
                } else {
                    table = new Hashtable();
                }
            } catch (IOException e) {
                tr.errorReportException(e, "Couldn't read hashtable");
                table = new Hashtable();
            }
            return(table);

        case kObjectPropertiesClass:
            Properties properties = new Properties();
            try {
                size = stream.readInt();
                for (i = 0; i < size; i++) {
                    properties.put(stream.readUTF(), stream.readUTF());
                }
            } catch (IOException e) {
                tr.errorReportException(e, "Couldn't load Properties");
            }
            return(properties);

        case kObjectVectorClass:
            Vector vector = null;
            if (tr.debug && Trace.ON)
                tr.$("Called to decode Vector");
            try {
                size = stream.readInt();
                if (size > 0) {
                    vector = new Vector(size);
                    for (i = 0; i < size; i++) {
                        vector.addElement(stream.decodeObject());
                    }
                } else {
                    vector = new Vector();
                }
            } catch (IOException e) {
                tr.errorReportException(e, "Couldn't read Vector");
                vector = new Vector();
            }
            return(vector);

        case kObjectThrowableClass:
            if (tr.debug && Trace.ON) {
                tr.$("Called to decode a Throwable");
            }
            try {
                Class cl = (Class) stream.decodeObject();
                String detail = stream.readUTF();
                Object result;
                try {
                    Class[] argTypes = new Class[1];
                    argTypes[0] = detail.getClass(); // cheesy
                    Constructor con;
                    try {
                        con = cl.getConstructor(argTypes);
                    } catch (NoSuchMethodError e) {
                        // translate into exception for lower clause
                        throw new NoSuchMethodException();
                    }
                    Object[] args = new Object[1];
                    args[0] = detail;
                    result = con.newInstance(args);
                } catch (Exception e) {
                    tr.debugReportException(e, "failed to construct Throwable with detail message: " + detail);
                    try {
                        result = cl.newInstance();
                    } catch (NoSuchMethodError e2) {
                        tr.debugReportException(e2, "encoded Throwables at least need a public no-arg constructor");
                        // translate into exception for lower claus
                        throw new NoSuchMethodException();
                    }
                }
                if (result instanceof RtCodeableThrowable) {
                    ((RtCodeableThrowable)result).decodeThrowableState(stream);
                }
                return result;
            } catch (Exception e) {
                tr.errorReportException(e, "Couldn't read Throwable");
            }
            break;

        default:
            if (tr.error) tr.$(
                                   "Error, decodeSpecialObject called to decode unknown type: " +
                                   type);
        }
        return(null);
    }
}


