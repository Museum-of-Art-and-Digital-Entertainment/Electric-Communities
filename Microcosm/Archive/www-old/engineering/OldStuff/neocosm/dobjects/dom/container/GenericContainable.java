package dom.container;

import dom.session.*;
import dom.id.*;
import java.io.Serializable;
import java.net.URL;

// XXX TESTING
public class GenericContainable extends ComponentDObject

{

    DObjectID myContainerID;
    
    public GenericContainable(ViewDObjectFacet viewFacet,
                               DObjectID newID,
                               SessionViewID homeID,
                               URL codeBase,
                               Serializable params) throws InstantiationException
    {
        super(viewFacet, newID, homeID, codeBase, params);
    }

    /**
     * Initialize this instance.  This is called in the DObject constructor, and
     * it allows subclasses to call arbitrary initialization code.  The implementation
     * for this class is to do nothing, as all critical initialization is done
     * within the DObject constructor
     *
     * @exception InstantiationException can be thrown if some problem with
     * initialization
     */
    protected void initialize() throws InstantiationException
    {
        super.initialize();
        try {
            addComponent("myContainable", new BaseContainable(this, "myContainable"));
        } catch (Exception e) {
            debug("<init>.  Couldn't create containable");
        }
    }
    
    protected void receiveCapability(DObjectFacet aFacet)
    {
        debug("GenericContainable.receiveCapability.  Facet provided is "+aFacet);
        if (aFacet instanceof BaseContainerFacet) {
            myContainerID = aFacet.getID();
        }
        notifyComponentsOfFacet(aFacet);
    }

    protected DObjectFacet getDefaultDObjectFacet(DObjectID requestor)
    {
        return ((BaseContainable) getComponentForIdentifier("myContainable")).getFacetForComponent(requestor, null);
    }
    
    public void testAddStart()
    {
        if (!isClient()) {
            debug("GenericContainable.testStart.  Starting add test");
            try {
                ((BaseContainable) getComponentForIdentifier("myContainable")).addToContainer(myContainerID, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void testRemoveStart()
    {
        if (!isClient()) {
            debug("GenericContainable.testStart.  Starting remove test");
            try {
                ((BaseContainable) getComponentForIdentifier("myContainable")).removeFromContainer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
            
    
}
