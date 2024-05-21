package ec.e.inspect;

import java.io.PrintWriter;

public class TraceInfoUntraceable
extends TraceInfo
{
    public TraceInfoUntraceable() {
        super(0,0);
    }

    public void println(PrintWriter pw, int indent) {
        printAllButLabel(pw, indent);
        pw.println("[Untraceable]");
    }
}
