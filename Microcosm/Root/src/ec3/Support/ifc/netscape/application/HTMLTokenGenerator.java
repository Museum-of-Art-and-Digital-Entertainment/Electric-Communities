// HTMLTokenGenerator.java
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.
//
//

package netscape.application;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/*
 *  HTMLTokenGenerator is a private subclass of FilterInputStream that parse HTML
 *  into tokens.
 *  @note 1.0 changes
 */
class HTMLTokenGenerator extends FilterInputStream {

    /*
     *  Token types
     */
    public final static byte NULL_TOKEN                     = 0x0;
    public final static byte STRING_TOKEN                   = 0x1;
    public final static byte MARKER_BEGIN_TOKEN             = 0x2;
    public final static byte MARKER_END_TOKEN               = 0x3;
    public final static byte COMMENT_TOKEN                  = 0x4;
           final static byte LAST_TOKEN_TYPE                = 0x4;


    final static int CHARACTER_COUNT_PER_ARRAY = 128;
    final static int CCPA_BIT_COUNT = 7;
    final static int CCPA_MASK      = 0x7F;

    final static int PARSING_NONE_STATE           =0;
    final static int PARSING_STRING_STATE         =1;
    final static int PARSING_MARKER_STATE         =2;
    final static int PARSING_COMMENT_STATE        =3;
    final static int PARSING_MARKER_OR_COMMENT_STATE    =4;
    final static int PARSING_END_COMMENT_ONE_STATE = 5;  /* First dash parsed */
    final static int PARSING_END_COMMENT_TWO_STATE = 6;  /* Second dash parsed */

    private byte input[][];  /* Vector of array of bytes */
    private int    nextAvailableByteIndex;
    private int    markedByteIndex;
    private int    nextFreeByteSlotIndex;

    private int    currentLineNumber;
    private int    parserState;

    private int    currentToken;
    private String currentTokenString;     /* The string for a string, the marker otherwise */
    private String currentTokenAttributes; /* The attribute string. null for a String   */

