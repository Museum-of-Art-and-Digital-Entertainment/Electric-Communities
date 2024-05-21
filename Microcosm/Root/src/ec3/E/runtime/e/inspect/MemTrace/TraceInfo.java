package ec.e.inspect;

import java.io.PrintWriter;

public class TraceInfo
{
    public final int address;
    public final int refKind;
    public final String asString;
    public String label = null;

    public TraceInfo(int a, int rk) {
        address = a;
        refKind = rk;
        Object theObj = Tracer.objectify(a);
        try {
            if (theObj != null) {
                asString = theObj.toString();
            } else {
                asString = "null";
            }
        } catch (Exception e) {
            System.out.println("Unprintable");
            asString = "#<unprintable>";
            // asString = "#<unprintable " + theObj.getClass() + ">";
        }
    }

    public void println(PrintWriter pw) {
        println(pw, 0);
    }

    public void println(PrintWriter pw, int indent) {
        printAllButLabel(pw, indent);
        if (label != null) {
            pw.print(" [is ");
            pw.print(label);
            pw.print("] ");
        }
        pw.println();
    }

    public void printAllButLabel(PrintWriter pw, int indent) {
        while (indent > 0) {
            pw.print("  ");
            indent--;
        }
        pw.print(Tracer.kindString(refKind));
        Object o = Tracer.objectify(address);
        if (o == null) {
            pw.print(" <null>");
        } else {
            pw.print(" ");
            pw.print(o.getClass());
            pw.print(" @ 0x");
            pw.print(Integer.toHexString(address));
            if (o instanceof Class) {
                pw.print(" (");
                pw.print(o.toString());
                pw.print(")");
            }
            pw.print(":");
        }
    }

    public boolean equals(Object other) {
        try {
            TraceInfo ti = (TraceInfo) other;
            return (address == ti.address);
        } catch (ClassCastException ignored) {
            return false;
        }
    }

    public int hashCode() {
        return address;
    }
}

