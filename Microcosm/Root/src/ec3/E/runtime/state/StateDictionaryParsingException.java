package ec.state;

/**
 * Exception thrown when an error occurs parsing a String into a
 * graph of StateDictionaries. The reason is available as the
 * string passed into the constructor.
 *
 * @see ec.state.StateDictionary
 */
public class StateDictionaryParsingException extends Exception
{
    /**
     * Constructor for exception
     *
     * @param reason The reason for the Exception.
     */
    StateDictionaryParsingException(String reason) {
        super(reason);
    }
}   
