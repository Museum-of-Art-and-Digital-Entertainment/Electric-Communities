// Deserializer.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.io.StringBufferInputStream;


/** FilterInputStream subclass that can deserialize Hashtables, Vectors,
  * arrays, and Strings from an ASCII stream.  The serialization format is very
  * similar to the output of Hashtable's and Vector's <b>toString()</b>
  * methods, except strings with non-alphanumeric characters are quoted and
  * special characters are escaped so that the output can be unambiguously
  * deserialized.
  * @see Serializer
  */
public class Deserializer extends FilterInputStream {
    private TokenGenerator tokenGenerator;


    /* static methods */


    /** Convenience method for creating a Deserializer taking its input from
      * <b>inputStream</b>.  This method only returns <b>null</b> on error
      * instead of throwing an exception.
      */
    public static Object readObject(InputStream inputStream) {
        Object object;
        Deserializer deserializer;

        try {
            deserializer = new Deserializer(inputStream);
            object = deserializer.readObject();
        } catch (IOException e) {
            object = null;
        } catch (DeserializationException e) {
            object = null;
        }
        return object;
    }



    /* constructors */


    /** Constructs a Deserializer that takes its input from <b>inputStream</b>.
      */
    public Deserializer(InputStream inputStream) {
        super(inputStream);
        tokenGenerator = new TokenGenerator(inputStream);
    }

    /** Convenience method for deserializing from the string
      * <b>serialization</b>. Returns <b>null</b> on error.
      */
    public static Object deserializeObject(String serialization) {
        StringBufferInputStream in;

        if (serialization == null)
            return null;

        in = new StringBufferInputStream(serialization);
        return readObject((InputStream)in);
    }

    /** Deserializes the next Dictionary, array, Vector, or String from the
      * current input stream.
      */
    public Object readObject() throws IOException, DeserializationException {
        return readObjectInternal();
    }

    private final Object readObjectInternal() throws IOException,
                                                DeserializationException {
        int token;

        if(!tokenGenerator.hasMoreTokens())
            return null;

        token = tokenGenerator.nextToken();
        switch(token) {
            case TokenGenerator.STRING_TOKEN:
                return stringForToken();
            case TokenGenerator.ARRAY_BEGIN_TOKEN:
                return readArray();
            case TokenGenerator.VECTOR_BEGIN_TOKEN:
                return readVector();
            case TokenGenerator.HASHTABLE_BEGIN_TOKEN:
                return readHashtable();
            default:
                syntaxError();
                return null;
        }
    }

    private final void readKeyValuePair(Hashtable result) throws IOException,
                                                    DeserializationException {
        Object key;
        Object value;
        int token;

        key = readObjectInternal();
        if(key == null)
            unterminatedExpression();

        if(!tokenGenerator.hasMoreTokens()) {
            unterminatedExpression();
        }

        token = tokenGenerator.nextToken();
        if(token != TokenGenerator.HASHTABLE_KEY_VALUE_SEP_TOKEN) {
            syntaxError();
        }

        if(!tokenGenerator.hasMoreTokens()) {
            unterminatedExpression();
        }

        value = readObjectInternal();
        if(value == null)
            unterminatedExpression();

        result.put(key,value);

        if(!tokenGenerator.hasMoreTokens()) {
            unterminatedExpression();
        }

        token = tokenGenerator.peekNextToken();
        if (token == TokenGenerator.HASHTABLE_KEY_VALUE_END_TOKEN ||
            token == TokenGenerator.GENERIC_SEP_TOKEN)
            tokenGenerator.nextToken();
    }

    private final Hashtable readHashtable() throws IOException,
                                                DeserializationException {
        Hashtable result = new Hashtable();
        int token;

        while(true) {
            if (!tokenGenerator.hasMoreTokens()) {
                unterminatedExpression();
            }

            token = tokenGenerator.peekNextToken();
            if (token == TokenGenerator.HASHTABLE_END_TOKEN) {
                tokenGenerator.nextToken();
                return result;
            }

            readKeyValuePair( result );
        }
    }

    private final Vector readVector() throws IOException,
                                                    DeserializationException {
        Vector result = new Vector();
        int token;
        boolean justAddedObject=false;
        Object object;

        while (true) {
            if (!tokenGenerator.hasMoreTokens()) {
                unterminatedExpression();
            }

            token = tokenGenerator.peekNextToken();
            if (token == TokenGenerator.VECTOR_END_TOKEN) {
                tokenGenerator.nextToken();
                return result;
            }

            if (token == TokenGenerator.GENERIC_SEP_TOKEN) {
                tokenGenerator.nextToken();
                if (justAddedObject) {
                    justAddedObject = false;
                } else {
                    syntaxError();
                }
            } else if (justAddedObject) {
                syntaxError();
            }

            object = readObjectInternal();
            if (object != null) {
                result.addElement(object);
                justAddedObject = true;
            }
        }
    }

