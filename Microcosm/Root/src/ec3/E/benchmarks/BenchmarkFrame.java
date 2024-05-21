/*  Add a frame around the Benchmark Applet so it can be run as an application
 *  Brian Marick for Electric Communities - March 1997 
 */

import java.awt.*;

class BenchmarkFrame extends Frame
{
  BenchmarkFrame(String title)
  {
    super(title);
  }

  public boolean handleEvent(Event e)
  {
    if (e.id == Event.WINDOW_DESTROY)
      {
    System.exit(0);
      }
    return super.handleEvent(e);
  }
}
