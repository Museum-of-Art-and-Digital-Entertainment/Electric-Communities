package ec.e.run;

public interface RtTether {
  void invoke (RtSealer message, Object args[], RtExceptionEnv ee);
  void invokeNow (RtSealer message, Object args[], RtExceptionEnv ee);
  boolean encodeMeForDeflector ();
}

