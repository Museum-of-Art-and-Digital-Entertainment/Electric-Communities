package ec.state;

/**
 * Interface for Objects that can be mapped in a StateDictionary.
 *
 * @see ec.state.ReadableStateDictionary
 * @see ec.state.StateDictionary
 * @see ec.state.WriteableStateDictionary
 */
public interface Stateful
{
    /** Encode Object state */
    void encodeState (WriteableStateDictionary dictionary);
    
    /** Decode preface, possibly returning a different (Stateful) Object */ 
    Stateful decodePrefaceState (ReadableStateDictionary dictionary);
    
    /** Decode Object's state */    
    void decodeBodyState (ReadableStateDictionary dictionary);
}   

