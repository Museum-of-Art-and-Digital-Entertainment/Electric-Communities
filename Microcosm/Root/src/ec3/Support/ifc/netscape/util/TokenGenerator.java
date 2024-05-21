// TokenGenerator.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.util;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/** TokenGenerator is a private class that transform an ASCII byte stream in
  * tokens. This is class is used by the Deserializer.
  */
class TokenGenerator extends FilterInputStream {

    /*
     *  Token types
     */
    public final static byte NULL_TOKEN                     = 0x0;
    public final static byte STRING_TOKEN                   = 0x1;
    public final static byte ARRAY_BEGIN_TOKEN              = 0x2;
    public final static byte ARRAY_END_TOKEN                = 0x3;
    public final static byte VECTOR_BEGIN_TOKEN             = 0x4;
    public final static byte VECTOR_END_TOKEN               = 0x5;
    public final static byte HASHTABLE_BEGIN_TOKEN          = 0x6;
    public final static byte HASHTABLE_KEY_VALUE_SEP_TOKEN  = 0x7;
    public final static byte HASHTABLE_KEY_VALUE_END_TOKEN  = 0x8;
    public final static byte HASHTABLE_END_TOKEN            = 0x9;
    public final static byte GENERIC_SEP_TOKEN              = 0xa;
    public final static byte NULL_VALUE_TOKEN               = 0xb;
    public final static byte LAST_TOKEN_TYPE                = 0xb;

    static byte tokenToAscii[];
    static byte asciiToToken[];


    static {
        tokenToAscii = new byte[LAST_TOKEN_TYPE+1];
        tokenToAscii[NULL_TOKEN]                     = 0; /* Error !*/
        tokenToAscii[STRING_TOKEN]                   = 0; /* Error !*/
        tokenToAscii[ARRAY_BEGIN_TOKEN]              = '[';
        tokenToAscii[ARRAY_END_TOKEN]                = ']';
        tokenToAscii[VECTOR_BEGIN_TOKEN]             = '(';
        tokenToAscii[VECTOR_END_TOKEN]               = ')';
        tokenToAscii[HASHTABLE_BEGIN_TOKEN]          = '{';
        tokenToAscii[HASHTABLE_KEY_VALUE_SEP_TOKEN]  = '=';
        tokenToAscii[HASHTABLE_KEY_VALUE_END_TOKEN]  = ';';
        tokenToAscii[HASHTABLE_END_TOKEN]            = '}';
        tokenToAscii[GENERIC_SEP_TOKEN]              = ',';
        tokenToAscii[NULL_VALUE_TOKEN]               = '@';

        asciiToToken = new byte[127];
        int i;
        for(i=0;i<=' ';i++)
            asciiToToken[i] = NULL_TOKEN;
        for(i=' '+1 ; i < 127 ; i++ )
            asciiToToken[i] = STRING_TOKEN;

        asciiToToken['['] = ARRAY_BEGIN_TOKEN;
        asciiToToken[']'] = ARRAY_END_TOKEN;
        asciiToToken['('] = VECTOR_BEGIN_TOKEN;
        asciiToToken[')'] = VECTOR_END_TOKEN;
        asciiToToken['{'] = HASHTABLE_BEGIN_TOKEN;
        asciiToToken['='] = HASHTABLE_KEY_VALUE_SEP_TOKEN;
        asciiToToken[';'] = HASHTABLE_KEY_VALUE_END_TOKEN;
        asciiToToken['}'] = HASHTABLE_END_TOKEN;
        asciiToToken[','] = GENERIC_SEP_TOKEN;
        asciiToToken['@'] = NULL_VALUE_TOKEN;
        asciiToToken['/'] = NULL_TOKEN; /* Comment begin/end */
    }

    final static int CHARACTER_COUNT_PER_ARRAY = 128;
    final static int CCPA_BIT_COUNT = 7;
    final static int CCPA_MASK      = 0x7F;

    final static int PARSING_NONE_STATE           =0;
    final static int PARSING_STRING_STATE         =1;
    final static int PARSING_QUOTED_STRING_STATE  =2;
    final static int PARSING_COMMENT_STATE        =3;
    final static int PARSING_C_STYLE_COMMENT_STATE=4;
    final static int PARSING_C_PLUS_PLUS_STYLE_COMMENT_STATE=5;

