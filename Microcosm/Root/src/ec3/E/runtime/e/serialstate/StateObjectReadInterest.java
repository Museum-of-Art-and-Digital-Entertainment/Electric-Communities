package ec.e.serialstate;

public interface StateObjectReadInterest  {
  /**
   * This method is called by the StateInputStream when a given object
   * is actually read from the input stream given as first parameter
   *
   * @param stream the StateInputStream that is calling this method on us
   * @param obj the Object that was read from input stream
   */
  public void objectToBeRead(StateInputStream istream, Object obj);
}
