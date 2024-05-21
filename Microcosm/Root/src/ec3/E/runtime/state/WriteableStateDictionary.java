package ec.state;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ec.vcache.ClassCache;
import ec.util.NestedException;

/**
 * Internal representation of a StateDictionary. A WriteableStateDictionary
 * is passed directly to a Stateful Object being encoded, but is never handed
 * out to any other Objects. A root level StateDictionary wraps one of these,
 * and any mappings in a StateDictionary to Stateful Objects actually maps to
 * a WriteableStateDictionary. When decoding from a StateDictionary, the
 * Stateful Object being decoded is passed a ReadableStateDictionary which
 * wraps the WriteableStateDictionary actually containing the State.
 *
 * @see ec.state.ReadableStateDictionary
 * @see ec.state.StateDictionary
 * @see ec.state.Stateful
 */
public class WriteableStateDictionary
{
    private Hashtable /* String, String | StateDictionary | StateIndex */ mappings;
    private Vector /* String */ keys;   // So StringRepresentation is in correct order
    private SharedState sharedState;    // Maintains unique mappings on Objects
    private String className;           // Class of Object we represent
    private int index;                  // Unique Object index
    private boolean isRoot;             // Whether or not top level

    final static int AverageInstanceSize = 16;

    /**
     * Add a String mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param value A String representing the value of the mapping.
     */
    public void addStringMapping (String key, String value) {
        keys.addElement(key);
        if (value == null) {
            mappings.put(key, new StateIndex(0));
        }
        else {
            mappings.put(key, "\"" + value + "\"");
        }
    }

    /**
     * Add a boolean mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param value A boolean representing the value of the mapping.
     */
    public void addBooleanMapping (String key, boolean value) {
        keys.addElement(key);
        String string = value ? StateDictionary.TrueValue : StateDictionary.FalseValue;
        mappings.put(key, string);
    }

    /**
     * Add a double mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param value A double representing the value of the mapping.
     */
    public void addDoubleMapping (String key, double value) {
        keys.addElement(key);
        String string = String.valueOf(value);
        mappings.put(key, string);
    }

    /**
     * Add an int mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param value An int representing the value of the mapping.
     */
    public void addIntMapping (String key, int value) {
        keys.addElement(key);
        String string = String.valueOf(value);
        mappings.put(key, string);
    }

    /**
     * Add a Hashtable mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param table A Hashtable representing the value of the mapping. The
     * Hashtable must contain only Strings and Stateful Objects.
     */
    public void addHashtableMapping (String key, Hashtable table) {
        keys.addElement(key);
        if (checkObjectMapping(key, table)) {
            HashtableState hashtableState = new HashtableState(table);
            WriteableStateDictionary dictionary =
                new WriteableStateDictionary(sharedState, hashtableState, table);
            mappings.put(key, dictionary);
        }
    }

    /**
     * Add a Vector mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param vector A Vector representing the value of the mapping. The
     * Vector must contain only Strings and Stateful Objects.
     */
    public void addVectorMapping (String key, Vector vector) {
        keys.addElement(key);
        if (checkObjectMapping(key, vector)) {
            VectorState vectorState = new VectorState(vector);
            WriteableStateDictionary dictionary =
                new WriteableStateDictionary(sharedState, vectorState, vector);
            mappings.put(key, dictionary);
        }
    }

    /**
     * Add a Stateful Object mapping to the StateDictionary.
     *
     * @param key A String representing the key for the mapping.
     * @param stateful A Stateful Object representing the value of the mapping.
     */
    public void addStatefulMapping (String key, Stateful stateful) {
        keys.addElement(key);
        if (checkObjectMapping(key, stateful)) {
            WriteableStateDictionary dictionary =
                new WriteableStateDictionary(sharedState, stateful);
            mappings.put(key, dictionary);
        }
    }

    /**
     * Convert this StateDictionary to a String Representation. The
     * String produced by this method can be parsed back into a graph
     * of StateDictionaries. This String is a deep representation of
     * the whole graph of StateDictionaries.
     *
     * @return The String representation.
     */
    public String toString () {
        StringBuffer buffer = new StringBuffer(50000); // XXX - Tune this
        buffer.append("/*\n\"" + StateDictionary.Header + StateDictionary.Version + "\"\n");
        appendStringRepresentation(buffer, 1);
        buffer.append("*/\n");
        return buffer.toString();
    }

