package ec.tools.bdbi;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * BDBI, or, Benchmarks Dan Believes In.
 */
eclass Bdbi
implements ELaunchable
{
    private BufferedReader myIn = 
        new BufferedReader(new InputStreamReader(System.in));

    static private Hashtable TheReports = new Hashtable();

    /** target amount of time to run each benchmark for, in msec */
    static int TheBenchmarkTime = 5000;

    static Benchmark[] TheBenchmarks = {
        new BMethod(),
        new BConstruct(),
        new BTryCatch(),
        new BEmethod(),
        new BEwhen(),
        new BEtryCatch(),
        new BEclosure()
    };

    emethod go(EEnvironment e) {
        this <- doOne();
    }

    emethod doOne() {
        System.out.println("Choose one please:");
        for (int i = 0; i < TheBenchmarks.length; i++) {
            System.out.println("  " + (i+1) + ": " + 
                ((Object)TheBenchmarks[i]).getClass().getName());
        }
        int choice;
        for (;;) {
            System.out.print("> ");
            String line;
            try {
                line = myIn.readLine();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                choice = 0;
                break;
            }
            try {
                choice = Integer.parseInt(line) - 1;
                if ((choice >= -1) && (choice < TheBenchmarks.length)) {
                    break;
                }
            } catch (NumberFormatException e) {
                // ignore and loop
            }
            System.out.println("Bad Choice. Try again.");
        }

        if (choice == -1) {
            System.exit(0);
        }

        TheBenchmarks[choice] <- run(this);
    }

    static void report(String msg) {
        System.out.println("[" + msg + "]");
    }

    static void reportTime(String title, double time) {
        report(title + ": " + timeString(time));
    }

    static void annotateTime(String title, double time) {
        reportTime(title, time);
        TheReports.put(title.intern(), new Double(time));
    }

    static String timeString(double time) {
        time *= 1000;
        int intPart = (int) time;
        time = (time - intPart) * 1000;
        String fracPart = "000" + ((int) time);
        return intPart + "." + fracPart.substring(fracPart.length() - 3) +
            " usec";
    }
}


