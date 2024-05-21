package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;


/**
 * Set up the TraceController, currently with hardwired properties.
 */
class Setup
{
  Setup()
  {
    Properties props = new Properties();
    props.put("TraceLog_write", "true");
    props.put("TraceLog_name","test.txt");
    props.put("TraceLog_trace", "event");
    props.put("TraceLog_default", "error");
    props.put("TraceLog_ec.e.run.test.Test", "verbose");
    props.put("TraceLog_subsystem", "usage");
    props.put("TraceLog_FONTS", "world");
    
    TraceController.start(props);
  }
}

