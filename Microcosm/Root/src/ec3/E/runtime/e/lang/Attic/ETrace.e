package ec.e.lang;

import java.util.Vector;
import ec.e.file.EStdio;

public interface RtTrace
{
    void $tr (String line);
    void $dump (String dumpLabel);
    void $clear ();
}

public eclass ETrace implements RtTrace
{
    private String label;
    private Vector lines = new Vector ();
    
    public ETrace (String l)
    {
        label = l;
    }

    public ETrace ()
    {
        this ("<no label>");
    }

    local void $tr (String line)
    {
        lines.addElement (line + " [Java]");
    }

    private void actualDump (String dumpLabel)
    {
        System.err.println ("Trace of " + label + " at " + dumpLabel + ":");
        int sz = lines.size ();
        for (int i = 0; i < sz; i++)
        {
            System.err.println ("  " + lines.elementAt (i));
        }
        lines.addElement ("dump at " + dumpLabel);
    }

    local void $dump (String dumpLabel)
    {
        actualDump (dumpLabel + " [Java]");
    }

    local void $clear ()
    {
        lines.removeAllElements ();
    }

    emethod tr (String line)
    {
        lines.addElement (line + " [E]");
    }

    emethod dump (String dumpLabel)
    {
        actualDump (dumpLabel + " [E]");
    }

    emethod clear ()
    {
        $clear ();
    }
}
