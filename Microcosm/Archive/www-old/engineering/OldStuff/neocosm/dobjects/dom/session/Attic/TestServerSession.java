package dom.session;

import dom.id.*;

public class TestServerSession {


    public static final void main(String args[])
    {
        ServerView view = null;
        try {
            view = new ServerView(SessionViewID.makeNewID("http://localhost:6008"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Created server view");

        /*
        // Code for testing generic container/containable stuff
        
        System.out.println("Creating instance of GenericContainer object");
        DObjectID containerID = null;
        try {
            containerID = view.createDObject(null, null,
                view.getID(), null, "dom.container.GenericContainer", null, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Instance of GenericContainer object created with id "+containerID);
        */
        Listener listen = new Listener(view, 6000);

        System.out.println("Listening for connections on port "+6000+"...");
        listen.start();

        
    }
}