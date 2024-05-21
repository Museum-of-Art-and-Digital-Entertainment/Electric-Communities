package dom.lilbuddies;

import dom.id.*;
import dom.net.*;
import dom.session.*;
import java.io.IOException;
import dom.container.*;

public class TestServerSession {

    public static final void main(String args[])
    {
        ChatServerView view = null;
        System.out.println("Creating server view");
        DObjectID id1 = null;
        try {
            view = new ChatServerView(SessionViewID.makeNewID("http://localhost:6008"));
            System.out.println("Created server view");
            id1 = DObjectID.makeNewID();
            System.out.println("Creating instance of new MultipointTextChat object...");
            // Create
            view.createMyDObject(id1, "dom.dobjects.MultipointTextChat");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Created dom.dobjects.MultipointTextChat with id "+id1);
        Listener listen = new Listener(view, 6008);
        System.out.println("Listening for connections on port "+6008+"...");
        listen.start();
    }
}