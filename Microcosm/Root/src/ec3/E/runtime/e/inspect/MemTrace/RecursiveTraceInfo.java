package ec.e.inspect;

import java.io.PrintWriter;

public class RecursiveTraceInfo
extends TraceInfo
{
    public TraceInfo[] trace = null;

    public RecursiveTraceInfo(TraceInfo orig) {
        super(orig.address, orig.refKind);
    }

    public RecursiveTraceInfo(int addr, int refKind) {
        super(addr, refKind);
    }

    public void println(PrintWriter pw, int indent) {
        super.println(pw, indent);
        if (trace == null) {
            while (indent > 0) {
                pw.print("  ");
                indent--;
            }
            pw.println("<Untraceable>");
            return;
        }
        indent++;
        for (int i = 0; i < trace.length; i++) {
            if (trace[i] != null) trace[i].println(pw, indent);
            else {
                while (indent > 0) {
                    pw.print("  ");
                    indent--;
                }
                pw.println("<untraceable>"); // Make this lower case, just to be different.
            }
        }
    }
}
