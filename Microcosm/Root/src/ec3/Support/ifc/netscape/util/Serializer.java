// Serializer.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.util;

import java.io.OutputStream;
import java.io.IOException;
import java.io.FilterOutputStream;
import java.io.ByteArrayOutputStream;

/** Object subclass that can serialize a fixed set of data types (Dictionaries,
  * arrays, Vectors, and Strings) to an ASCII stream.  If the object passed in
  * is not one of these types, or contains an object that is not one of these
  * types, the Serializer converts the object to a string via the object's
  * <b>toString()</b> method.  The serialization format is very similar to the
  * output of Hashtable's and Vector's <b>toString()</b> methods, except that
  * strings with non-alphanumeric characters are quoted and special characters
  * are escaped, so that the output can be unambiguously deserialized.
  * Serializer produces an ASCII representation with few, if any, spaces
  * separating components. To get a more readable representation, use the
  * OutputSerializer class.
  * @see Deserializer
  * @see OutputSerializer
  * @note 1.0 Added several unsafe characters that will always be quoted
  *           (fixed problem with archiving the @ symbol)
  */
public class Serializer extends FilterOutputStream {
    static private boolean unsafeChars[];
    static private int SMALL_STRING_LIMIT=40;
    private final int BUF_LEN = 128;
    private byte buf[] = new byte[BUF_LEN];
    private int bufIndex=0;

    static {
        int i,c;
        unsafeChars = new boolean[127];
        for(i=0;i<' ';i++)
            unsafeChars[i]=true;

        unsafeChars[' '] = true; /* Token separator*/
        unsafeChars['"'] = true; /* Strings */
        unsafeChars['['] = true; /* Arrays  */
        unsafeChars[']'] = true;
        unsafeChars[','] = true;
        unsafeChars['('] = true; /* Vectors */
        unsafeChars[')'] = true;
        unsafeChars['{'] = true; /* Dictionaries*/
        unsafeChars['}'] = true;
        unsafeChars['='] = true;
        unsafeChars[';'] = true;
        unsafeChars['/'] = true; /* Comment */
        unsafeChars['@'] = true; /* Null */

        unsafeChars['!'] = true; /* Reserved */
        unsafeChars['#'] = true;
        unsafeChars['$'] = true;
        unsafeChars['%'] = true;
        unsafeChars['&'] = true;
        unsafeChars['\''] = true;
        unsafeChars[':'] = true;
        unsafeChars['<'] = true;
        unsafeChars['>'] = true;
        unsafeChars['?'] = true;
        unsafeChars['\\'] = true;
        unsafeChars['^'] = true;
        unsafeChars['`'] = true;
        unsafeChars['|'] = true;
        unsafeChars['~'] = true;
    }

    /** Constructs a Serializer that writes its output to <b>outputStream</b>.
      */
    public Serializer(OutputStream outputStream) {
        super(outputStream);
    }

    private void flushBuffer() throws IOException {
        if( bufIndex > 0) {
            this.write(buf,0,bufIndex);
            bufIndex=0;
        }
    }

    final void writeOutput(int character) throws IOException {
        if( bufIndex >= BUF_LEN)
            flushBuffer();
        buf[bufIndex++] = (byte) character;
    }

    private final void serializeHashtable(Hashtable h) throws IOException {
        Enumeration e=h.keys();
        Object key;
        Object value;
        writeOutput('{');
        while(e.hasMoreElements()) {
            key = e.nextElement();
            value = h.get(key);
            /*
              /* Serialize the key. Test if it is a string to avoid one
                 /* recursion in the common case.
                  */
            if( key instanceof String )
                serializeString((String)key);
            else
                serializeObjectInternal(key);

            writeOutput('=');
            if( value instanceof String )
                serializeString((String)value);
            else
                serializeObjectInternal(value);
            writeOutput(';');
        }
        writeOutput('}');
    }

    private final void serializeArray(Object a[]) throws IOException {
        Object o;
        int i,c;
        writeOutput('[');
        for(i=0,c=a.length;i<c;i++) {
            o = a[i];
            if( o instanceof String)
                serializeString((String)o);
            else
                serializeObjectInternal(o);
            if( i < (c-1))
                writeOutput(',');
        }
        writeOutput(']');
    }

    private final void serializeVector(Vector v) throws IOException {
        Object o;
        int i,c;
        writeOutput('(');
        for(i=0,c=v.count();i<c;i++) {
            o = v.elementAt(i);
            if(o instanceof String)
                serializeString((String)o);
            else
                serializeObjectInternal(o);
            if( i < (c-1))
                writeOutput(',');
        }
        writeOutput(')');
    }

    final boolean stringRequiresQuotes(String s) {
        char ch;
        int i,c;
        for(i=0,c=s.length();i<c;i++){
            ch = s.charAt(i);
            if( ch >= 127 )
                return true;
            else if( unsafeChars[ch] )
                return true;
        }
        return false;
    }