    private byte input[][];  /* Vector of array of bytes */

    private int    nextAvailableByteIndex;
    private int    markedByteIndex;
    private int    nextFreeByteSlotIndex;

    private byte      bytesForCurrentToken[];
    private int       currentToken;
    private int       lastToken;

    private int       currentLineNumber;

    private boolean previousCharacterWasBackslash=false;
    private boolean starFound=false; /* used while parsing C-style comments */
    private int     parserState;

    public TokenGenerator(InputStream in) {
        super(in);
        input = new byte[1][];
        input[0] = new byte[CHARACTER_COUNT_PER_ARRAY];
        nextAvailableByteIndex=0;
        nextFreeByteSlotIndex =0;
        currentLineNumber=0;
        parserState = PARSING_NONE_STATE;
    }

    private final void markCurrentCharacter() {
        markedByteIndex=nextAvailableByteIndex;
    }

    private final void markPreviousCharacter() {
        markedByteIndex=nextAvailableByteIndex-1;
    }

    private final void growInputBuffer() {
        byte newInput[][] = new byte[input.length+1][];
        System.arraycopy(input,0,newInput,0,input.length);
        newInput[input.length] = new byte[CHARACTER_COUNT_PER_ARRAY];
        input = newInput;
    }

    private final void readMoreCharacters() throws IOException {
        int length;
        int currentArrayIndex = nextFreeByteSlotIndex >> CCPA_BIT_COUNT;

        if( currentArrayIndex >= input.length )
            growInputBuffer();

        length = read(input[currentArrayIndex],nextFreeByteSlotIndex & CCPA_MASK,
                      CHARACTER_COUNT_PER_ARRAY - (nextFreeByteSlotIndex & CCPA_MASK));
        if(length != -1 )
            nextFreeByteSlotIndex += length;
        else
            return;

        /* Make sure we read at least CHARCTER_COUNT_PER_ARRAY characters
         * this is necessary to keep a constant number of cached characters
         */
        if(available() > 0 && length < CHARACTER_COUNT_PER_ARRAY ) {
            currentArrayIndex = nextFreeByteSlotIndex >> CCPA_BIT_COUNT;

            if( currentArrayIndex >= input.length )
                growInputBuffer();

            length = read(input[currentArrayIndex],nextFreeByteSlotIndex & CCPA_MASK,
                          CHARACTER_COUNT_PER_ARRAY - (nextFreeByteSlotIndex & CCPA_MASK));
            if( length != -1 )
                nextFreeByteSlotIndex += length;
        }
    }

    private final boolean hasMoreCharacters() throws IOException {
        if( nextAvailableByteIndex < nextFreeByteSlotIndex )
            return true;
        else {
            readMoreCharacters();
            if( nextAvailableByteIndex < nextFreeByteSlotIndex )
                return true;
        }
        return false;
    }

    private final byte peekNextCharacter() throws IOException {
        byte result=0;
        if( nextAvailableByteIndex >= nextFreeByteSlotIndex )
            readMoreCharacters();

        if( nextAvailableByteIndex < nextFreeByteSlotIndex ) {
            result = input[nextAvailableByteIndex >> CCPA_BIT_COUNT][nextAvailableByteIndex & CCPA_MASK];
            nextAvailableByteIndex++;
        }
        return result;
    }

    private final void rewindToMarkedCharacter() {
        nextAvailableByteIndex = markedByteIndex;
    }

    /* Warning this method is "inlined" manually in parseOneToken */
    private final void deletePeekedCharacters() {
        markedByteIndex=-1;
        while((nextAvailableByteIndex >> CCPA_BIT_COUNT) > 0) {
            byte tmp[] = input[0];
            int i,c;
            for(i=0,c=input.length-1; i < c ; i++ )
                input[i] = input[i+1];
            input[input.length-1] = tmp;
            nextAvailableByteIndex -= CHARACTER_COUNT_PER_ARRAY;
            nextFreeByteSlotIndex  -= CHARACTER_COUNT_PER_ARRAY;
        }
    }

