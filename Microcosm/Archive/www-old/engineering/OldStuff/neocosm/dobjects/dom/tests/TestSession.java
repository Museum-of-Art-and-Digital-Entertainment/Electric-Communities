package dom.tests;

import dom.session.*;
import dom.id.*;
import dom.net.*;
import java.io.IOException;
import dom.container.*;

/**
 * Entry point for testing a client.  This class declares the main method for
 * testing a client view.
 *
 * @author Scott Lewis
 * @see TestClientView
 * @see ClientView
 */
public class TestSession {

    public static final void main(String args[])
    {
        TestClientView view = null;
        System.out.println("Created client view");
        DObjectID id1 = null;
        DObjectID id2 = null;
        try {
            view = new TestClientView(SessionViewID.makeNewID((String) null));
            /*
            id1 = DObjectID.makeNewID();
            //id2 = DObjectID.makeNewID();
            System.out.println("Creating instance of new GenericContainable being created...");
            // Create
            view.createDObject(null, id1,
                view.getID(), null, "dom.container.GenericContainable", null, true);
            System.out.println("Instance of GenericContainable created with id "+id1);
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
        
/*
        try {
            Thread.currentThread().sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            view.createDObject(null, id2,
                view.getID(), null, "dom.container.GenericContainable", id1, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
 */       
        System.out.println("Joining group...");
        try {
            view.joinSession(SessionViewID.makeNewID("http://localhost:6000"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Waiting 10 seconds...");
        try {
            Thread.sleep(10000);
        } catch (Exception e) {}

        // GenericContainable containable = (GenericContainable) view.getDObjectForID(id1);
        
        /*
        System.out.println("Initiating remove...");
        if (containable != null) {
            System.out.println("Initiating remove on containable..."+id1);
            containable.testRemoveStart();
        }

        System.out.println("Waiting 20 seconds...");
        try {
            Thread.sleep(20000);
        } catch (Exception e) {}
                
        System.out.println("Initiating add...");
        containable = (GenericContainable) view.getDObjectForID(id1);
        if (containable != null) {
            System.out.println("Initiating add on containable..."+id1);
            containable.testAddStart();
        }

        System.out.println("Waiting 60 seconds...");
        try {
            Thread.sleep(60000);
        } catch (Exception e) {}
        
        System.out.println("Initiating remove...");
        containable = (GenericContainable) view.getDObjectForID(id1);
        if (containable != null) {
            System.out.println("Initiating remove on containable..."+id1);
            containable.testRemoveStart();
        }
        */
        
        /*
        System.out.println("Waiting 20 seconds...");
        try {
            Thread.sleep(20000);
        } catch (Exception e) {}
                
        */
        System.out.println("Leaving group...");
        try {
            view.leaveSession();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        /*
        System.out.println("Sending DObject message");
        
        try {
            view.sendDataToPresences(new DObjectPacket(new DObjectID(), null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Waiting 5 seconds...");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {}
                
        try {
            view.sendCreateMsg(null, new CreateDObjectData(id, view.getID(), "dom.session.TestDObject", null, null, null, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        */
    }
}