    /**
     * Decodes a StateDictionary into the Object it represents. In so
     * doing, the Object represented is asked to decode itself, which
     * will likely recursively decode the entire graph of StateDictionaries.
     *
     * @return The Stateful Object represented by this StateDictionary.
     */
    /* package */ Stateful getStatefulObject ()  {
        Stateful stateful = null;
        try {
            Class clazz = ClassCache.forName(className);
            stateful = (Stateful)clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NestedException("Can't instantiate Object of Class " + className, e);
        }
        ReadableStateDictionary dictionary = new ReadableStateDictionary(this);
        stateful = stateful.decodePrefaceState(dictionary);
        sharedState.put(index, stateful);
        stateful.decodeBodyState(dictionary);
        if (isRoot) {
            sharedState.clearIndexMappings();
            sharedState.wakeup();
        }
        return stateful;
    }

    /**
     * Private method to precheck Object. If the Object is null,
     * the null marker is mapped. If the Object has already been
     * mapped, the existing unique index for the Object is mapped.
     *
     * @return Whether or not the caller needs to map the
     * Object.
     */
    private boolean checkObjectMapping (String key, Object obj) {
        if (obj == null) {
            mappings.put(key, new StateIndex(0));
            return false; // Caller doesn't need to add Object
        }
        int index = sharedState.get(obj);
        if (index != 0) {
            mappings.put(key, new StateIndex(index));
        }
        return (index == 0); // If zero, caller needs to add
    }

    /**
     * Package private method called by ReadableStateDictionary
     * to get a value mapping for a key. If the value is a
     * StateDictionary, it is decoded into the Stateful Object
     * it represents, and the Object is returned. If the mapping
     * is a unique identifier for a previously mapped Object, the
     * previously mapped Object is returned. If the shared Object
     * has not been decoded yet, null will be returned.
     *
     * @return The value of the mapping.
     */
    /* package */ Object getMapping (String key) {
        Object mapping = mappings.get(key);
        if (mapping instanceof WriteableStateDictionary) {
            WriteableStateDictionary dictionary = (WriteableStateDictionary)mapping;
            mapping = dictionary.getStatefulObject();
        }
        else if (mapping instanceof StateIndex) {
            int index = ((StateIndex)mapping).getIndex();
            if (index == 0) {
                mapping = null;
            }
            else {
                mapping = sharedState.get(index);
            }
        }
        else if (mapping instanceof String) {
            // Cool
        }
        else {
            // XXX - Squawk!
        }
        return mapping;
    }

