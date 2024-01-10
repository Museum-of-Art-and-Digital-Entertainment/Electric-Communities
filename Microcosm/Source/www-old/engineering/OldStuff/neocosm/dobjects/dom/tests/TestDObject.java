package dom.tests;

import dom.session.*;
import dom.id.*;
import java.net.URL;
import java.io.*;

public class TestDObject extends DObject
{
    DObjectID otherID;
    
    // do nothing
    public TestDObject(ViewDObjectFacet viewFacet, DObjectID myID,
        SessionViewID viewID, URL codebase, Serializable param)
        throws InstantiationException
        {
            super(viewFacet, myID, viewID, codebase, param);
            if (param != null) otherID = (DObjectID) param;
            addDObjectListener();
        }
        
    public void bogus() {}
    
    protected void receiveCapability(DObjectFacet aFacet)
    {
        System.out.println("TestDObject.receiveCapability.  Facet provided is "+aFacet);
    }

}