    private final Object[] readArray() throws IOException,
                                                DeserializationException {
        Object buf[] = new Object[16];
        int bufIndex=0;
        Object obj;
        int nextToken;
        boolean justAddedObject=false;

        while (true) {
            if (!tokenGenerator.hasMoreTokens()) {
                unterminatedExpression();
            }

            nextToken = tokenGenerator.peekNextToken();
            if (nextToken == TokenGenerator.ARRAY_END_TOKEN) {
                tokenGenerator.nextToken();
                Object result[] = new Object[bufIndex];
                System.arraycopy(buf, 0, result, 0, bufIndex);
                return result;
            } else if (nextToken == TokenGenerator.GENERIC_SEP_TOKEN) {
                tokenGenerator.nextToken();
                if (justAddedObject) {
                    justAddedObject=false;
                } else {
                    syntaxError();
                }
            } else if (justAddedObject) {
                syntaxError();
            }

            // This code is here and not in readObjectInternal() because we
            // only want to allow null in arrays.  It should be a syntax error
            // for the null token to appear elsewhere.

            nextToken = tokenGenerator.peekNextToken();
            if (nextToken == TokenGenerator.NULL_VALUE_TOKEN) {
                tokenGenerator.nextToken();
                obj = null;
            } else
                obj = readObjectInternal();

            buf[bufIndex++] = obj;
            if (bufIndex == buf.length) {
                Object newBuf[] = new Object[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, buf.length);
                buf = newBuf;
            }
            justAddedObject = true;
        }
    }

    private final String stringForToken() throws DeserializationException{
        byte str[] = tokenGenerator.bytesForLastToken();

        if (str == null || str.length == 0 ) {
            internalInconsistency("empty string");
        }

        if ( str[0] == '"' ) {
            byte b;
            char charBuf[] = new char[32];
            int charBufIndex=0;
            int i,c;

            for (i=1,c=str.length-1;i<c;i++) {
                b = str[i];
                if ( b == '\\' ) {
                    byte nextByte = 0;
                    char nextChar = 0;
                    if(++i < c )
                        nextByte = str[i];
                    else
                        malformedString();

                    switch(nextByte) {
                        case '"':
                            nextChar = (char)nextByte;
                            break;
                        case 't':
                            nextChar = '\t';
                            break;
                        case 'n':
                            nextChar = '\n';
                            break;
                        case 'r':
                            nextChar = '\r';
                            break;
                        case '\\':
                            nextChar = '\\';
                            break;
                        case 'u':
                            byte one=0,two=0,three=0,four=0;

                            if (++i < c )
                                one = str[i];
                            else
                                malformedString();

                            if (++i < c)
                                two = str[i];
                            else
                                malformedString();

                            if (++i < c)
                                three = str[i];
                            else
                                malformedString();

                            if (++i < c)
                                four = str[i];
                            else
                                malformedString();

                            if (isHexa(one) && isHexa(two) && isHexa(three) &&
                                isHexa(four)) {
                                nextChar =
                                        (char)((asciiToFourBits(one) << 12) |
                                               (asciiToFourBits(two) << 8)  |
                                               (asciiToFourBits(three) << 4) |
                                               asciiToFourBits(four));
                                break;
                            } else
                                malformedString();
                                break;

                        default:
                            byte up=0,middle=0,low=0;

                            up = nextByte;
                            if (++i < c )
                                middle = str[i];
                            else
                                i--;

                            if (++i < c )
                                low = str[i];
                            else
                                i--;

                            if (up >= '0' && up <= '7' &&
                                middle >= '0' && middle <= '7' &&
                                low >= '0' && low <= '7' ) {
                                nextChar = (char)(((up - '0') << 6) |
                                           ((middle - '0') << 3) |
                                           (low - '0'));
                                break;
                            } else {
                                malformedString();
                            }

                            break;
                    }

                    charBuf[charBufIndex++] = nextChar;
                    if (charBufIndex == charBuf.length ) {
                        char newCharBuf[]  = new char[charBuf.length * 2];
                        System.arraycopy(charBuf, 0, newCharBuf, 0,
                                         charBuf.length);
                        charBuf = newCharBuf;
                    }
                } else {
                    charBuf[charBufIndex++] = (char)b;
                    if (charBufIndex == charBuf.length ) {
                        char newCharBuf[]  = new char[charBuf.length * 2];
                        System.arraycopy(charBuf, 0, newCharBuf, 0,
                                         charBuf.length);
                        charBuf = newCharBuf;
                    }
                }
            }

            return new String(charBuf,0,charBufIndex);
        } else {
            return new String(str,0);
        }
    }

    private final boolean isHexa(byte b) {
        if ((b >= '0' && b <= '9') ||
            (b >= 'a' && b <= 'f') ||
            (b >= 'A' && b <= 'F') )
            return true;
        else
            return false;
    }

    private final byte asciiToFourBits(byte b) {
        if( b >= '0' && b <= '9' )
            return (byte)(b - '0');
        else if( b >= 'a' && b <= 'f')
            return (byte)(b - 'a'+0xa);
        else
            /* We assume that byte b makes sense... Caller should test it */
            return (byte)(b - 'A'+0xa);
    }

    private void malformedString() throws DeserializationException {
        int line = tokenGenerator.lineForLastToken();

        throw new DeserializationException("Malformed string at line " + line +
                                           ":" +
                    new String(tokenGenerator.bytesForLastToken(),0), line);
    }

    private void syntaxError() throws DeserializationException {
        int line = tokenGenerator.lineForLastToken();
        throw new DeserializationException("Syntax error at line " + line,
                                            line);
    }

    private void internalInconsistency(String type) throws
                                                    DeserializationException {
        int line = tokenGenerator.lineForLastToken();

        throw new DeserializationException(
            "Internal inconsistency exception. Please report this problem. " +
            type + " " + line, line);
    }

    private void unterminatedExpression() throws DeserializationException {
        int line = tokenGenerator.lineForLastToken();

        throw new DeserializationException("Unterminated expression at line " +
                                           line, line);
    }
}
