package ec.e.timer;

public class Timer 
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Timer class");
    }
    public static void Ping(int millis) {}
}

public class ClockController
{
  static {
      throw new ExceptionInInitializerError("Loaded dummy ClockController class");
  }
  static ClockController cc = new ClockController();
  static Clock cl = new Clock();
  
    public static ClockController TheSmashingClockController() {
        return cc;
    }
    public static ClockController TheQuakeProofClockController() {
        return cc;
    }
    public Clock newClock(long interval, ETickHandling_$_Intf handler, Object arg) {
      return cl;
    }
}   

public class Clock
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Clock class");
    }

    public void start() {
    }

    public void terminate() {
    }
}   

public interface ETickHandling
extends ETickHandling_$_Intf
{
}

public interface ETickHandling_$_Intf
extends EObject_$_Intf
{
}   

public interface ETickHandling_$_Impl
{
}

