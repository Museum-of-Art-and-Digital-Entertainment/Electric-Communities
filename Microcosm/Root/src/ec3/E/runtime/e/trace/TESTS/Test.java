package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;


class Test
{
  
  static Trace subsystemTrace;

  public static void main (String[] args)
  {
    new Setup();
    subsystemTrace = new Trace("Subsystem");

    subsystemTrace.errorm("================================");
    if (subsystemTrace.error) subsystemTrace.errorm("subsystemTrace.error: Test.main");
    if (subsystemTrace.warning && Trace.ON) subsystemTrace.warningm("subsystemTrace.warning: Test.main");
    if (subsystemTrace.world && Trace.ON) subsystemTrace.worldm("subsystemTrace.world: Test.main");
    if (subsystemTrace.usage && Trace.ON) subsystemTrace.usagem("subsystemTrace.usage: Test.main");
    if (subsystemTrace.event && Trace.ON) subsystemTrace.eventm("subsystemTrace.event: Test.main");
    if (subsystemTrace.debug && Trace.ON) subsystemTrace.debugm("subsystemTrace.debug: Test.main");
    if (subsystemTrace.verbose && Trace.ON) subsystemTrace.verbosem("subsystemTrace.verbose: Test.main");

    subsystemTrace.errorm("================================");
    if (Trace.fonts.error) Trace.fonts.errorm("Trace.fonts.error: Test.main");
    if (Trace.fonts.warning && Trace.ON) Trace.fonts.warningm("Trace.fonts.warning: Test.main");
    if (Trace.fonts.world && Trace.ON) Trace.fonts.worldm("Trace.fonts.world: Test.main");
    if (Trace.fonts.usage && Trace.ON) Trace.fonts.usagem("Trace.fonts.usage: Test.main");
    if (Trace.fonts.event && Trace.ON) Trace.fonts.eventm("Trace.fonts.event: Test.main");
    if (Trace.fonts.debug && Trace.ON) Trace.fonts.debugm("Trace.fonts.debug: Test.main");
    if (Trace.fonts.verbose && Trace.ON) Trace.fonts.verbosem("Trace.fonts.verbose: Test.main");

    (new Test()).go();
  }
  

  public void go()
  {
    subsystemTrace.errorm("================================");
    if (subsystemTrace.error) subsystemTrace.errorm("subsystemTrace.error: Test.go");
    if (subsystemTrace.warning && Trace.ON) subsystemTrace.warningm("subsystemTrace.warning: Test.go");
    if (subsystemTrace.world && Trace.ON) subsystemTrace.worldm("subsystemTrace.world: Test.go");
    if (subsystemTrace.usage && Trace.ON) subsystemTrace.usagem("subsystemTrace.usage: Test.go");
    if (subsystemTrace.event && Trace.ON) subsystemTrace.eventm("subsystemTrace.event: Test.go");
    if (subsystemTrace.debug && Trace.ON) subsystemTrace.debugm("subsystemTrace.debug: Test.go");
    if (subsystemTrace.verbose && Trace.ON) subsystemTrace.verbosem("subsystemTrace.verbose: Test.go");

    subsystemTrace.errorm("================================");
    if (Trace.fonts.error) Trace.fonts.errorm("Trace.fonts.error: Test.go");
    if (Trace.fonts.warning && Trace.ON) Trace.fonts.warningm("Trace.fonts.warning: Test.go");
    if (Trace.fonts.world && Trace.ON) Trace.fonts.worldm("Trace.fonts.world: Test.go");
    if (Trace.fonts.usage && Trace.ON) Trace.fonts.usagem("Trace.fonts.usage: Test.go");
    if (Trace.fonts.event && Trace.ON) Trace.fonts.eventm("Trace.fonts.event: Test.go");
    if (Trace.fonts.debug && Trace.ON) Trace.fonts.debugm("Trace.fonts.debug: Test.go");
    if (Trace.fonts.verbose && Trace.ON) Trace.fonts.verbosem("Trace.fonts.verbose: Test.go");

    (new Other()).go(1);
  }
}

