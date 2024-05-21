package netscape.constructor;

/** If your Application.application() object implements this
  * interface, you are running inside Constructor.
  *
  */
public interface Constructor {
    /** Returns true, if the application is running in
      * Build or Wire mode of Constructor.
      */
    public boolean inConstructionMode();
}