    private final void deletePeekedCharactersMinusOne(){
        markedByteIndex=-1;
        while(((nextAvailableByteIndex-1) >> CCPA_BIT_COUNT) > 0) {
            byte tmp[] = input[0];
            int i,c;
            for(i=0,c=input.length-1; i < c ; i++ )
                input[i] = input[i+1];
            input[input.length-1] = tmp;
            nextAvailableByteIndex -= CHARACTER_COUNT_PER_ARRAY;
            nextFreeByteSlotIndex  -= CHARACTER_COUNT_PER_ARRAY;
        }
    }

    private final byte[] getAndDeletePeekedCharacters() {
        int length = nextAvailableByteIndex - markedByteIndex;
        byte result[] = new byte[length];
        int i,c;
        /* arraycopy is slower than this */
        for(i=markedByteIndex,c=markedByteIndex+length; i < c ; i++ ) {
            result[i-markedByteIndex] = input[i >> CCPA_BIT_COUNT][i & CCPA_MASK];
        }
        deletePeekedCharacters();
        markedByteIndex=-1;
        return result;
    }

    private final byte[] getAndDeletePeekedCharactersMinusOne() {
        int length = nextAvailableByteIndex - markedByteIndex - 1;
        byte result[] = new byte[length];
        int i,c;
        /* arraycopy is slower than this */
        for(i=markedByteIndex,c=markedByteIndex+length; i < c ; i++ ) {
            result[i-markedByteIndex] = input[i >> CCPA_BIT_COUNT][i & CCPA_MASK];
        }
        deletePeekedCharactersMinusOne();
        markedByteIndex=-1;
        return result;
    }

    private final void parseOneToken() throws DeserializationException,IOException {
        byte ch;
        while(currentToken == NULL_TOKEN ) {
            if( nextAvailableByteIndex >= nextFreeByteSlotIndex ) {
                readMoreCharacters();
                if( nextAvailableByteIndex >= nextFreeByteSlotIndex )
                    break;
            }

            if(markedByteIndex == -1 )
                markedByteIndex=nextAvailableByteIndex; /* markCurrentCharacter(); */

            /* This is peekNextCharcter */
            ch  = input[nextAvailableByteIndex >> CCPA_BIT_COUNT][nextAvailableByteIndex & CCPA_MASK];
            nextAvailableByteIndex++;
            if( ch == '\n' )
                currentLineNumber++;
            if( parserState == PARSING_QUOTED_STRING_STATE ) {
                if(!previousCharacterWasBackslash && ch=='"') {
                    currentToken = STRING_TOKEN;
                    bytesForCurrentToken = getAndDeletePeekedCharacters();
                    parserState = PARSING_NONE_STATE;
                    markedByteIndex=nextAvailableByteIndex; /* markCurrentCharacter(); */
                    previousCharacterWasBackslash = false;
                } else if( ch == '\\')
                    previousCharacterWasBackslash = true;
                else
                    previousCharacterWasBackslash = false;
            } else {
                int token;
                if(ch >= 0 && ch < 127 )
                    token = asciiToToken[ch];
                else
                    token = NULL_TOKEN;
                if( parserState == PARSING_STRING_STATE ) {
                    if( ch != '"' && token == STRING_TOKEN )
                        continue;
                    else {
                        currentToken = STRING_TOKEN;
                        bytesForCurrentToken = getAndDeletePeekedCharactersMinusOne();
                        parserState = PARSING_NONE_STATE;
                        markedByteIndex=nextAvailableByteIndex-1; /*    markPreviousCharacter(); */
                        rewindToMarkedCharacter();
                    }
                } else if( parserState == PARSING_COMMENT_STATE ) {
                    if( ch == '*' )
                        parserState = PARSING_C_STYLE_COMMENT_STATE;
                    else if( ch == '/' )
                        parserState = PARSING_C_PLUS_PLUS_STYLE_COMMENT_STATE;
                    else
                        throw new DeserializationException("Syntax error at line " + lineForLastToken(),
                                                           lineForLastToken());
                } else if( parserState == PARSING_C_STYLE_COMMENT_STATE) {
                    if( starFound && ch == '/') {
                        starFound = false;
                        parserState = PARSING_NONE_STATE;
                        continue;
                    }
                    if( ch == '*' )
                        starFound = true;
                    else
                        starFound = false;
                } else if( parserState == PARSING_C_PLUS_PLUS_STYLE_COMMENT_STATE ) {
                    if( ch == '\n' ) {
                        parserState = PARSING_NONE_STATE;
                        continue;
                    }

                } else {
                    if( ch == '/' ) {
                        parserState = PARSING_COMMENT_STATE;
                        continue;
                    }
                    if( token == NULL_TOKEN )
                        continue;
                    else if( token == STRING_TOKEN ) {
                        if( ch == '"' )
                            parserState = PARSING_QUOTED_STRING_STATE;
                        else
                            parserState = PARSING_STRING_STATE;


                        deletePeekedCharactersMinusOne();
                        markedByteIndex=nextAvailableByteIndex-1; /*     markPreviousCharacter(); */
                    } else {
                        currentToken = token;
                        bytesForCurrentToken = (byte[]) null;
                        /* This is deletePeekedCharacter() */
                        markedByteIndex=-1;
                        while((nextAvailableByteIndex >> CCPA_BIT_COUNT) > 0) {
                            byte tmp[] = input[0];
                            int i,c;
                            for(i=0,c=input.length-1; i < c ; i++ )
                                input[i] = input[i+1];
                            input[input.length-1] = tmp;
                            nextAvailableByteIndex -= CHARACTER_COUNT_PER_ARRAY;
                            nextFreeByteSlotIndex  -= CHARACTER_COUNT_PER_ARRAY;
                        }
                        markedByteIndex=nextAvailableByteIndex; /* markCurrentCharacter(); */
                    }
                }
            }
        }

        if(currentToken == NULL_TOKEN && !hasMoreCharacters()) {
            switch( parserState ) {
            case PARSING_NONE_STATE:
                break;
            case PARSING_STRING_STATE:
                currentToken = STRING_TOKEN;
                bytesForCurrentToken=getAndDeletePeekedCharacters();
                parserState = PARSING_NONE_STATE;
                previousCharacterWasBackslash=false;
                break;
            case PARSING_QUOTED_STRING_STATE:
                /* Syntax error unterminated String with quote */
                parserState = PARSING_NONE_STATE;
                previousCharacterWasBackslash=false;
                throw new DeserializationException("Unterminated string at line " + lineForLastToken(),
                                                   lineForLastToken());
            case PARSING_COMMENT_STATE:
                parserState = PARSING_NONE_STATE;
                throw new DeserializationException("Syntax error at line " + lineForLastToken(),
                                                   lineForLastToken());
            case PARSING_C_STYLE_COMMENT_STATE:
                parserState = PARSING_NONE_STATE;
                starFound=false;
                throw new DeserializationException("Unterminated comment at line " + lineForLastToken(),
                                                   lineForLastToken());
            case PARSING_C_PLUS_PLUS_STYLE_COMMENT_STATE:
                parserState = PARSING_NONE_STATE;
                break;
            }
        }
    }



