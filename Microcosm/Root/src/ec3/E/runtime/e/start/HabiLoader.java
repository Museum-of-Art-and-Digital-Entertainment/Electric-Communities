package ec.e.run;

import ec.vcache.ClassCache;
import ec.util.Native;
import java.io.*;
import java.util.Hashtable;

public class HabiLoader extends Thread {
    private static final boolean DEBUG = true;
    private String fileName;
    private BufferedReader myLoadListReader = null;
    private BufferedReader myScreenListReader = null;
    private BufferedWriter myOutListWriter = null;
    private Hashtable myScreenTable = null;
    private long totalTime = 0;
    private java.util.Vector v = new java.util.Vector(2300);
    private static boolean noInstance = true;
    static Trace tr_timer = new Trace("StartupTimer");
    static Trace tr = new Trace("HabiLoader");

    private HabiLoader(String inFileName,
                       String screenFileName,
                       String outFileName) throws IOException {
        setPriority(3);
        this.fileName = fileName;
        myLoadListReader =
          new BufferedReader(new FileReader(inFileName));
        if (screenFileName != null) {
          myScreenListReader =
            new BufferedReader(new FileReader(screenFileName));
          String className = null;
          while ((className=myScreenListReader.readLine()) != null) {
            if (myScreenTable == null) {
              myScreenTable = new Hashtable();
            }
            myScreenTable.put(className, className);
          }
          myScreenListReader.close();
        }
        if (outFileName != null) {
          myOutListWriter =
            new BufferedWriter(new FileWriter(outFileName));
        }
    }

    public static
    HabiLoader oneTimeInstance(String inFileName) throws IOException {
        if (noInstance) {
          noInstance = false;
        } else {
          throw new RuntimeException("HabiLoader instance already exists");
        }
        return (new HabiLoader(inFileName, null, null));
    }

    public static void main(String argv[]) {
      try{
        String screenFileName = null;
        String inFileName = null;
        String outFileName = null;
        for (int i=0; i < argv.length; i++) {
          if ("-o".equals(argv[i])) {  // Output file
            outFileName = argv[++i];
          } else if ("-s".equals(argv[i])) {
            screenFileName = argv[++i];
          } else {
            inFileName = argv[i];
          }
        }
        HabiLoader hl =
          new HabiLoader(inFileName, screenFileName, outFileName);
        hl.run();
        System.exit(0);
      } catch (IOException ioe) {
        System.exit(0);
      }
    }

    public void readClasses() throws IOException {
        String line;
        int i = 0;
        long stime = 0;
        Class c;
        Object o;
        v.setSize(2300);
        long ltime = Native.queryTimer();

        try {
            while ((line = myLoadListReader.readLine()) != null) {
              if ((myScreenTable == null) ||
                  (!myScreenTable.containsKey(line))) {
                try {
                    stime = Native.queryTimer();
                    c = ClassCache.forName(line);
                    if (tr.debug && Trace.ON) {
                      tr.debugm("Class: "+ line +
                                ", loaded by classloader: "+
                                c.getClassLoader());
                    }
                    totalTime += Native.deltaTimerMSec(stime);
                    v.setElementAt(c,i);
                    i++;
                    if (myOutListWriter != null) {
                      myOutListWriter.write(line);
                      myOutListWriter.newLine();
                    }
                } catch (java.lang.Exception e) {
                  if (DEBUG) {
                    System.out.println("Excluding ("+line+
                              "): Exception while loading: "+e.getMessage());
                  }
                } catch (java.lang.Error e) {
                  if (DEBUG) {
                    System.out.println("Excluding ("+line+
                              "): Error while loading: "+e.getMessage());
                  }
                }
              } else {
                if (DEBUG) {
                  System.out.println("Excluding ("+line+"): In exclusion list");
                }
              }
//               if (i % 600 == 0) {
//                 setPriority(3);
//               }

//               if (i % 1700 == 0) {
//                 setPriority(4);
//               }

              if (i % 100 == 0) {
                if(tr_timer.debug && Trace.ON) {
                  tr_timer.debugm(i + " classes loaded so far...("+
                                  totalTime+" ms)");
                }
              }
            }
        } finally {
          myLoadListReader.close();
          if (myOutListWriter != null) {
            myOutListWriter.close();
          }
          long looptime = (Native.queryTimer()-ltime)/1000;
          if(tr_timer.debug && Trace.ON) {
            tr_timer.debugm(i + " classes were loaded, sum time ("+
                            totalTime+" ms)"+", Loop time ("+looptime+" ms)");
          }
        }
    }

    public void run() {
        try {
            this.readClasses();
        } catch (IOException e) {}
        if (tr_timer.debug && Trace.ON) {
          tr_timer.debugm("*** Done Loading Classes ***");
        }
    }
}












