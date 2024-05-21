package ec.state;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ec.util.NestedException;
import ec.tables.ObjKeyTable;
import ec.tables.IntKeyTable;

/**
 * A StateDictionary is a key/value representation of a Stateful Object's state. The
 * encoding of an Object into a StateDictionary is totally explicit, and done by the
 * Stateful Object itself. The class of the Stateful Object is implicitly kept in the
 * StateDictionary, but everything else is put in by the Object.
 * <p>
 * To separate the capability for an Object to encode itself into a StateDictionary
 * from the capability to hold onto a StateDictionary (which can be written to a String
 * and later decoded), Stateful Objects are actually passed a WriteableStateDictionary
 * in their encode methods. A top level (root) StateDictionary is a wrapper around a
 * WriteableStateDictionary Object created when a Stateful Object is encoded.
 * <p>
 * A StateDictionary is immutable once created, mappings cannot be added, changed
 * or removed. As well, any Object referencing a StateDictionary cannot see
 * its mappings, only the Object created during decode of a StateDictionary can
 * get the mappings. Accessing mappings is done through a separate capability via
 * a ReadableStateDictionary that wraps the WriteableStateDictionary during decode.
 * <p>
 * Keys must be Strings, and values can be one of a limited set of types:
 * <p>
 * <pre>
 * String
 * Object implementing the Stateful interface
 * primitive type boolean
 * primitive type double
 * primitive type int
 * Hashtable (containing only Strings and Stateful Objects)
 * Vector (containing only Strings and Stateful Objects)
 * </pre>
 * <p>
 * To create a StateDictionary, you call CreateStateDictionary with an Object implementing
 * the Stateful interface. The StateDictionary will implicitly save the class of the
 * Object, and then ask the Object to encode itself into the StateDictionary. The Object
 * can map other Stateful Objects into the StateDictionary, thereby capturing a graph
 * of Objects in a graph of StateDictionaries. The graph can be cyclic, and Object
 * identity is preserved such that if multiple references are made to the same Object
 * during encoding, upon decoding all references will be to the same Object.
 * <p>
 * Note that String identity is not preserved, the same String passed in multiple
 * times will be encoded/decoded as separate Strings.
 * <p>
 * A StateDictionary can be converted into a String representation, which is human
 * readable. This String can then be parsed back into a StateDictionary. A simple
 * example of the String representing a StateDictionary looks like the following:
 * <p>
 * <pre>
 *  "*** State Dictionary *** Version 0.1"
 *  ec.tests.state.BigBundle (#1) {
 *      "Name" = "Mr. Big"
 *      "LittleBundle" = ec.tests.state.LittleBundle (#2) {
 *          "Refs" = ec.e.state.VectorState (#3) {
 *              "Number of Elements" = 1
 *              "Element 0" = "Ref String 0"
 *          }
 *          "BigBundle" = #1
 *          "Type" = "1234"
 *      }
 *      "SimpleBundles" = ec.e.state.VectorState (#4) {
 *          "Number of Elements" = 1
 *          "Element 0" = ec.tests.state.SimpleBundle (#5) {
 *          }
 *      }
 *      "IntValue" = 9999
 *      "DoubleValue" = 7654.321
 *      "Whatever" = &lt;true&gt;
 *      "Whenever" = &lt;false&gt;
 *      "Nobody" = &lt;null&gt;
 *  }
 * </pre>
 * <p>
 * A graph of objects can be decoded from StateDictionary which has been fully encoded 
 * or parsed from a String. This is done by calling the method getStatefulObject
 * on the StateDictionary. The StateDictionary will create an instance of the class
 * that it describes (note that Stateful Objects must have a null constructor
 * which can be called to create an "empty" object suitable for decoding the
 * state). After creating the Object, the StateDictionary will pass in a read
 * only capability to itself to the Object, asking the Object to first decode its
 * preface (which gives the Object the opportunity to return a different Object
 * based on some of the information contained within the mappings), and then will
 * ask the returned Object (typically the same Object) to finish decoding itself.
 * <p>
 * As Objects are decoded, any that implement the StateAwakener interface are
 * saved in a list. After the top level StateDictionary (and hence all other
 * StateDictionaries contained within the graph) has been decoded, all of the
 * objects in the wakeup list have the method wakeupAfterStateDecoding called.
 * This gives Objects that need to invoke methods on other Objects they refer to
 * the chance to do so when it is guaranteed that all Objects in the graph have
 * been fully decoded.
 * <p>
 * Note that Objects can never reference a StateDictionary for any Object other
 * than themselves. This prevents mutually suspicious Objects from accessing each
 * other's state, and provides encapsulation of state between Objects. During
 * decoding, Objects cannot add, remove, or mutate mappings in their StateDictionary,
 * as a ReadableStateDictionary capability is all the Object can reference.
 * <p>
 * Versioning can be handled explicitly by the Stateful Object being coded. The Object
 * can put in a key/value pair representing the Version, and use that upon decoding
 * to determine the version of the Object that was encoded into the StateDictionary.
 * In simple cases, versioning can be done simply by using whatever key/value mappings
 * are available in the StateDictionary.
 * <p>
 * @see ec.state.ReadableStateDictionary 
 * @see ec.state.Stateful 
 * @see ec.state.StateAwakener 
 * @see ec.state.WriteableStateDictionary
 */ 