    /* Return true if the TokenGenerator has more token to return */
    public final boolean hasMoreTokens() throws DeserializationException,IOException {
        if( currentToken != NULL_TOKEN )
            return true;
        else {
            parseOneToken();
            if( currentToken != NULL_TOKEN )
                return true;
        }
        return false;
    }

    /* Return the next token */
    public  final int nextToken() throws DeserializationException,IOException {
        int result = NULL_TOKEN;
        if( currentToken == NULL_TOKEN ) {
            parseOneToken();
        }
        if( currentToken != NULL_TOKEN ) {
            result = currentToken;
            lastToken = currentToken;
            currentToken = NULL_TOKEN;
        }
        return result;
    }

    /* Return the next available token but does not remove it */
    public final int peekNextToken() throws DeserializationException,IOException {
        hasMoreTokens();
        lastToken = currentToken;
        return currentToken;
    }

    /* Return the bytes parsed to generate the last returned token.
     */
    public final byte[] bytesForLastToken() {
        if( lastToken == STRING_TOKEN )
            return bytesForCurrentToken;
        else {
            byte b[] = new byte[1];
            b[0] = tokenToAscii[lastToken];
            return b;
        }
    }

    /* Returns the ASCII byte that matches a given token. Use this method
     * when the token is a separator (everything but a string).
     */
    public byte byteForLastToken() {
        return tokenToAscii[lastToken];
    }

    /* Return the line number for the last returned token */
    public int lineForLastToken() {
        return currentLineNumber+1;
    }
}

