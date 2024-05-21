package ec.ifc.app;

import ec.e.boot.EBoot;
import ec.util.EThreadGroup;
import netscape.application.Application;

public class IFCBoot
{

    public static void main (String args[])  {
        EThreadGroup.callEMain("ec.ifc.app.IFCBoot", args);
    }
    
    public static void EMain (String args[])  {
        Object object = new Object();
        Thread thread = new Thread(new IFCThreadRunnable(object, new ECApplication()));
//        System.out.println("IFCBoot created Application: " + Application.application());
        thread.start();
        waitForAppSet(object);
        try {       
            EBoot.EMain(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static void waitForAppSet (Object object)  {
        synchronized(object)  {
            try {
                object.wait(0);
            } catch (Exception e) {
            }
        }
//        System.out.println("IFCBoot - Application started");
    }       
}

class IFCThreadRunnable implements Runnable
{
    private Object waiter;
    private ECApplication application;
    
    IFCThreadRunnable (Object waiter, ECApplication application)  {
        this.waiter = waiter;
        this.application = application;
    }   
    
    public void run ()  {
        synchronized(waiter) {
            try {
                waiter.notify();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        application.run();      
    }   
}       
