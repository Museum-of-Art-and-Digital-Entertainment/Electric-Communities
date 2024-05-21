package dom.lilbuddies;

import dom.id.*;
import dom.net.*;
import dom.session.*;
import java.io.IOException;
import dom.container.*;

import java.awt.Frame;

public class TestClientSession {

    public static final void main(String args[])
    {
        if (args.length < 1) {
            System.out.println("Usage:  java dom.lilbuddies.TestClientSession <your name>");
            return;
        }
        ChatClientView view = null;
        System.out.println("Created client view");
        DObjectID id1 = null;
        DObjectID id2 = null;
        try {
            LilBuddiesFrame aFrame = new LilBuddiesFrame();
            view = new ChatClientView(SessionViewID.makeNewID((String) null), aFrame, args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Joining group...");
        try {
            view.joinMyGroup(SessionViewID.makeNewID("http://localhost:6008"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Waiting 10 seconds...");
        try {
            Thread.sleep(10000);
        } catch (Exception e) {}

    }
}