package ec.e.run.test;

import ec.e.run.Trace;

class Other
{
  static Trace subsystemTrace = new Trace("Subsystem");

  Other()
  {
    subsystemTrace.errorm("================================");
    if (subsystemTrace.error) subsystemTrace.errorm("subsystemTrace.error: Other.<init>");
    if (subsystemTrace.warning && Trace.ON) subsystemTrace.warningm("subsystemTrace.warning: Other.<init>");
    if (subsystemTrace.world && Trace.ON) subsystemTrace.worldm("subsystemTrace.world: Other.<init>");
    if (subsystemTrace.usage && Trace.ON) subsystemTrace.usagem("subsystemTrace.usage: Other.<init>");
    if (subsystemTrace.event && Trace.ON) subsystemTrace.eventm("subsystemTrace.event: Other.<init>");
    if (subsystemTrace.debug && Trace.ON) subsystemTrace.debugm("subsystemTrace.debug: Other.<init>");
    if (subsystemTrace.verbose && Trace.ON) subsystemTrace.verbosem("subsystemTrace.verbose: Other.<init>");

    subsystemTrace.errorm("================================");
    if (Trace.fonts.error) Trace.fonts.errorm("Trace.fonts.error: Other.<init>");
    if (Trace.fonts.warning && Trace.ON) Trace.fonts.warningm("Trace.fonts.warning: Other.<init>");
    if (Trace.fonts.world && Trace.ON) Trace.fonts.worldm("Trace.fonts.world: Other.<init>");
    if (Trace.fonts.usage && Trace.ON) Trace.fonts.usagem("Trace.fonts.usage: Other.<init>");
    if (Trace.fonts.event && Trace.ON) Trace.fonts.eventm("Trace.fonts.event: Other.<init>");
    if (Trace.fonts.debug && Trace.ON) Trace.fonts.debugm("Trace.fonts.debug: Other.<init>");
    if (Trace.fonts.verbose && Trace.ON) Trace.fonts.verbosem("Trace.fonts.verbose: Other.<init>");
  }
  

  public void go(int i)
  {
    Trace.fonts.errorm("================================");
    if (subsystemTrace.error) subsystemTrace.errorm("subsystemTrace.error: Other.go");
    if (subsystemTrace.warning && Trace.ON) subsystemTrace.warningm("subsystemTrace.warning: Other.go");
    if (subsystemTrace.world && Trace.ON) subsystemTrace.worldm("subsystemTrace.world: Other.go");
    if (subsystemTrace.usage && Trace.ON) subsystemTrace.usagem("subsystemTrace.usage: Other.go");
    if (subsystemTrace.event && Trace.ON) subsystemTrace.eventm("subsystemTrace.event: Other.go");
    if (subsystemTrace.debug && Trace.ON) subsystemTrace.debugm("subsystemTrace.debug: Other.go");
    if (subsystemTrace.verbose && Trace.ON) subsystemTrace.verbosem("subsystemTrace.verbose: Other.go");

    Trace.fonts.errorm("================================");
    if (Trace.fonts.error) Trace.fonts.errorm("Trace.fonts.error: Other.go");
    if (Trace.fonts.warning && Trace.ON) Trace.fonts.warningm("Trace.fonts.warning: Other.go");
    if (Trace.fonts.world && Trace.ON) Trace.fonts.worldm("Trace.fonts.world: Other.go");
    if (Trace.fonts.usage && Trace.ON) Trace.fonts.usagem("Trace.fonts.usage: Other.go");
    if (Trace.fonts.event && Trace.ON) Trace.fonts.eventm("Trace.fonts.event: Other.go");
    if (Trace.fonts.debug && Trace.ON) Trace.fonts.debugm("Trace.fonts.debug: Other.go");
    if (Trace.fonts.verbose && Trace.ON) Trace.fonts.verbosem("Trace.fonts.verbose: Other.go");
  }
}
