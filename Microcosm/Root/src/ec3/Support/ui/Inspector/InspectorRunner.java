package ec.ui;
import netscape.application.*;
import ec.ifc.app.ECApplication;

/**

 * We need a subclass of thread that knows what we want it to do
 * when we ask it to run().

 */

class InspectorRunner extends Thread {
    private ECApplication application;
    private boolean needToRunApplication;   
    
    InspectorRunner () {
        Object synchronizer = ECApplication.TheApplicationSynchronizer;
        synchronized(synchronizer)  {       
            Application theApp = Application.application();
            if (theApp == null)  {
                application = new ECApplication();
                needToRunApplication = true;
            }
            else {
                needToRunApplication = false;                
                try {
                    application = (ECApplication)theApp;
                } catch (ClassCastException e) {
                    System.err.println("Error in Inspector init - IFC Application is already running, but it is not an ECApplication");
                }
            }
        }
    }

    public void run () {
        if (needToRunApplication)  {
            application.run();
        }
    }
}