    public HTMLTokenGenerator(InputStream in) {
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
        /* Make sure we read at least CHARCTER_COUNT_PER_ARRAY characters */
        if( length < CHARACTER_COUNT_PER_ARRAY ) {
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

    private final boolean isSpaceOrCR(byte b) {
        if( b == ' ' || b == '\t' || b == '\n' || b == '\r')
            return true;
        else
            return false;
    }

    /* Return the string for the marker attributes */
    private String attributes(byte bArray[]) throws HTMLParsingException  {
        int i,c;
        byte ch;

        if( bArray.length == 0 || bArray[0] != '<' ||
            bArray[bArray.length-1] != '>')
            syntaxError("Malformed marker");

        i = 1;
        c = bArray.length;
        while(i < c && isSpaceOrCR(bArray[i]))
            i++;

        while(i<c  && !isSpaceOrCR(bArray[i]))
            i++;

        while(i<c && isSpaceOrCR(bArray[i]))
            i++;

        if( (c-1)-i > 0 )
            return new String( bArray , 0, i, (c-1)-i);
        else
            return "";
    }

    /* Return the string for the marker (excluding the / for end markers) */
    private String marker(byte bArray[]) throws HTMLParsingException {
        int i,c;
        int start;
        if( bArray.length == 0 || bArray[0] != '<' ||
            bArray[bArray.length-1] != '>')
            syntaxError("Malformed marker");

        start = i = 1;
        c = bArray.length;

        while(i < c && isSpaceOrCR(bArray[i])) {
            i++;
            start++;
        }

        if( bArray[i] == '/' ) {
            i++;
            start++;
        }


        while(i<(c-1) && !isSpaceOrCR(bArray[i]))
            i++;

        return (String)(new String( bArray, 0 , start, i - start )).toUpperCase();
    }

    /** Return true if the marker in the buffer is a marker begin */
    private boolean isMarkerBegin(byte bArray[]) throws HTMLParsingException {
        int i,c;

        if( bArray.length == 0 || bArray[0] != '<' ||
            bArray[bArray.length-1] != '>')
            syntaxError("Malformed marker");

        i = 1;
        c = bArray.length;

        while(i < c && isSpaceOrCR(bArray[i]))
            i++;

        if( bArray[i] == '/' )
            return false;
        else
            return true;
    }

    private final void parseOneToken() throws HTMLParsingException,IOException {
        byte ch;

        while( currentToken == NULL_TOKEN ) {
            if( hasMoreCharacters() ) {
                ch = peekNextCharacter();
            } else {
                if( parserState != PARSING_STRING_STATE ) {
                    if( markedByteIndex != -1 )
                        rewindToMarkedCharacter();
                }
                break;
            }
            if( ch == '\n' )
                currentLineNumber++;
            switch( parserState ) {
            case PARSING_NONE_STATE:
                if( ch == '<' )
                    parserState = PARSING_MARKER_OR_COMMENT_STATE;
                else
                    parserState = PARSING_STRING_STATE;
                markPreviousCharacter();
                break;
            case PARSING_STRING_STATE:
                if( ch == '<' ) {
                    currentToken = STRING_TOKEN;
                    currentTokenAttributes = null;
                    currentTokenString = new String(getAndDeletePeekedCharactersMinusOne(),0);
                    markPreviousCharacter();
                    parserState = PARSING_MARKER_OR_COMMENT_STATE;
                }
                break;
            case PARSING_MARKER_STATE:
                if( ch == '>' ) {
                    byte allMarker[] = getAndDeletePeekedCharacters();
                    if( isMarkerBegin(allMarker))
                        currentToken = MARKER_BEGIN_TOKEN;
                    else
                        currentToken = MARKER_END_TOKEN;
                    currentTokenAttributes = attributes( allMarker );
                    currentTokenString     = marker( allMarker);
                    parserState = PARSING_NONE_STATE;
                }
                break;
            case PARSING_MARKER_OR_COMMENT_STATE:
                if( ch == '!' )
                    parserState = PARSING_COMMENT_STATE;
                else
                    parserState = PARSING_MARKER_STATE;
                break;
            case PARSING_COMMENT_STATE:
                if( ch == '-')
                    parserState = PARSING_END_COMMENT_ONE_STATE;
                else if( ch == '>' ) {
                    currentToken = COMMENT_TOKEN;
                    currentTokenString = new String(getAndDeletePeekedCharacters(),0);
                    currentTokenAttributes = null;
                    parserState = PARSING_NONE_STATE;
                }
                break;
            case PARSING_END_COMMENT_ONE_STATE:
                if( ch == '-')
                    parserState = PARSING_END_COMMENT_TWO_STATE;
                else if( ch == '>' ) {
                    currentToken = COMMENT_TOKEN;
                    currentTokenString = new String(getAndDeletePeekedCharacters(),0);
                    currentTokenAttributes = null;
                    parserState = PARSING_NONE_STATE;
                } else
                    parserState = PARSING_COMMENT_STATE;
                break;
            case PARSING_END_COMMENT_TWO_STATE:
                if( ch == '\n' || ch == '\r' )
                    continue;
                else if( ch == '>' ) {
                    currentToken = COMMENT_TOKEN;
                    currentTokenString = new String(getAndDeletePeekedCharacters(),0);
                    currentTokenAttributes = null;
                    parserState = PARSING_NONE_STATE;
                } else
                    parserState = PARSING_COMMENT_STATE;
                break;
            }
        }

        if( currentToken == NULL_TOKEN && !hasMoreCharacters() ) {
            switch( parserState ) {
            case PARSING_NONE_STATE:
                break;
            case PARSING_STRING_STATE:
                currentToken = STRING_TOKEN;
                currentTokenString = new String(getAndDeletePeekedCharacters(),0);
                currentTokenAttributes = null;
                parserState = PARSING_NONE_STATE;
                break;
            case PARSING_MARKER_STATE:
            case PARSING_MARKER_OR_COMMENT_STATE:
                parserState = PARSING_NONE_STATE;
                syntaxError("Unterminated marker");
                break;
            default:
                parserState = PARSING_NONE_STATE;
                syntaxError("Unterminated comment. Comment should end with -->");
                break;
            }
        }
    }



    /* Return true if the TokenGenerator has more token to return */
    public final boolean hasMoreTokens() throws HTMLParsingException,IOException {
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
    public final int nextToken() throws HTMLParsingException,IOException {
        int result = NULL_TOKEN;
        if( currentToken == NULL_TOKEN ) {
            parseOneToken();
        }
        if( currentToken != NULL_TOKEN ) {
            result = currentToken;
            currentToken = NULL_TOKEN;
        }
        return result;
    }

    /* Return the next available token but does not remove it */
    final int peekNextToken() throws HTMLParsingException,IOException {
        if( currentToken == NULL_TOKEN )
            parseOneToken();
        return currentToken;
    }

    /* Return the String value of the last returned token.
     * The string value for a string is the string, the comment for a comment
     * and the marker for a marker.
     */
    public final String stringForLastToken() {
        return currentTokenString;
    }

    /* Return the attribute argument for the last returned token.
     */
    public final String attributesForLastToken() {
        return currentTokenAttributes;
    }

    /* Return the line number for the last returned token */
    final int lineForLastToken() {
        return currentLineNumber+1;
    }


    final void syntaxError(String description) throws HTMLParsingException {
        throw new HTMLParsingException( description , lineForLastToken());
    }
}





