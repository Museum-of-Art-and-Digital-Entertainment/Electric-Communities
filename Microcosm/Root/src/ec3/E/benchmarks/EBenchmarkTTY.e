import ec.e.lang.EInteger;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.net.*;
import java.util.*;

public eclass EBenchmarkTTY implements ELaunchable
{

  emethod go (EEnvironment env) 
  {
    (new BenchmarkTTYThread()).start();
  }
}
 
public class BenchmarkTTYThread extends Thread
{
  public void run()
  {
    BenchmarkTTY b = new BenchmarkTTY();
    b.go();
  }
}    