public class StateDictionary {
    final static String TrueValue = "<true>";
    final static String FalseValue = "<false>";
    final static String NullString = "<null>";
            
    final static String Version = "0.2";
    final static String Header = "*** State Dictionary *** Version ";
        
    private WriteableStateDictionary dictionary; // StateDictionary we're wrapping
    
    /**
     * Creates a StateDictionary to represent the Stateful Object.
     *
     * @param obj The Stateful Object to be represented in the StateDictionary
     * @return The StateDictionary
     */ 
    public static StateDictionary CreateStateDictionary (Stateful obj) {
        WriteableStateDictionary dictionary = new WriteableStateDictionary(obj);
        return new StateDictionary(dictionary);
    }   
        
    /**
     * Decodes a StateDictionary into the Object it represents. In so
     * doing, the Object represented is asked to decode itself, which 
     * will likely recursively decode the entire graph of StateDictionaries.
     *
     * @return The Stateful Object represented by this StateDictionary.
     */ 
    public Stateful getStatefulObject ()  {
        return dictionary.getStatefulObject();
    }
    
    /**
     * Convert this StateDictionary to a String Representation. The
     * String produced by this method can be parsed back into a graph
     * of StateDictionaries. Ths String is a deep representation of 
     * the whole graph of StateDictionaries.
     *
     * @return The String representation.
     */ 
    public String toString () {
        return dictionary.toString();
    }
    
    /**
     * Parses a String to produce a graph of StateDictionaries. The top level
     * (root) StateDictionary can be asked to return the Stateful Object it
     * represents.
     *
     * @param string The String to parse.
     * @return The top of the StateDictionary graph produced
     * as a result of parsing the String.
     *
     * @exception StateDictionaryParsingException Thrown if there is a parsing error.
     */ 
    public static StateDictionary parseString (String string) throws StateDictionaryParsingException {
        StateDictionaryParser parser = new StateDictionaryParser(string);
        parser.nextString(true); // Skip leading "/*"
        String header = parser.nextString(true);
        String versionHeader = StateDictionary.Header + StateDictionary.Version;
        if (versionHeader.equals(header) == false) {
            throw new StateDictionaryParsingException("Version mismatch, expected " +
                StateDictionary.Version + 
                ", got " + header);
        }
        
        SharedState sharedState = new SharedState(SharedState.Incoming);
        
        String className = parser.nextString(true); 
        int index = parser.nextIndex(); 
        parser.nextString(true); // Skip "{"
        WriteableStateDictionary dictionary =
            new WriteableStateDictionary(sharedState, className, index, true);
        dictionary.parse(parser);
        return new StateDictionary(dictionary);
    }
    
    /**
     * Package Constructor to wrap a WriteableStateDictionary in a
     * StateDictionary.
     *
     * @return The StateDictionary wrapping the WriteableStateDictionary
     */
    /* package */ StateDictionary (WriteableStateDictionary dictionary) {
        this.dictionary = dictionary;
    }   
}

class StateDictionaryParser
{
    static final int TypeBeginning = -1;
    static final int TypeNull = 0;
    static final int TypeString = 1;
    static final int TypeIndex = 2;
    static final int TypeObject = 3;
    static final int TypeEnd = 4;
    static final int TypeError = 5;
    
