/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.property;

import java.util.Hashtable;
import java.util.Vector;
import ec.cosm.objects.Global;
import ec.cosm.objects.ikVerbManager$kind;
import ec.cosm.objects.jiVerbHandler;
import ec.cosm.objects.jMenuEntry;
import ec.cosm.objects.jPieMenuRecord;
import ec.cosm.objects.jiVerbManager;
import ec.cosm.objects.jVerbContext;
import ec.cosm.objects.jiVerbPeer;
import ec.cosm.ui.presenter.PropertySheetInputHandler;
import ec.cosm.ui.presenter.PSProperty;
import ec.cosm.ui.presenter.PSPropertyException;
import ec.cosm.ui.presenter.PropertySheetMessage;
import ec.cosm.ui.presenter.UIPresenterFactory;
import ec.cosm.ui.presenter.PropertySheetPresenter;
import ec.e.file.EStdio;
import ec.e.lang.jObjectFuture;
import ec.e.run.Trace;
import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;
import ec.util.assertion.Assertion;

public interface jiPropertyManager {
    public void report(PSProperty property, int source);
}

public class jPropertyManagerFacet implements jiPropertyManagerFacet {
    private int mySource;
    private jiPropertyManager myTarget;
    public jPropertyManagerFacet(jiPropertyManager target, int source) {
      myTarget = target;
      mySource = source;
    }
    public void report(PSProperty property) {
        if (myTarget != null)
          myTarget.report(property, mySource);
    }

    public void accept(EResult status) {
      status <- forward(etrue);
    }

    public void forwardProperties(Vector properties, EResult result) {
      result <- forward(new jObjectFuture(properties));
    }
}

class jPropertySheetVerbHandler extends jiVerbHandler {

    /** Target property manager */
    protected iskProperty$kind myPropertyManager;


    /** Constructor */
    public jPropertySheetVerbHandler(iskProperty$kind propertyManager) {
      myPropertyManager = propertyManager;
    }


    /** Avatar behaviors needed */
    public Vector aoBehaviorsNeeded () {
      return null; // createBehaviorVector("starttrade");
    }


    /** property manager behaviors needed */
    public Vector propertyManagerBehaviorsNeeded () {
      return null; // createBehaviorVector({"moveto", "putat"});
    }


    /**
     * Callback - actually perform the verb
     */
    public void performVerb(Hashtable aoBehaviorFacets,
                            Hashtable doBehaviorFacets,
                            Hashtable propertyManagerFacets,
                            Object directObject,
                            Object param) {
      myPropertyManager <- iPropertySheet(aoBehaviorFacets, propertyManagerFacets);
    }

}

public interface iiECProperty$iijif
{
    PropertySheetMessage createModifyMessage();
    PropertySheetMessage createShowMessage();
    PropertySheetMessage createReportMessage(PSProperty property);
    jiPropertyManagerFacet registerPropertyPeer(jiPropertyPeer peer);
    void report(PSProperty property, int source);
    jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorMenus, jVerbContext context);
}

