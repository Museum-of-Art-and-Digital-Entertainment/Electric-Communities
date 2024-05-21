/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.describe;

import java.util.Vector;
import java.util.Hashtable;
import ec.cosm.ingredients.property.ikProperty$kind;
import ec.cosm.objects.Global;
import ec.cosm.objects.ikVerbManager$kind;
import ec.cosm.objects.jiPropertyManagerFacet;
import ec.cosm.objects.jiPropertyPeer;
import ec.cosm.objects.jiPropertyRegistration;
import ec.cosm.objects.jiVerbHandler;
import ec.cosm.objects.jiVerbManager;
import ec.cosm.objects.jiVerbPeer;
import ec.cosm.objects.jiVerbPeer;
import ec.cosm.objects.jMenuEntry;
import ec.cosm.objects.jPieMenuRecord;
import ec.cosm.objects.jVerbContext;
import ec.cosm.ui.presenter.AlertPresenter;
import ec.cosm.ui.presenter.PSProperty;
import ec.cosm.ui.presenter.PSPropertyException;
import ec.e.lang.EString;
import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;

einterface simpleDescribeAlertHandler {
  describeAlertWasCancelled(AlertPresenter presenter);
}

public interface iiSimpleDescriber$iijif
{
    void setShortDescription(String shortDescription);
    void setDescription(String description);
    String getShortDescription();
    String getDescription();
    jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorRecord, jVerbContext context);
    void getProperties(EResult result);
    void propose(PSProperty property, EResult result);
    void updateClientState();
}

public eclass iiSimpleDescriber$iicode
extends Ingredient
implements IngredientJif, iskDescriber$kind, iiSimpleDescriber$iijif, jiDescriber, simpleDescribeAlertHandler, jiVerbPeer, jiPropertyPeer
{
    jiPropertyManagerFacet myPropertyManagerFacet;
    private static final Trace theTrace =  new Trace("describe");
    AlertPresenter myAlertPresenter;
    public static final int DESCRIBE_PROPERTY =  0;

    /* State bundle 'myState' */
    private ec.cosm.ingredients.describe.istDescriber myState;
    /* Neighbors */
    ikVerbManager$kind iinVerbManager;
    ikProperty$kind iinProperty;

    public iiSimpleDescriber$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istDescriber)state);
    }

    public Object jiGetClientState() {return(myState);}
    public void setShortDescription(String shortDescription) {
        myState.theShortDescription = shortDescription;
        updateClientState();
    }
    public void setDescription(String description) {
        myState.theDescription = description;
        updateClientState();
    }
    public String getShortDescription() {
        return (myState.theShortDescription);
    }
    public String getDescription() {
        return (myState.theDescription);
    }
    public jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorRecord, jVerbContext context) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("contextualizing Describe for "+context);
      if (context.amDO()) {
        jMenuEntry entry = new jMenuEntry("COMMAND:DESCRIBE", new jDescribeVerbHandler(this));
        priorRecord.forceEntry(entry);
      }
      return priorRecord;
    }
    public void getProperties(EResult result) {
      Vector properties = new Vector();

      properties.addElement(new PSProperty(DESCRIBE_PROPERTY,
                                           "DESCRIPTION",
                                           myState.theDescription));
      myPropertyManagerFacet.forwardProperties(properties, result);
    }
    public void propose(PSProperty property, EResult result) {
      if (property.id() == DESCRIBE_PROPERTY) {
      
        String newDescription = (String) property.value();
        
        if (newDescription == null) {
          newDescription = new String("This object has no description");
        }

        myState.theDescription = newDescription;
        updateClientState();
        myPropertyManagerFacet.accept(result);
      }
    }
    public void updateClientState() {
      // send the entire state bundle to the clients.
      RtEnvelope env =
        envelope ((pkDescriberClient$kind) <- pUpdateClientState(jiGetClientState()));
      //KSSHack How are we going to do this in Plubar?
      //KSSHack PresenceRouter.sendEnvelopeToOthers(environment.otherPresences, env);
    }
    local void init(istDescriber externalState) {
      if (null == externalState)  {
        myState = new istDescriber(); // no state block provided. Create one.
      } else {
        myState = externalState;    // use provided state block.
      }

      // make sure the state is valid.          
      if (null == myState.theDescription)  {
        myState.theDescription = "Object has no description.";
      }
      
      ((jiVerbManager)iinVerbManager).registerVerbPeer((jiVerbPeer)this); // register
      
      if (IAmTheHost && iinProperty != null) {
        myPropertyManagerFacet = ((jiPropertyRegistration)iinProperty).
                                   registerPropertyPeer((jiPropertyPeer)this);
      }
      
      ((jiVerbManager)iinVerbManager).registerVerbPeer((jiVerbPeer)this);
      
      myAlertPresenter = null;
    }
    local void init() {
      this.init(null);
    }
    emethod iDescribe(Hashtable aoBehaviorFacets) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("Describing!");

    // if there's a describe window on screen from last time, kill it off.
    // It's tricky and probably not worthwhile to try to reuse the
    // existing one, so we'll always make a new one.
    if (myAlertPresenter != null) {
      myAlertPresenter.cancelAlert(); 
    }

      // create a dialog presenter.
      myAlertPresenter = Global.theUIPresenterFactory.createAlertPresenter();
      
      String             noTitles[] = {};
      myAlertPresenter.initPersistent(myState.theShortDescription,
                                      myState.theDescription, 
                                      noTitles);
      // we want to be notified when the alert is closed so 
    // we can null out the reference to the presenter, allowing
    // it to be gc'ed
    RtEnvelope cancelEnvelope 
      = envelope (simpleDescribeAlertHandler <- describeAlertWasCancelled(myAlertPresenter));
    
    myAlertPresenter.postAlert(this, null, cancelEnvelope);
    }
    emethod describeAlertWasCancelled(AlertPresenter cancelledPresenter) {
    // allow the alert presenter to be gc'ed -- we'll make
    // a fresh one next time. Do nothing if the cancelled presenter
    // is no longer the current one
    if (cancelledPresenter == myAlertPresenter) {
      myAlertPresenter = null;
    }
  }
    emethod uShortDescribe(EResult name, EResult thumbnail) {
      name <- forward(new EString(myState.theShortDescription));
      thumbnail <- forward(new EString(myState.theThumbnail));
    }
    emethod pUpdateClientState(Object newState) {
      myState = (istDescriber) newState;
      // Describer is pretty simple, no fancy init required here.
    }
}

    class jDescribeVerbHandler
    extends jiVerbHandler {
    private ikDescriber$kind myDescriber;
    private static final Trace theTrace = new Trace("describeverbhandler");

    
    jDescribeVerbHandler (ikDescriber$kind describer) {
      myDescriber = describer;
    }
    
    public Vector aoBehaviorsNeeded () {
      return createBehaviorVector("urllaunch");
    }
    
    public void performVerb (Hashtable aoBehaviorFacets,
                             Hashtable doBehaviorFacets,
                             Hashtable bodyBehaviorFacets,
                             Object directObject,
                             Object param) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("Calling iDescribe on "+myDescriber);
      // don't even need any facets since this behavior's on us alone
      myDescriber <- iDescribe(aoBehaviorFacets);
    }
  }
