// Codable.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** The Codable interface declares the methods that permit objects of public
  * classes to be archived and restored. If you are writing an application
  * that simply stores and retrieves objects of a codable class (that is,
  * the class implements the <b>Codable</b> interface), you don't need to
  * know the details of the <b>Codable</b> interface. Rather, you use the
  * methods provided by the <b>Archiver</b> and <b>Unarchiver</b> classes to
  * store and retrieve codable objects. <P>
  * However, if you are providing a class and you want applications to be
  * able to archive and restore objects of that class, you must implement
  * the <b>Codable</b> interface in that class. The methods of the
  * <b>Codable</b> interface, called by <b>Archiver</b> and
  * <b>Unarchiver</b> objects, enable an object to describe its essential
  * characteristics, and encode and decode the state of those
  * characteristics. </P>
  * <P>When an <b>Archiver</b> object calls the codable object's
  * describeClassInfo() method, the codable object provides its class name
  * and version and a set of key/type pairs. The key is a string defined by
  * the codable object's class that labels a characteristic of object of
  * that class. The type is a constant, defined by the <b>Codable</b>
  * interface, that identifies a codable data type.</P>
  * <P>When an <b>Archiver</b> object calls the codable object's encode()
  * method, the codable object describes its essential state by providing
  * a set of key/value pairs. The key identifies a characteristic previously
  * defined through <b>describeClassInfo()</b>. The value is the current
  * value of the characteristic. Encoding an object refers to reporting its
  * current key/value pairs.</P>
  * <P>When an object is to be restored from an archive, IFC calls the
  * object's empty constructor and then calls its <b>decode()</b> method to
  * initialize it with the values of the object's characteristics saved in the
  * archive. <i><b>Note:</b> that classes that support the <b>Codable</b>
  * interface must provide a public empty constructor in order for decoding to
  * work properly.</i> </P>
  * <P>The <b>View</b> class is codable. If you subclass <b>View</b>, you
  * need to implement the <b>Codable</b> interface to save what is unique about
  * your class. </P>
  * <P>The following sample shows how a custom subclass of <b>View</b> might
  * encode itself. </P>
  *
  * <pre>
  * public class MyView extends View {
  *
  *         Codable someObject;
  *
  *         public MyView() {
  *         }
  *
  *         public void describeClassInfo(ClassInfo info) {
  *                         super.describeClassInfo(info);
  *                         info.addClass("MyView", 1);
  *                         info.addField("someObject", OBJECT_TYPE);
  *         }
  *
  *         public void encode(Encoder encoder) throws CodingException {
  *                         super.encode(encoder);
  *                         encoder.encodeObject("someObject", someObject);
  *         }
  *
  *         public void decode(Decoder decoder) throws CodingException {
  *                         super.decode(decoder);
  *                         someObject = decoder.decodeObject("someObject");
  *         }
  *
  *         public void finishDecoding() throws CodingException {
  *                         super.finishDecoding();
  *         }
  * }
  * </pre>
  * @see Encoder
  * @see Decoder
  * @see Archiver
  * @see Unarchiver
  * @see ClassInfo
  */
public interface Codable {
    /** The codable data type constant specifying data of type boolean.
      */
    public static final byte BOOLEAN_TYPE       =  0;
    /** The codable data type constant specifying an array of data of type boolean.
      */
    public static final byte BOOLEAN_ARRAY_TYPE =  1;
    /** Primitive Codable type.
      */
    public static final byte CHAR_TYPE          =  2;
    /** Primitive Codable type.
      */
    public static final byte CHAR_ARRAY_TYPE    =  3;
    /** The codable data type constant specifying data of type byte.
      */
    public static final byte BYTE_TYPE          =  4;
    /** The codable data type constant specifying an array of data of type byte.
      */
    public static final byte BYTE_ARRAY_TYPE    =  5;
    /** The codable data type constant specifying data of type short.
      */
    public static final byte SHORT_TYPE         =  6;
    /** The codable data type constant specifying an array of data of type short.
      */
    public static final byte SHORT_ARRAY_TYPE   =  7;
    /** The codable data type constant specifying data of type int.
      */
    public static final byte INT_TYPE           =  8;
    /** The codable data type constant specifying an array of data of type int.
      */
    public static final byte INT_ARRAY_TYPE     =  9;
    /** The codable data type constant specifying data of type long.
      */
    public static final byte LONG_TYPE          = 10;
    /** The codable data type constant specifying an array of data of type long.
      */
    public static final byte LONG_ARRAY_TYPE    = 11;
    /** The codable data type constant specifying data of type float.
      */
    public static final byte FLOAT_TYPE         = 12;
    /** The codable data type constant specifying an array of data of type float.
      */
    public static final byte FLOAT_ARRAY_TYPE   = 13;
    /** The codable data type constant specifying data of type double.
      */
    public static final byte DOUBLE_TYPE        = 14;
    /** The codable data type constant specifying an array of data of type double.
      */
    public static final byte DOUBLE_ARRAY_TYPE  = 15;
    /** The codable data type constant specifying data of type string.
      */
    public static final byte STRING_TYPE        = 16;
    /** The codable data type constant specifying an array of data of type string.
      */
    public static final byte STRING_ARRAY_TYPE  = 17;
    /** The codable data type constant specifying object data.
      */
    public static final byte OBJECT_TYPE        = 18;
    /** The codable data type constant specifying an array of objects.
      */
    public static final byte OBJECT_ARRAY_TYPE  = 19;


