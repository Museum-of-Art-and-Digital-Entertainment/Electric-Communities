package ec.e.inspect;

import java.io.PrintWriter;

public class TraceInfoBackref
extends TraceInfo
{
    public final TraceInfo ref;

    public TraceInfoBackref(TraceInfo orig) {
        super(orig.address, orig.refKind);
        ref = orig;
    }

    public void println(PrintWriter pw, int indent) {
        printAllButLabel(pw, indent);
        pw.print(" [ref ");
        pw.print(ref.label);
        pw.println("]");
    }
}