    private final boolean stringRequiresQuotes(char str[]) {
        char ch;
        int i,c;
        for(i=0,c=str.length;i<c;i++){
            ch = str[i];
            if( ch >= 127 )
                return true;
            else if( unsafeChars[ch] )
                return true;
        }
        return false;
    }

    private final int fourBitToAscii(int n) {
        if( n < 0xa)
            return '0' + n;
        else
            return 'A' + (n-0xa);
    }

    void serializeString(String s) throws IOException {
        boolean shouldUseQuote=false;
        boolean shouldUseArray=true;
        int i,length;
        char ch;
        char str[] = null;

        if( s == null || ((length = s.length())==0)) {
            writeOutput('"');
            writeOutput('"');
            return;
        }

        if( length <= 8 ) {
            shouldUseArray=false;
        } else {
            shouldUseArray=true;
            str = s.toCharArray();
        }

        /* If the string is bigger than SMALL_STRING_LIMIT, don't bother
         * searching for unsafe character. The probably to have a space is high
         * enough to add '"' automatically.
         */
        if( length > SMALL_STRING_LIMIT )
            shouldUseQuote=true;
        else {
            if( shouldUseArray)
                shouldUseQuote = stringRequiresQuotes(str);
            else
                shouldUseQuote = stringRequiresQuotes(s);
        }

        if( shouldUseQuote )
            writeOutput('"');
        for(i=0; i < length ; i++ ) {
            if( shouldUseArray )
                ch = str[i];
            else
                ch = s.charAt(i);
            if( ch < 0xff ) { /* ASCII */
                if( ch >= '#' && ch <= '~' && ch != '\\')
                    writeOutput(ch);
                else
                    switch( ch ) {
                    case ' ':
                    case '!':
                        writeOutput(ch);
                        break;
                    case '"':
                        writeOutput('\\');
                        writeOutput('"');
                        break;
                    case '\t':
                        writeOutput('\\');
                        writeOutput('t');
                        break;
                    case '\n':
                        writeOutput('\\');
                        writeOutput('n');
                        break;
                    case '\r':
                        writeOutput('\\');
                        writeOutput('r');
                        break;
                    case '\\':
                        writeOutput('\\');
                        writeOutput('\\');
                        break;
                    default:
                        writeOutput('\\');
                        writeOutput('0'+(ch >> 6));
                        writeOutput('0'+((ch >> 3) & 0x7));
                        writeOutput('0'+(ch & 0x7));
                        break;
                    }
            } else { /* Unicode */
                writeOutput('\\');
                writeOutput('u');
                writeOutput(fourBitToAscii(ch >> 12));
                writeOutput(fourBitToAscii((ch >> 8) & 0xf));
                writeOutput(fourBitToAscii((ch >> 4) & 0xf));
                writeOutput(fourBitToAscii(ch & 0xf));
            }
        }
        if( shouldUseArray )
            str=null; /* Pretty please!*/
        if( shouldUseQuote )
            writeOutput('"');
    }

    final void serializeNull() throws IOException {
        // We have our own magic null token!  This should only happen
        // in arrays.
        writeOutput('@');
    }

    private final void serializeObjectInternal(Object anObject) throws
                                                                IOException {
        if( anObject instanceof String)
            serializeString((String) anObject);
        else if( anObject instanceof Hashtable)
            serializeHashtable((Hashtable) anObject);
        else if( anObject instanceof Object[])
            serializeArray((Object[])anObject);
        else if( anObject instanceof Vector)
            serializeVector((Vector)anObject);
        else if( anObject == null)
            serializeNull();
        else
            serializeString(anObject.toString());
    }

    /** Flushes the Serializer's output to its stream.
      */
    public void flush() throws IOException {
        flushBuffer();
        super.flush();
    }

    /** Serializes <b>anObject</b> to its stream.
      */
    public void writeObject(Object anObject) throws IOException {
        serializeObjectInternal(anObject);
    }


    /* conveniences */



    /** Convenience method for generating <b>anObject</b>'s ASCII
      * serialization. Returns <b>null</b> on error.
      */
    public static String serializeObject(Object anObject) {
        String result=null;
        if( anObject == null )
            result=null;
        else {
            ByteArrayOutputStream memory = new ByteArrayOutputStream(256);
            Serializer serializer = new Serializer(memory);

            try {
                serializer.writeObject(anObject);
                serializer.flush();
            } catch (IOException e) {}


            result = memory.toString();
            try {
                serializer.close();
                memory.close();
            } catch(IOException e) {}
            memory=null;
            serializer=null;
        }
        return result;
    }

    /** Convenience method for writing <b>anObject</b's ASCII serialization
      * to <b>outputStream</b>. Returns <b>true</b> if the serialization and
      * writing succeeds, rather than throwing an exception.
      */
    public static boolean writeObject(OutputStream outputStream,
                                      Object anObject) {
        Serializer serializer;

        try {
            serializer = new Serializer(outputStream);
            serializer.writeObject(anObject);
            serializer.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
