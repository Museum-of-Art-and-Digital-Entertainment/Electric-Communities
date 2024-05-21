import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

import java.text.DecimalFormat;
import ec.util.Native; 

class ReadThread implements Runnable {
     DataInputStream myIn;
     byte[] buffer = new byte[64*1024]; // 64K
     long startTime, delta, stopTime;
     long totalReadTime = 0;
     long totalRTTime = 0;
     double bps;
     long totalBytes = 0;
     Client myMom;

     DecimalFormat myBPSFormat = new DecimalFormat("000.00");
     DecimalFormat myBFormat = new DecimalFormat("00000");
     DecimalFormat myTimeFormat = new DecimalFormat("000000");

     ReadThread(Client mom, DataInputStream in) {
         myIn = in;
         myMom = mom;
         Thread t = new Thread(this);
         t.start();
     }

     public void run() {
         try {
 
             int numMessages = myIn.readInt();
             for (int i = 0; i < numMessages; i++) {
                 int msgSize = myIn.readInt();
                 long msgTime = myIn.readLong();
          
                 int bytesRead = 0; 
                 startTime = Native.queryTimer();
                 myIn.readFully(buffer, 0, msgSize);
                 stopTime = Native.queryTimer();

                 delta = stopTime - startTime;
                 bps = (double) msgSize / (double) (delta);
                 System.out.print("Read: " + myTimeFormat.format(delta) + 
                                    " (" + myBPSFormat.format(bps) + ") " );
                 totalReadTime += delta;

                 // now compute round trip time
                 delta = stopTime - msgTime;
                 bps = (double) msgSize / (double) (delta);
                 System.out.println("RT: " + myTimeFormat.format(delta) +
                                    " (" + myBPSFormat.format(bps) + ")" );
                 totalRTTime += delta;
                 totalBytes += msgSize;
     
             }
             System.out.println("Num Messages        : " + numMessages);
             System.out.println("Total Bytes Sent    : " + totalBytes);
             System.out.println("Total Time to Send  : " + myMom.totalWriteTime);
             System.out.println("Total Time to Read  : " + totalReadTime);
             System.out.println("Ave Bytes/uSec Sent : " + 
                                   ((double)totalBytes/(double)myMom.totalWriteTime));
             System.out.println("Ave Bytes/uSec Read : " + 
                                   ((double)totalBytes/(double)totalReadTime));
             System.out.println("Ave RT uSecs/msg   : " + 
                    (double)totalRTTime / (double)numMessages);
         }
         catch (Exception e) {
             System.out.println("Read: exception: " + e); 
         }
         
     }
}


class Client {

     Socket mySocket;
     int myPort;
     String myHost;
     boolean isDone = false;
     DataOutputStream myOut;
     static final int BUFFER_SIZE = 64 * 1024;
     byte[] buf = new byte[BUFFER_SIZE];
     int numMessages = 100;
     public long totalWriteTime = 0;

     Random myRandom;

     DecimalFormat myBPSFormat = new DecimalFormat("000.00");
     DecimalFormat myBFormat = new DecimalFormat("00000");
     DecimalFormat myTimeFormat = new DecimalFormat("000000");
     

     public Client(String host, int port, long seed) {
         myPort = port;
         myHost = host;
         myRandom = new Random(seed);

         long startTime = 0;
         long stopTime = 0;
         long deltaWrite = 0;
         long deltaRead = 0;
         double bps = 0.0;

         System.out.println("Client trying to connect to " + host + ":" + port);
         try {
             mySocket = new Socket(myHost, myPort);
             ReadThread rt = 
                 new ReadThread(this, new DataInputStream(mySocket.getInputStream()));

             myOut = new DataOutputStream(mySocket.getOutputStream());

             myOut.writeInt(numMessages);

             for (int i = 0; i < numMessages; i++) {

                 int msgSize = (int)(myRandom.nextFloat() * BUFFER_SIZE);

                 myOut.writeInt(msgSize);
                 startTime = Native.queryTimer();
                 myOut.writeLong(startTime);

                 myOut.write(buf, 0, msgSize);
                 deltaWrite = Native.deltaTimerUSec(startTime);
                 totalWriteTime += deltaWrite;
                
                 bps = (double) msgSize / (double) deltaWrite;
                 System.out.println(myBFormat.format(msgSize) + 
                                  " Send: " + myTimeFormat.format(deltaWrite) + 
                                  " (" + myBPSFormat.format(bps) + ") ");
             }

             
         }
         catch (Exception e) {
             System.out.println("Exception" + e);
         }
     }
}

public class JavaClient {

    public static void main (String[] argv) {
        if (argv.length > 0) {
            Client c = null;
            if (argv.length == 3) {
                c = new Client(argv[0], 
                               new Integer(argv[1]).intValue(),
                               new Long(argv[2]).longValue());
            } else if (argv.length == 2) {
                c = new Client(argv[0], new Integer(argv[1]).intValue(), 12345678);
            } else {
                c = new Client("locahost", new Integer(argv[1]).intValue(), 12345678);
            }
        } else { 
            System.out.println("Usage: java JavaClient hostname port [seed]");
        }
    }
}