public eclass iiECProperty$iicode extends Ingredient
implements IngredientJif, iskProperty$kind, iiECProperty$iijif, PropertySheetInputHandler, jiVerbPeer, jiPropertyManager, jiPropertyRegistration
{
    boolean myFirstPropertyMessageFromUI;
    ikProperty$kind mySaveMessageOrderer;
    PropertySheetPresenter myPresenter;
    boolean myActive;
    Vector myPeers;
    Vector myProperties;
    private static final Trace theTrace =  new Trace("property");

    /* State bundle 'myState' */
    private ec.cosm.ingredients.property.istProperty myState;
    /* Neighbors */
    ikVerbManager$kind iinVerbManager;

    public iiECProperty$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istProperty)state);
    }

    local void setNeighbors(ikVerbManager$kind in_iinVerbManager) {
        iinVerbManager = in_iinVerbManager;
    }

    public Object jiGetClientState() {
      return new istProperty();
    }
    public PropertySheetMessage createModifyMessage() {
      return new PropertySheetMessage(
        PropertySheetMessage.START,
        null,
        null
        );
    }
    public PropertySheetMessage createShowMessage() {
      return new PropertySheetMessage(
        PropertySheetMessage.SHOW,
        null,
        null
        );
    }
    public PropertySheetMessage createReportMessage(PSProperty property) {

      Vector properties = new Vector(1);

      try {
        properties.addElement(property.clone());
        return new PropertySheetMessage(
          PropertySheetMessage.REPORT,
          properties,
          null
          );
      } catch (PSPropertyException e) {
        Assertion.test(
          e.getErrorCode() == PSPropertyException.UNCLONEABLE_TYPE);
        if (theTrace.debug && Trace.ON) {
          theTrace.debugm("iiECProperty createReportMessage exception" +
                "property: " + property + " exception = " + e);
        }
        return null;
      }
    }
    public jiPropertyManagerFacet registerPropertyPeer(jiPropertyPeer peer) {
      // no unregister yet. add to a Vector instance variable that
      // will be used when getting properties. Need a source (int)
      // variable in the property (for routing, source is index
      // to the peer)
      if (myPeers == null)
        myPeers = new Vector();
      myPeers.addElement(peer);
      // TODO note if same ingredient registers twice it gets 2 source, facets
      return new jPropertyManagerFacet(this, myPeers.size() - 1);
    }
    public void report(PSProperty property, int source) {
      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty report enter " +
              "property: " + property + " source = " + source);
      }
      if (!myActive)
        return;
      property.setSource(source);
      PropertySheetMessage msg = createReportMessage(property);
      if (msg == null) {
        return;
      }
      iPropertyInputGateway((EResult)null, msg,
        PropertySheetMessage.WORLD_EVENT);
    }
    void iPropertyInputGateway(EResult propertySheetInputHandler, PropertySheetMessage msg, int source) {
      ikProperty$kind messageOrderer;
      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty enter iPropertyInputGateway enter " +
              "PropertySheetInputHandler: " + propertySheetInputHandler +
              "msg: " + msg +
              "source: " + source);
        }
      if (myFirstPropertyMessageFromUI) {
        iPropertyInput(&messageOrderer, propertySheetInputHandler, msg, source);
        myFirstPropertyMessageFromUI = false;
      } else {
        mySaveMessageOrderer <- iPropertyInput(&messageOrderer,
          propertySheetInputHandler, msg, source);
      }
      mySaveMessageOrderer = messageOrderer;
    }
    Vector createTestProperties() {
        Vector result = new Vector();

        boolean readOnly = false;

        result.addElement(new PSProperty(1, 0, "DESCRIPTION",
            "A mean dog", readOnly));
        return result;

    }
    void iGetProperties(Vector peers, Vector properties, EResult whenDone, int ix, int size) {
         if (ix >= size)
            whenDone <- forward(etrue);
         else {
                jObjectFuture /* Vector */ peerPropertiesChannel;

                ((jiPropertyPeer)myPeers.elementAt(ix)).getProperties(
                  &peerPropertiesChannel);
                ewhen peerPropertiesChannel (Object theObject) { // EVALUE LINES 26 FREEVARS 1 CLASSVARS 1
                 Vector peerProperties = (Vector)theObject;
                 // TODO: catch cast exception and bypass this peer with
                 // debug warning
                 for (int j = 0; j < peerProperties.size(); j++) {
                  ((PSProperty)peerProperties.elementAt(j)).setSource(ix);
                  try {
                    myProperties.addElement(
                      ((PSProperty)peerProperties.elementAt(j)).clone());
                    if (theTrace.debug && Trace.ON)
                      theTrace.debugm("iiProperty got property " +
                        peerProperties.elementAt(j));
                  } catch (PSPropertyException e) {
                    // XXX this property doesn't get reported
                    // to GUI unless we stuff exception in
                    // and GUI looks for that, but peer doesn't
                    // know that, will report...manager isn't
                    // checking currently if reported property was
                    // in initial set...gui will get and presumably
                    // dump in with some error logged
                    if (theTrace.debug && Trace.ON) {
                      theTrace.debugm("iiECProperty START exception" +
                        "property: " + (PSProperty)peerProperties.elementAt(j)
                         + " exception = " + e);
                    }
                 }
                }
                iGetProperties (myPeers, myProperties, whenDone,
                  ix + 1, size);
              }
          }
    }
    void readyForNextInput(EResult messageOrderer, EResult propertySheetInputHandler, int source) {

      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty readyForNextInput enter " +
              "messageOrderer: " + messageOrderer +
              "PropertySheetInputHandler: " + propertySheetInputHandler +
              "source: " + source);
      }

      messageOrderer <- forward(this);
      // TODO may be host or multiple client, need to forward for that one
      if (source == PropertySheetMessage.HOSTUI) {
        propertySheetInputHandler <- forward(this);
      }

    }
    public jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorMenus, jVerbContext context) {

//      if (theTrace.debug && Trace.ON) {
//         theTrace.debugm("Contextualizing for property with context " + context);
//       }

        if (!IAmTheHost)
          return priorMenus;

        if (myPeers.size() == 0)
            return priorMenus;

        if (context.amDO()) {
          jiVerbHandler handler = new jPropertySheetVerbHandler(this);
          jMenuEntry entry = new jMenuEntry("COMMAND:MODIFYME", handler);
          priorMenus.forceEntry(entry);
        }

//      if (theTrace.debug && Trace.ON) {
//        theTrace.debugm("Done contextualizing, priorMenus is " + priorMenus);
//      }

      return priorMenus;
    }
    local void init(istProperty propertyState) {

      // Tracing
      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty init enter");
      }

      // Initialize state
      myState = propertyState;

      //KSSHack Replaced by the IAmHost variable in Ingredient
      //KSSHackIAmTheHost = ((environment.flags & PresenceEnvironment.IsHostPresence)
      //KSSHack              != 0);

      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty init IAmTheHost = " + IAmTheHost);
      }

      myFirstPropertyMessageFromUI = true;
      mySaveMessageOrderer = null;
      myPresenter = null;
      myActive = false;

      if (myPeers == null)
        myPeers = new Vector();

      // for beta, property sheet is only offered at the host
      // (note the host presence does not accept property sheet messages
      // of any kind from client presences)
      if (IAmTheHost)
        ((jiVerbManager)iinVerbManager).registerVerbPeer((jiVerbPeer)this);
     }
    local void init() {
      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty blind init enter");
      }
      this.init(null);
    }
    emethod uPropertySheet() {
      EStdio.out().println("  In Property_ingr:uPropertySheet()");
      if (IAmTheHost && (myPeers.size() == 0)) {
        ethrow new eePropertySheetNoPropertiesException();
        return;
      } else if (IAmTheHost) {
        PropertySheetMessage msg = createModifyMessage();
        // XXX if this is called from init, iPropertyInput fails
        // due to getting a non-null presenter, which isn't tested for
        iPropertyInputGateway((EResult)null, msg,
          PropertySheetMessage.WORLD_EVENT);
      } else {
        ethrow new eePropertySheetClientNotSupportedException();
        return;
      }
    }
    emethod uPropertyFromUI(EResult propertySheetInputHandler, PropertySheetMessage msg) {
      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty uPropertyFromUI enter " +
              "PropertySheetInputHandler: " + propertySheetInputHandler +
              "msg: " + msg);
      }
      iPropertyInputGateway(propertySheetInputHandler, msg,
        PropertySheetMessage.HOSTUI);
    }
    emethod iPropertyInput(EResult messageOrderer, EResult propertySheetInputHandler, PropertySheetMessage msg, int source) {

      if (theTrace.debug && Trace.ON) {
        theTrace.debugm("iiECProperty iPropertyInput enter " +
              "messageOrderer: " + messageOrderer +
              "PropertySheetInputHandler: " + propertySheetInputHandler +
              "msg: " + msg +
              "source:" + source);
      }

      // TODO handle variety of messages here
      switch(msg.action()) {
        // TODO validation source based on action
        // TODO only START applies if not active
        case PropertySheetMessage.START:
          if (!myActive) {
            UIPresenterFactory factory = Global.theUIPresenterFactory;
            myPresenter = factory.createPropertySheetPresenter();
            myProperties = new Vector();
            EBoolean getPropertiesWhenDone;

            iGetProperties (myPeers, myProperties, &getPropertiesWhenDone,
                0, myPeers.size());
            ewhen getPropertiesWhenDone (boolean status) { // EWHENDONE LINES 6 FREEVARS 3 CLASSVARS 2
              myPresenter.init(this, "gui/dialogs/placeholder3.gif",
               null, null, myProperties);
              myActive = true;
              myPresenter.update(createShowMessage()); // po: reuse one
              readyForNextInput(messageOrderer, propertySheetInputHandler,
                source);
            }

        } else {
          if (theTrace.debug && Trace.ON) {
            theTrace.debugm("iiECProperty - sending SHOW to GUI");
          }
          myPresenter.update(createShowMessage()); // po: reuse one
          readyForNextInput(messageOrderer, propertySheetInputHandler,source);
        }
          break;

        case PropertySheetMessage.PROPOSE:
            //  assert (source == PropertySheetMessage.HOSTUI)
            if (theTrace.debug && Trace.ON) {
              theTrace.debugm("iiECProperty - received PROPOSE from GUI");
            }
         EBoolean statusChannel;
         PSProperty proposeProperty;
         etry {
          try {
            // TODO assuming just one property
            proposeProperty = (PSProperty)
              ((PSProperty)msg.properties().elementAt(0)).clone();

            ((jiPropertyPeer)(myPeers.elementAt(proposeProperty.source()))).
                propose(proposeProperty, &statusChannel);

            ewhen statusChannel (boolean status) { // EWHENDONE LINES 6 FREEVARS 0 CLASSVARS 0
              if (theTrace.debug && Trace.ON) {
                  theTrace.debugm("iiECProperty - status reply, status = "
                    + status);
              }
              if (status) {
                 // TODO clone proposeProperty for each report message
                // once we have multiple GUI, send report to the
                // others, so assumes have all the info in this closure
                // to do that, i.e. the property, the source
              }
              // ignore false reply, expect exception to be thrown
              // trace: log false to see if exception also thrown?
            }
           } catch (Throwable e) {
              ethrow e;
           }
          } ecatch (PSPropertyException e) { // LINES 21 FREEVARS 2 CLASSVARS 1
            // source UI needs to know about this
            if (theTrace.debug && Trace.ON) {
                theTrace.debugm("iiECProperty - propose exception = "
                  + e);
            }
            if (e.getErrorCode() == PSPropertyException.UNKNOWN_ID) {
              if (theTrace.debug && Trace.ON) {
                theTrace.debugm("iiECProperty - unknown id: property = " +
                  proposeProperty);
              }
            }
            // Let unexpected type exception go straight thru to GUI,
            // since manager doesn't manipulate type at all

            // at the moment must have come from host UI...
            // if due to clone failure, gui told (this is unlikely
            // if it's in original set and type wasn't changed by GUI)
            msg.setException(e);
            myPresenter.update(msg);
            &statusChannel <- forward(efalse);
          } ecatch (Throwable e) { // spam LINES 3 FREEVARS 0 CLASSVARS 0
            // this is an internal error, log for now, may
            // want to cancel any UI or at least alert (update may
            // or not have been done, UI may be stale or optimistically
            // in error)
              if (theTrace.debug && Trace.ON) {
                  theTrace.debugm("iiECProperty - unknown exception, exception = "
                    + e);
              }
            // really want to continue? how could you tell?
          }
          readyForNextInput(messageOrderer, propertySheetInputHandler,source);
          break;
        case PropertySheetMessage.REPORT:
            Assertion.test(source == PropertySheetMessage.WORLD_EVENT);
            if (theTrace.debug && Trace.ON) {
              theTrace.debugm("iiECProperty - received REPORT from world");
            }
            myPresenter.update(msg);
            readyForNextInput(messageOrderer, propertySheetInputHandler,
                source);
            break;
        case PropertySheetMessage.CANCEL:
          if (theTrace.debug && Trace.ON) {
            theTrace.debugm("iiECProperty - received CANCEL from GUI");
          }

          myActive = false;
          myProperties = null;
      // null out presenter so it can be gc'ed now, regardless of
      // whether we ever make another one.
      myPresenter = null;

/*
          // TEST of uPropertySheet

          etry {
            uPropertySheet();
          } ecatch (Exception e) { // COMMENTED OUT
            if (theTrace.debug && Trace.ON) {
                theTrace.debugm("iiECProperty - exception for uPropertySheet = "
                  + e);
            }
          }
 */

          readyForNextInput(messageOrderer, propertySheetInputHandler,source);
          break;
        }


      }
    emethod iPropertySheet(Hashtable aoBehaviors, Hashtable propertyBehaviors) {
      // TODO ignore behaviors (avatar) for now

      PropertySheetMessage msg = createModifyMessage();

      iPropertyInputGateway((EResult)null, msg,
        PropertySheetMessage.WORLD_EVENT);

    }
}

