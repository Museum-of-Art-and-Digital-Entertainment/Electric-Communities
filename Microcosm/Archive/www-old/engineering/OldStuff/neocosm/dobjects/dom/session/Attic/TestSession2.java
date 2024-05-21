package dom.session;

import dom.id.*;
import dom.net.*;
import java.io.IOException;
import dom.container.*;

public class TestSession2 {


    public static final void main(String args[])
    {
        ClientView view = null;
        System.out.println("Created second client view");
        try {
            view = new ClientView(SessionViewID.makeNewID((String) null));
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
            view.joinGroup(SessionViewID.makeNewID("http://localhost:6000"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Second test session waiting 60 seconds...");
        try {
            Thread.sleep(60000);
        } catch (Exception e) {}

    }
}