import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import ec.util.Native; 

class EchoConnection implements Runnable {
   byte[] myBuffer;
   InputStream in;
   OutputStream out;
   int myBufSize;

   public EchoConnection(Socket s, int bufSize) {
      System.out.println("New EchoConnection, buffer size is " + bufSize);
      myBuffer = new byte[bufSize];
      myBufSize = bufSize;
      try {
         in  = s.getInputStream();
         out = s.getOutputStream();
         Thread t = new Thread(this);
         t.start();
      }
      catch(IOException e) {
          System.out.println("EchoConnection error: " + e);
      }
   }

   public void run()
   {
       // grab everything the client sends us and turn it right
       // back around.
       try {
           int bytesRead = 0;

           while(true) {
              if((bytesRead = in.read(myBuffer, 0, myBufSize)) == -1)
                 break;
              out.write(myBuffer, 0, bytesRead);
           }
      }
      catch(IOException e) {
          System.out.println("EchoConnection run error: " + e);
      }
   }
}

class Server {
     ServerSocket mySocket;
     int myPort;
     boolean isDone = false;
     int myBufSize = 2048; 

     public Server(int bufSize) {
         System.out.println("New Server");
         myBufSize = bufSize;
         try {
             mySocket = new ServerSocket(0);
             myPort = mySocket.getLocalPort();
             System.out.println("Listening on port: " + myPort);
         }
         catch (Exception e) {
             System.out.println("Exception while creating ServerSocket: " + e);
         }

         // handle connections
         while(!isDone) try {
            Socket newSocket = mySocket.accept();
            new EchoConnection(newSocket, myBufSize);
         }
         catch(IOException e) {
             System.out.println("Exception while handling new connection: " + e);
         }
     }
}

public class JavaServer {

    public static void main (String[] argv) {
        Server s;
        if (argv.length > 0) {
            s = new Server(new Integer(argv[0]).intValue());
        } else { 
            s = new Server(2048);
        }
    }
}
