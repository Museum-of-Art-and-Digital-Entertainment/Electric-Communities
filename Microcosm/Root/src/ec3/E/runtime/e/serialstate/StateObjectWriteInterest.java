package ec.e.serialstate;

public interface StateObjectWriteInterest  {
  /**
   * Method that is called by StateOutputStream when a given object
   * is written to the output stream
   * @param stream the StateOutputStream where the Object is being written
   * @param obj the Object being written
   */
   public void objectToBeWritten(StateOutputStream ostream, Object obj);
}

