package dom.tests;

import dom.session.*;
import dom.id.*;
import java.net.URL;

/**
 * Test view class.  Just exposes some protected methods to test code
 * that is declared outside of the dom.session package.
 *
 * @author Scott Lewis
 * @see ClientView
 */
public class TestClientView extends ClientView
{

    public TestClientView(SessionViewID id)
    {
        super(id);
    }

    public DObjectID createDObject(DObject creator,
                                   DObjectID id,
                                   SessionViewID homeViewID,
                                   URL codebase,
                                   String className,
                                   Object param,
                                   boolean activate)
        throws Exception
    {
        return super.createDObject(creator, id, homeViewID, codebase, className, param, activate);
    }
    
    public void joinSession(SessionViewID target)
        throws Exception
    {
        super.joinGroup(target);
    }
    
    public void leaveSession()
        throws Exception
    {
        super.leaveGroup();
    }
    
    public DObject getDObjectForID(DObjectID id)
    {
        return super.getDObjectForID(id);
    }
}