    /** Defines the key/type pairs that describe the essential characteristics
      * of a class. Typically, this method is called by an IFC <b>Archiver</b>
      * object, not an application. <P>Each class that can be archived has a
      * schema that defines the critical information to be saved for a given
      * instance of that class. This method provides the field names and
      * <b>Codable</b> data types that define the information to be archived for
      * object of a given classs, along with the class name and class version.
      * The field names frequently correspond to the instance variables in the
      * class.</P>
      * <P>If you are providing a new class that you want to be archivable, you
      * implement <b>describeClassInfo()</b> to describe the key characteristics
      * of your class. If your class is a subclass of a <b>Codable</b> class,
      * first call super.describeClassInfo() to allow the superclass to add its
      * class name, class version, and key/type pairs. Then call info.addClass()
      * and info.addField() to add the key/type pairs that describe your class.
      * </P>
      * <P>Note that because subclasses of your class can call this method, it
      * might be invoked multiple times. As a result, <b>describeClassInfo()</b>
      * should not contain any code that should not be executed more than once.</P>
      * <P>See the sample code in the class description.</P>
      * @param info  A ClassInfo object that describes the schema to encode
      * the class. The called object populates the instance with the information
      * for its class.
      * @see ClassInfo
      * @see Archiver
      */
    public void describeClassInfo(ClassInfo info);

    /** Encodes the essential state of an object. This method is called by an
      * <b>Archiver</b> object to obtain the key/value pair information that
      * defines the called object's current state.
      * <P>An object of a class that implements this method calls the
      * appropriate Encoder methods to provide key/value pair information for
      * the characteristics defined by its <b>describeClassInfo()</b> method.
      * </P><P>The order in which an object encodes its key/value pairs is not
      * important. However, it is more efficient to encode them in the same
      * order that the keys are added in <b>describeClassInfo()</b>. </P>
      * <P>See the class description for information about key/value pairs.
      * @param encoder  An Encoder object that stores state information for an
      * object to be archived. The called object populates the instance with
      * information about its state.
      * @exception CodingException  Occurs when the object cannot be encoded
      * completely.
      * @see Encoder
      * @see Archiver
      */
    public void encode(Encoder encoder) throws CodingException;

    /** Restores an archived object's essential state. Typically, an
      * <b>Archiver</b> object calls this method immediately after calling the
      * archived object's empty constructor. The new object then restores
      * the saved state by calling the appropriate Decoder methods.
      * <P>When implementing this method, you can restore your object's key/value
      * pairs in any order. However, it is most efficient to decode them in
      * the same order that you add the keys in the <b>describeClassInfo()</b>
      * method. </P>
      * @param decoder  A Decoder object containing the saved state
      * information for the object to be restored.
      * @exception CodingException  Occurs when the object cannot be decoded
      * completely.
      * @see Decoder
      * @see Unarchiver
      */
    public void decode(Decoder decoder) throws CodingException;

    /** Gives a restored object an opportunity to communicate with its related
      * objects after all have been decoded. When an object containing a reference
      * to another encoded object is restored from an archive, the referenced
      * object may not be fully initialized. This method provides an opportunity
      * to complete the initialization. More specifically, objects retrieved from
      * <b>decodeObject()</b> might not have been fully initialized, so
      * finishDecoding() gives newly created instances an opportunity to talk to
      * other objects just after <b>decode()</b> hasbeen called on all of them.
      * @exception CodingException  Occurs when the object cannot be encoded
      * completely.
      * @see Decoder
      * @see Unarchiver
      */
    public void finishDecoding() throws CodingException;
}
