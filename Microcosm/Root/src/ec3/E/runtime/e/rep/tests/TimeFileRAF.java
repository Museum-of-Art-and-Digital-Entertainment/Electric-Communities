// Small utility for timing how long it takes to load all images and appearances
// from files (as opposed to getting them from the repository).

// Harry Richardson - 18/11/97

package ec.e.rep;

import ec.cosm.gui.appearance.*;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.io.File;
import ec.util.Native;
import java.util.Vector;
import java.util.Enumeration;

public class TimeFileRAF {
    public static final int CRADD_FLAG   = 1;
    public static final int CRAPP2D_FLAG = 2;
    public static final int CRAPP3D_FLAG = 3;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax is: TimeFile <filename>, where <filename>" +
                " is an images.cur type file");
            System.exit(-1);
        }

        TimeFileRAF file_test = new TimeFileRAF();
        file_test.go(args[0]);
    }   

    public void go(String file_list) {
        int token_flag = 0;
        
        int binary_count = 0;
        int appearance2d_count = 0;
        int appearance3d_count = 0;

        long binary_time = 0;
        long appearance2d_time = 0;
        long appearance3d_time = 0;
        Vector binFileNames = new Vector(2000);
        Vector app2FileNames = new Vector(1000);
        Vector app3FileNames = new Vector(500);

        Native.initializeTimer();
    
        try {
            FileReader input = new FileReader(file_list);
            StreamTokenizer st = new StreamTokenizer(input);
        
            st.ordinaryChar('/');
            st.nextToken();

            // Loop over the file until we're out of tokens...
            while(st.ttype != StreamTokenizer.TT_EOF) {
                if (st.ttype == StreamTokenizer.TT_WORD || st.ttype == '"') {
                    try {
                        switch(token_flag) {
                            case CRADD_FLAG:
                                if ((st.sval.toLowerCase()).endsWith(".bmp")) {
                                  binFileNames.addElement(st.sval);
                                }
                                break;
                            case CRAPP2D_FLAG: 
                                app2FileNames.addElement(st.sval);
                                break;
                            case CRAPP3D_FLAG: 
                                app3FileNames.addElement(st.sval);
                                break;
                        }
                    } catch (Exception e) {
                        // Ignore - bad data file...
                    }
                    token_flag = 0;
                    if (st.sval.equals("cradd"))  token_flag  = CRADD_FLAG;
                    if (st.sval.equals("crapp2d")) token_flag = CRAPP2D_FLAG;
                    if (st.sval.equals("crapp3d")) token_flag = CRAPP3D_FLAG;
                }
                st.nextToken();
            }
            input.close();

            getBinarys(binFileNames);
            //            getAppearance2Ds(app2FileNames);
            //            getAppearance3Ds(app3FileNames);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBinarys(Vector filenameList) {
        // System.out.println("Binary: " + file);
        System.out.println("Loading BMP's w/ RAF("+filenameList.size()+"): ");
        long deltaT = 0;
        float count = 0;
        Enumeration filenames = filenameList.elements();
        while (filenames.hasMoreElements()) {
          String file = (String)filenames.nextElement();
          try {
            long time = Native.queryTimer();
            File f = new File(file);
            //            FileInputStream fis = new FileInputStream(f);
            RandomAccessFile fis = new RandomAccessFile(f, "r");
            byte[] b = new byte[(int) f.length()];

            fis.read(b);

            fis.close();
            deltaT += Native.queryTimer() - time;
            count++;
          } catch (Exception e) {
            // Don't really care what's wrong - just throw...
          }
        }
        System.out.println("   Done loading ("+count+") BMP's: "+
                           deltaT/1000 +" mSeconds, "+
                           ((1000000.0*count)/deltaT) +" obj/sec");
    }

    private void getAppearance2Ds(Vector filenameList) {
        // System.out.println("Appearance2D: " + file);

        System.out.println("Loading app2D's ("+filenameList.size()+"): ");
        FileReader input = null;
        long deltaT = 0;
        float count = 0;
        Enumeration filenames = filenameList.elements();
        while (filenames.hasMoreElements()) {
          String file = (String)filenames.nextElement();
          try {
            long time = Native.queryTimer();
            input = new FileReader(file);
            StreamTokenizer st = new StreamTokenizer(input);
            Appearance2D.getFromTextFile(st, null, null);
            input.close();
            deltaT += Native.queryTimer() - time;
            count++;
          } catch (Exception e) {
            System.out.println(e.getMessage()+": "+file);
            if (input != null) {    
                try {
                    input.close();
                } catch (IOException dummy) {}
            }
          }
        }
        System.out.println("   Done loading ("+count+") app2D's: "+
                           deltaT/1000 +" mSeconds, "+
                           ((1000000.0*count)/deltaT) +" obj/sec");
    }                                                   

    private void getAppearance3Ds(Vector filenameList) {
        // System.out.println("Appearance3D: " + file);

        System.out.println("Loading app3D's ("+filenameList.size()+"): ");
        FileReader input = null;
        long deltaT = 0;
        float count = 0;
        Enumeration filenames = filenameList.elements();
        while (filenames.hasMoreElements()) {
          String file = (String)filenames.nextElement();
          try {
            long time = Native.queryTimer();
            input = new FileReader(file);
            StreamTokenizer st = new StreamTokenizer(input);
            Appearance3D.getFromTextFile(st, null, null);
            input.close();
            deltaT += Native.queryTimer() - time;
            count++;
          } catch (Exception e) {
            if (input != null) {    
                try {
                    input.close();
                } catch (IOException dummy) {}
            }
          }
        }
        System.out.println("   Done loading ("+count+") app3D's: "+
                           deltaT/1000 +" mSeconds, "+
                           ((1000000.0*count)/deltaT) +" obj/sec");
    }
}