    private char[] chars;
    private int type;
    private int index;
    
    StateDictionaryParser (String string) {
        chars = string.toCharArray();
        index = 0;
        type = TypeBeginning;
    }   
    
    String nextString (boolean stripQuotes) {
        if (findNext() == false) {
            return null;
        }
        char ch = chars[index];
        if (ch == '"') {
            type = TypeString;
            if (stripQuotes) {
                return makeUnquotedString();
            }
            else {
                return makeQuotedString();
            }
        }
        else if ((ch == '(') || (ch == '#')) {
            type = TypeIndex;
            return makeIndex();
        }
        else {
            type = TypeString; // Assume String
            String string = makeString(); // Might reset type to Error
            if (StateDictionary.NullString.equals(string)) {
                type = TypeNull; // Turns out to be null indicator
            }
            return string;
        }
    }   

    String nextValue () throws StateDictionaryParsingException {
        String sepString = nextString(true);
        String string = nextString(false); // Will set type
        
        boolean isChar = Character.isUnicodeIdentifierStart(string.charAt(0));
        if (isChar) {
            type = TypeObject;
        }       
        return string;
    }   
    
    int nextIndex () throws StateDictionaryParsingException {
        String indexString = nextString(true);
        if (type != TypeIndex) {
            throw new StateDictionaryParsingException("Expected Index, got type " + type +
                ", string is " + indexString);
        }
        return (new Integer(indexString)).intValue();
    }   
    
    int currentValueType () {
        return type;
    }   
        
    private boolean findNext () {
        char ch;
        while (true) {
            ch = chars[index];
            if ((ch != ' ') && (ch != '\t') && (ch != '\n')) {
                break;
            }
            if (++index >= chars.length) {
                type = TypeError;
                return false;
            }
        }
        return true;
    }
        
    private String makeString () {
        int start = index;
        char ch;
        while (true) {
            ch = chars[index];
            if ((ch == ' ') || (ch == '\t') || (ch == '\n')) {
                break;
            }
            if (++index >= chars.length) {
                type = TypeError;
                return null;
            }
        }
        int size = index - start;
        return new String(chars, start, size);      
    }   
    
    private String makeQuotedString () {
        int start = index++;
        while (true) {
            if (index >= chars.length) {
                type = TypeError;
                return null;
            }
            if (chars[index++] == '"') {
                break;
            }
        }
        int size = index - start;
        return new String(chars, start, size);      
    }   
    
    private String makeUnquotedString () {
        int start = ++index;
        while (true) {
            if (index >= chars.length) {
                type = TypeError;
                return null;
            }
            if (chars[index++] == '"') {
                break;
            }
        }
        int size = index - (start + 1);
        if (size == 0) {
            return "";
        }
        return new String(chars, start, size);      
    }   
    
    private String makeIndex () {
        if (chars[index] == '(') {
            index = index + 2;
        }
        else {
            index++;
        }
        int start = index;
        while (true) {
            if (index >= chars.length) {
                type = TypeError;
                return null;
            }
            char ch = chars[index++];
            if ((ch == ')') || Character.isWhitespace(ch)) {
                break;
            }
        }
        int size = index - (start + 1);
        if (size == 0) {
            return "0";
        }
        return new String(chars, start, size);      
    }   
}   

class StateIndex
{
    int index;
    
    StateIndex (int index)  {
        this.index = index;
    }   
    
    int getIndex()  {
        return index;
    }   
}   

class SharedState
{
    public static final int Outgoing = 1;
    public static final int Incoming = 2;
    
    public ObjKeyTable objectMappings;
    public IntKeyTable indexMappings;
    
    private int currentIndex = 0;
    private int direction;
    private Vector wakers;
    
    SharedState (int direction) {
        this.direction = direction;
        if (direction == Incoming) {
            setIndexMappings();
        }
        else {
            objectMappings = new ObjKeyTable(0, 32, false);
        }
    }
    
    int get (Object obj) {
        if (direction != Outgoing) {
            // Squawk
        }
        return objectMappings.get(obj);
    }   
    
    int put (Object obj) {
        if (direction != Outgoing) {
            // Squawk
        }
        objectMappings.put(obj, ++currentIndex);
        return currentIndex;
    }
    