    /**
     * Appends String description of this StateDictionary to the StringBuffer.
     *
     * @param buffer The buffer containing a representation of
     * a graph of StateDictionaries.
     * @param indent The indent level for this StateDictionary
     */
    private void appendStringRepresentation (StringBuffer buffer, int indent) {
        // XXX - Use shared final String constants for #, {, etc.
        String s = className + " (#" + index + ") {\n";
        buffer.append(s);
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            String key = (String)keys.elementAt(i);
            Object object = mappings.get(key);
            buffer.append(indentation(indent));
            buffer.append("\"" + key + "\"");
            if (object instanceof String) {
                String string = (String)object;
                buffer.append(" = " + string);
                buffer.append("\n");
            }
            else if (object instanceof StateIndex) {
                int index = ((StateIndex)object).getIndex();
                if (index == 0) {
                    buffer.append(" = " + StateDictionary.NullString + "\n");
                }
                else {
                    buffer.append(" = #");
                    buffer.append(index);
                    buffer.append("\n");
                }
            }
            else if (object instanceof WriteableStateDictionary) {
                buffer.append(" = ");
                WriteableStateDictionary dictionary = (WriteableStateDictionary)object;
                dictionary.appendStringRepresentation(buffer, indent + 1);
            }
        }
        buffer.append(indentation(indent-1));
        buffer.append("}\n");
    }

    /**
     * Fills self with a set of key/value mappings parsed from the Parser.
     *
     * @param parser The parser used to get tokens.
     */
    /* package */ void parse (StateDictionaryParser parser) throws StateDictionaryParsingException {
        String key;
        String value;
        int type;
        while (true) {
            key = parser.nextString(true);
            if ("}".equals(key)) {
                return; // Done with this Object
            }
            value = parser.nextValue();
            type = parser.currentValueType();
            switch (type) {
                case StateDictionaryParser.TypeString: {
                    mappings.put(key, value);
                    keys.addElement(key);
                    break;
                }
                case StateDictionaryParser.TypeNull: {
                    mappings.put(key, new StateIndex(0));
                    keys.addElement(key);
                    break;
                }
                case StateDictionaryParser.TypeIndex: {
                    mappings.put(key, new StateIndex((new Integer(value)).intValue()));
                    keys.addElement(key);
                    break;
                }
                case StateDictionaryParser.TypeObject: {
                    int index = parser.nextIndex();
                    parser.nextString(true); // Skip "{"
                    WriteableStateDictionary dictionary =
                        new WriteableStateDictionary(sharedState, value, index, false);
                    dictionary.parse(parser);
                    mappings.put(key, dictionary);
                    keys.addElement(key);
                    break;
                }
                case StateDictionaryParser.TypeError: {
                    throw new StateDictionaryParsingException("Error parsing, key: " +
                        key + ", value: " + value);
                }
                case StateDictionaryParser.TypeEnd: {
                    return;
                }
                default: {
                    throw new StateDictionaryParsingException("Unexpected type while parsing, key: " +
                        key + ", value: " + value + ", type: " + type);
                }
            }
        }
    }

    static final private int QuickIndentMax = 11;
    static final private int IndentIncrement = 4;
    // XXX - Build this up dynamically with IndentIncrement as a Property
    static final private String[] QuickIndents =  {
        "",
        "    ",
        "        ",
        "            ",
        "                ",
        "                    ",
        "                        ",
        "                            ",
        "                                ",
        "                                    ",
        "                                        ",
    };

    /**
     * Returns a String of spaces representing an indentation level.
     *
     * @param indent The indentation level.
     * @return The indented string.
     */
    private String indentation (int indent) {
        if (indent < QuickIndentMax) {
            return QuickIndents[indent];
        }
        int amount = indent * IndentIncrement;
        char chars[] = new char[amount];
        for (int i = 0; i < amount; i++) {
            chars[i] = ' ';
        }
        return new String(chars);
    }

    /**
     * Constructor to create top level WriteableStateDictionary
     *
     * @param obj An Object implementing the Stateful interface that
     * will be encoded into the StateDictionary.
     */
    /* package */ WriteableStateDictionary (Stateful obj) {
        SharedState sharedState = new SharedState(SharedState.Outgoing);
        isRoot = true;
        init(sharedState, obj, obj);
        // Now set ourselves up to be incoming
        sharedState.becomeIncoming();
    }

    /**
     * Constructor to create WriteableStateDictionary to be filled in by parsing
     * a String wrapped in a StateDictionaryParser.
     *
     * @param sharedState The internal SharedState to maintain object identity
     * @param className The name of the class for the Object to be wrapped in the
     * StateDictionary
     * @param index The unique Object identity
     * @param isRoot Indicator of whether or not this is a top level StateDictionary
     */
    /* package */
    WriteableStateDictionary (SharedState sharedState, String className, int index, boolean isRoot) {
        this.sharedState = sharedState;
        this.className = className;
        this.index = index;
        this.isRoot = isRoot;
        mappings = new Hashtable(AverageInstanceSize);
        keys = new Vector(AverageInstanceSize);
    }

    /**
     * Constructor to create a WriteableStateDictionary in a graph of
     * StateDictionaries.
     *
     * @param sharedState The internal SharedState to maintain object identity
     * @param obj An Object implementing the Stateful interface that
     * will be encoded into the StateDictionary. This Object is used for identity lookup.
     */
    private WriteableStateDictionary (SharedState sharedState, Stateful obj) {
        isRoot = false;
        init(sharedState, obj, obj);
    }

    /**
     * Constructor to create a WriteableStateDictionary in a graph of
     * StateDictionaries.
     *
     * @param sharedState The internal SharedState to maintain object identity
     * @param obj An Object implementing the Stateful interface that
     * will be encoded into the StateDictionary.
     * @param identity An Object to be used for identity lookup (to determine if
     * the Object has already been encoded into a StateDictionary). This is used by
     * internal wrappers for Vector and Hashtable.
     */
    private WriteableStateDictionary (SharedState sharedState, Stateful obj, Object identity) {
        isRoot = false;
        init(sharedState, obj, identity);
    }

    /**
     * Initializer for WriteableStateDictionary, called from constructors.
     *
     * @param sharedState The internal SharedState to maintain object identity
     * @param obj An Object implementing the Stateful interface that
     * will be encoded into the StateDictionary.
     * @param identity An Object to be used for identity lookup (to determine if
     * the Object has already been encoded into a StateDictionary). In most cases
     * this is the same as the Stateful Object, but it is different for internal
     * wrappings such as Vector and Hashtable.
     */
    private void init (SharedState sharedState, Stateful obj, Object identity) {
        this.sharedState = sharedState;
        this.index = sharedState.put(identity);
        mappings = new Hashtable(AverageInstanceSize);
        keys = new Vector(AverageInstanceSize);
        className = obj.getClass().getName();
        obj.encodeState(this);
    }
}
