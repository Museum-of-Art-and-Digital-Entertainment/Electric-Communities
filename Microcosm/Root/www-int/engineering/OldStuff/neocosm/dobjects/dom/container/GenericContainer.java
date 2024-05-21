package dom.container;

import dom.session.*;
import dom.id.*;
import java.io.Serializable;
import java.net.URL;

// XXX TESTING
public class GenericContainer extends ComponentDObject

{
    
    BaseContainer myContainer;
    
    public GenericContainer(ViewDObjectFacet viewFacet,
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
            myContainer = new BaseContainer(this, "myContainer");
            addComponent("myContainer", myContainer);
        } catch (Exception e) {
            debug("<init>.  Couldn't create container");
        }
        
    }
    
    protected DObjectFacet getDefaultDObjectFacet(DObjectID requestor)
    {
        return myContainer.getFacetForComponent(requestor, null);
    }

}