    void becomeIncoming () {
        if (direction != Outgoing) {
            // Squawk
        }
        direction = Incoming;
        objectMappings = null;
        setIndexMappings();
    }
    
    Object get (int index) {
        if (direction != Incoming) {
            // Squawk
        }
        return indexMappings.get(index);
    }   
    
    void put (int index, Object obj) {
        if (direction != Incoming) {
            // Squawk
        }
        indexMappings.put(index, obj);
        if (obj instanceof StateAwakener) {
            if (wakers == null) {
                wakers = new Vector();
            }
            wakers.addElement(obj);
        }
    }   
    
    void wakeup () {
        if (wakers == null) {
            return;
        }
        Enumeration elements = wakers.elements();
        while (elements.hasMoreElements()) {
            StateAwakener awakener = (StateAwakener)elements.nextElement();
            awakener.wakeupAfterStateDecoding();
        }
        wakers = null;
    }
    
    void clearIndexMappings () {
        setIndexMappings();
    }   
    
    private void setIndexMappings () {
        indexMappings = new IntKeyTable(32, false);
    }   
}    

class VectorState implements Stateful
{
    private Vector vector;
    
    private static final String NumberKey = "Number of Elements";
    private static final String ElementKey = "Element ";
        
    VectorState () {
    }   

    VectorState (Vector vector) {
        this.vector = vector;   
    }   
    
    public Vector getVector () {
        return vector;
    }   
    
    public void encodeState (WriteableStateDictionary dictionary) {
        int size = vector.size();
        dictionary.addIntMapping(NumberKey, size);
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            Object element = vector.elementAt(i);
            if (element == null) {
                    dictionary.addStatefulMapping(ElementKey + i, null);
            }
            else if (element instanceof String) {
                    String string = (String)element;
                    dictionary.addStringMapping(ElementKey + i, string);
            }
            else if (element instanceof Stateful) {
                    Stateful stateful = (Stateful)element;
                    dictionary.addStatefulMapping(ElementKey + i, stateful);
            }
            else {
                // Squawk!
            }
        }
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        int size = dictionary.getIntMapping(NumberKey);
        if (size == 0) {
            vector = new Vector(16); // Why 16... Why not...
            return;
        }
        vector = new Vector(size);
        for (int i = 0; i < size; i++) {
            vector.addElement(dictionary.getObjectMapping(ElementKey + i)); 
        }
    }
}
    
class HashtableState implements Stateful
{
    private Hashtable table;
    
    private static final String NumberKey = "Number of Entries";
    private static final String KeyKey = "Key ";
    private static final String ValueKey = "Value ";
        
    HashtableState () {
    }       
    
    HashtableState (Hashtable table) {
        this.table = table;
    }   
    
    public Hashtable getHashtable () {
        return table;
    }   
    
    public void encodeState (WriteableStateDictionary dictionary) {
        int size = table.size();
        dictionary.addIntMapping(NumberKey, size);
        if (size == 0) {
            return;
        }
        Enumeration keys = table.keys();
        Enumeration values = table.elements();
        int i = 0;
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = values.nextElement();
            if (key instanceof String) {
                    String string = (String)key;
                    dictionary.addStringMapping(KeyKey + i, string);
            }
            else if (key instanceof Stateful) {
                    Stateful stateful = (Stateful)key;
                    dictionary.addStatefulMapping(KeyKey + i, stateful);
            }
            else {
                // Squawk!
            }
            if (value instanceof String) {
                    String string = (String)value;
                    dictionary.addStringMapping(ValueKey + i, string);
            }
            else if (value instanceof Stateful) {
                    Stateful stateful = (Stateful)value;
                    dictionary.addStatefulMapping(ValueKey + i, stateful);
            }
            else {
                // Squawk!
            }
            i++; // Increment key index
        }
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        int size = dictionary.getIntMapping(NumberKey);
        if (size == 0) {
            table = new Hashtable(16); // Why 16... Why not...
            return;
        }
        table = new Hashtable(size);
        for (int i = 0; i < size; i++) {
            table.put(dictionary.getObjectMapping(KeyKey + i),
                    dictionary.getObjectMapping(ValueKey + i)); 
        }
    }
}   
