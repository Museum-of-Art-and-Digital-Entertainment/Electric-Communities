package ec.e.run.test;

import ec.e.run.Trace;
import ec.e.run.TraceController;
import java.util.Properties;
import java.util.Hashtable;


/**
 * Check how well replacing or deleting full log files works.
 * See =README=
 */
class Fill
{
    static final int overwriteAction = 0;
    static final int defaultAction = 1;
    static final int renameAction = 2;
    static final int otherOverwriteAction = 3;

    public static void main (String[] args) {
        
        int action;
        if (args.length == 0) {
            System.out.println("Going to do the default (add new backups).");
            action = defaultAction;
        } else if (args[0].equals("one")) {
            System.out.println("Going to overwrite the backup file.");
            action = overwriteAction;
        } else if (args[0].equals("1")) {
            System.out.println("Going to overwrite the backup file.");
            action = otherOverwriteAction;
        } else if (args[0].equals("many")) {
            System.out.println("Going to add new backups.");
            action = renameAction;
        } else {
            action = 234;
            System.out.println("Duh?");
            System.exit(1);
        }
        
        Properties props = new Properties();
        switch(action) {
            case overwriteAction:
                props.put("TraceLog_backups", "one");
                System.out.println("A single backup file.");
                break;
            case otherOverwriteAction:
                props.put("TraceLog_backups", "1");
                System.out.println("A single backup file.");
                break;
            case defaultAction:
                System.out.println("Multiple backup files.");
                break;
            case renameAction:
                props.put("TraceLog_backups", "many");
                System.out.println("Multiple backup files.");
                break;
        }

        props.put("TraceLog_write", "true");
        props.put("TraceLog_size", "30000");
        props.put("TraceLog_tag", "ECLog");
        
        Trace.comm.worldm("Expect log to file with date in it.");
        TraceController.start(props);
        new Fill().go();

        Trace.comm.worldm("That's all, folks - end of test.");
    }

    void go() {
        Object obj = new Hashtable(3);
        String s = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        for (int i = 0; i < 1000; i++) {
            Trace.vat.worldm(i + " is " + s, obj);
        }
    }
}

