package ec.state;

/**
 * Interface for Objects which want to be notified after all
 * Objects in a StateDictionary graph have been decoded.
 *
 * @see ec.state.StateDictionary
 * @see ec.state.Stateful
 */
public interface StateAwakener extends Stateful
{
    /** 
     * Called after all Objects in StateDictionary graph have been
     * fully decoded.
     */
    void wakeupAfterStateDecoding();
}

