/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.texturize;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import ec.cosm.gui.framework.GUICommandPresenterEvent;
import ec.cosm.ingredients.behavior.ikBehaviorManager$kind;
import ec.cosm.ingredients.behavior.jBehaviorWrapper;
import ec.cosm.ingredients.behavior.jiBehaviorRevoker;
import ec.cosm.ingredients.behavior.jiBehaviorWrapper;
import ec.cosm.objects.eeTOSAddUnumFailed;
import ec.cosm.objects.ikVerbManager$kind;
import ec.cosm.objects.iskCompositable$kind;
import ec.cosm.objects.iskContainership$kind;
import ec.cosm.objects.jIdentity;
import ec.cosm.objects.jiPresenterInterest;
import ec.cosm.objects.jiRegisterContainableInterest;
import ec.cosm.objects.jiRegisterPresenterInterest;
import ec.cosm.objects.jiVerbHandler;
import ec.cosm.objects.jiVerbManager;
import ec.cosm.objects.jiVerbPeer;
import ec.cosm.objects.jMenuEntry;
import ec.cosm.objects.jPieMenuRecord;
import ec.cosm.objects.jVerbContext;
import ec.cosm.objects.PresentationState;
import ec.cosm.objects.TextureStructure;
import ec.cosm.objects.ukAvatarAlertBehavior$kind;
import ec.cosm.ui.presenter.CompositablePresenter;
import ec.cosm.ui.presenter.PresenterException;
import ec.cosm.ui.presenter.SwatchData;
import ec.cosm.ui.presenter.UnumUIPresenter;
import ec.e.run.Trace;
import ec.pl.runtime.PresenceEnvironment;
import ec.pl.runtime.PresenceRouter;
import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;

class jTexturizerVerbHandler
    extends jiVerbHandler {
    /* This is the behavior we are concerned about */
    private final static String myBehaviorString = new String("isTexturizable");
    /* This should be the texturizer ingredient */
    private iskTexturizer$kind myTexturizer;
  
    /* The texturizer ingredient passes itself to the constructor */
    private static final Trace theTrace = new Trace("texturize");
    
    /* The texturizer ingredient should pass itself to the constructor */
    jTexturizerVerbHandler (iskTexturizer$kind tex) {
      myTexturizer = tex;
    }
    
    /** Set up the lsit of behaviors for texturizer verbs */
    public Vector doBehaviorsNeeded () {
      return createBehaviorVector(myBehaviorString);
    }
    
    public Vector aoBehaviorsNeeded () {
      Vector ret = new Vector();
      ret.addElement("alert");
      return ret;
    }
    
    /** Perform a texturizer verb.  This verb should only be called if your
     * ingredient has a jTexturizerVerbHandler.  Then:
     *  1) Make sure the direct object (DO) "isTexturizable"
     *  2) Get the behavior supplied by the texturizable DO, which should
     *     be eTexturizableBehaviorWrapper, which implements ukTexturizable
     *  3) Get the texturizable target from the behavior
     *  4) Get the part from param.commandVerb()
     *  5) Tell the texturizer ingredient to texturize the specified part
     *     of the texturizable target
     */
    public void performVerb (Hashtable aoBehaviorFacets,
                             Hashtable doBehaviorFacets,
                             Hashtable bodyBehaviorFacets,
                             Object directObject,
                             Object param) {

      /* Make sure what we're pointing at has the "isTexturizable" behavior */
      if (doBehaviorFacets.containsKey(myBehaviorString)) {

        Object behavior = doBehaviorFacets.get(myBehaviorString);
        if (theTrace.debug && Trace.ON) { theTrace.debugm("behavior="+myBehaviorString); }

        /* Make sure the behavior is a ukTexturizable */
        if (behavior != null && behavior instanceof eTexturizableBehaviorWrapper) {
          ukTexturizable$kind target = (ukTexturizable$kind)behavior;

          /* Make sure the param is a GUICommandPresenterEvent */
          if (param != null && param instanceof GUICommandPresenterEvent) {
            Object verb = ((GUICommandPresenterEvent)param).commandVerb();

            /* Make sure the commandVerb Object is a String */
            if (verb instanceof String) {
              String part = (String)verb;
              
              ukAvatarAlertBehavior$kind alert =
                (ukAvatarAlertBehavior$kind)aoBehaviorFacets.get("alert");
              myTexturizer <- uTexturize(target, part, alert);
            }
          } else {
            theTrace.debugm("isTexturizable param not GUICommandPresenterEvent");
          }
        } else {
          theTrace.debugm("isTexturizable class not ukTexturizable");
        }
      } else {
        if (theTrace.debug && Trace.ON) { theTrace.debugm("Unknown verb call"); }
      }
    }
  }
    class jTexturizableBehavior {
    /** The interface to call uSetTexture on */
    public ukTexturizable$kind myTexturizableKind;
    /** A list of part names */
    public Vector myTexturizableParts = new Vector(8, 4);
    private static final Trace theTrace = new Trace("texturize");

    /** Constructor with the texturizable interface and the Hashtable
     * from the state of the texturizable ingredient from which to extract
     * the part names
     */
    public jTexturizableBehavior (ukTexturizable$kind texturizableKind,
                                  Enumeration maskNames) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("texturizableKind "+texturizableKind);                                  
      myTexturizableKind = texturizableKind;
      if (maskNames != null) {
        while (maskNames.hasMoreElements()) {
            myTexturizableParts.addElement((String)maskNames.nextElement());
        }
      }  
    }
    
    public String toString () {
      return "jTexturizableBehavior[myTexturizable "+myTexturizableKind+"]";
    }
  }
    interface jTexturizableBehaviorIntf {
    Vector getParts();
  }

eclass eTexturizableBehaviorWrapper
    extends jBehaviorWrapper
    implements ukTexturizable$kind, jTexturizableBehaviorIntf {

    /** Convenient Java class to hold the data */
    private jTexturizableBehavior myBehavior = null;
    private static final Trace theTrace = new Trace("texturize");
    
    /** Construct with jTexturizableBehavior */
    local eTexturizableBehaviorWrapper(jTexturizableBehavior beh) {
      super();
      myBehavior = beh;
    }

    /** Construct with jTexturizableBehavior and a jiBehaviorRevoker */
    local eTexturizableBehaviorWrapper(jTexturizableBehavior beh,
                                       jiBehaviorRevoker revoker) {
      super(revoker);
      if (theTrace.debug && Trace.ON) theTrace.debugm("eTexturizableBehaviorWrapper[myBehavior "+myBehavior+"]");
      myBehavior = beh;
    }

    local jiBehaviorWrapper cloneUnclonableWrapper(jiBehaviorRevoker revoker) {
      if (myClonable == true) {
        return new eTexturizableBehaviorWrapper(myBehavior, revoker);
      } else {
        return null;
      }
    }

    /** public method available through the jTexturizableBehaviorIntf to
     * allow access to the list of parts on the texturizable object
     */
    local Vector getParts() {
      if (myBehavior != null && myBehavior.myTexturizableParts != null) {
        return myBehavior.myTexturizableParts;
      } else {
      return null;
      }
    }
    
    /** The method from the ukTexturizable interface which allows the
     * texturizer ingredient which finally gets the wrapper to set the
     * testure on the underlying texturizable object
     */
    emethod uSetTexture (TextureStructure textureStructure, String part)
    {
      if (theTrace.debug && Trace.ON) theTrace.debugm("myBehavior="+myBehavior);   
      if (!isRevoked()) {
        myBehavior.myTexturizableKind <- uSetTexture(textureStructure, part);
      }
    }

    public String toString () {
      return "eTexturizableBehaviorWrapper[myBehavior "+myBehavior+"]";
    }
  }
public interface iiTexturizable$iijif
{
    void iPresenterAvailable(CompositablePresenter thePresenter, UnumUIPresenter uiPresenter, PresentationState pState, Hashtable behaviors, jIdentity identity);
}

public eclass iiTexturizable$iicode
extends Ingredient
implements IngredientJif, iskTexturizable$kind, iiTexturizable$iijif, jiPresenterInterest
{
    private static final Trace theTrace =  new Trace("texturize");
    pkTexturizableHost$kind myHost;
    CompositablePresenter myPresenter;
    private static final String myBehaviorString =  new String("isTexturizable");
    //KSSHack Put here as a quick fix for Plubar test
    PresenceEnvironment environment = null;

    /* State bundle 'myState' */
    private ec.cosm.ingredients.texturize.istTexturizable myState;
    /* Neighbors */
    iskCompositable$kind iinCompositable;
    ikBehaviorManager$kind iinBehaviorManager;
    iskContainership$kind iinContainership;

    public iiTexturizable$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istTexturizable)state);
    }

    local void setNeighbors(iskCompositable$kind in_iinCompositable, ikBehaviorManager$kind in_iinBehaviorManager, iskContainership$kind in_iinContainership) {
        iinCompositable = in_iinCompositable;
        iinBehaviorManager = in_iinBehaviorManager;
        iinContainership = in_iinContainership;
    }

    public void hostSetTexture(TextureStructure textureStructure, String part) {
      // get the current presentation?!
      if (theTrace.debug && Trace.ON) { theTrace.debugm("myHost="+myHost+" part='"+part+"' tex="+textureStructure); }      
      // note that this works ONLY on the host!!!
      // (client una do not get their presentation state in containership, only in composition!)
      Object currentPresentation = ((jiRegisterContainableInterest)iinContainership).iGetPstate();

      // clone and update!
      PresentationState pstate = new PresentationState((PresentationState)currentPresentation);
      if (theTrace.debug && Trace.ON) { theTrace.debugm("currentPresentation is "+currentPresentation+", new is "+pstate); }
      /* If the part is not named, set it to "default" */
      if (part == null) {
        part = new String("default");
      }

      if (theTrace.debug && Trace.ON) { theTrace.debugm("textureStructure is "+textureStructure+", part is '"+part+"'"); }

      pstate.myPState.textureBindings.put(part, textureStructure);

      if (theTrace.debug && Trace.ON) { theTrace.debugm("table after adding new part is "+pstate.myPState.textureBindings); }


      ((jiRegisterContainableInterest)iinContainership).iRequestChangePresentation(pstate);
      
      return;
    }
    public Object jiGetClientState() {
      return myState;
    }
    public void iPresenterAvailable(CompositablePresenter thePresenter, UnumUIPresenter uiPresenter, PresentationState pState, Hashtable behaviors, jIdentity identity) {
      //* Store the presenter so we can tell it to texturize */
      myPresenter = thePresenter;
      
      // don't add the behavior unless the user CAN texturize this object.
      if ((IAmTheHost || myState.clientTexturizationEnabled) &&
          myPresenter != null) {
          
        SwatchData data = myPresenter.getSwatchData("default");

        if (theTrace.debug && Trace.ON) { theTrace.debugm("In iPresenterAvailable() with SwatchData=" +
                   data); }

        if (data != null) {
        
          /* Construct a behavior wrapper with the iskTexturizable interface (which
           * implements ukTexturizable) and the Hashtable containing the parts of
           * the texturizable object */
           
          jTexturizableBehavior behavior =
          new jTexturizableBehavior(this, data.getMaskNames());

          if (theTrace.debug && Trace.ON) { theTrace.debugm("In iPresenterAvailable() with behavior=" +
                       behavior); }

            /* Pass the wrapper onto the behavior manager so texturizers can get
             * at it */
            iinBehaviorManager <- iRegisterBehavior(myBehaviorString,
                                  new eTexturizableBehaviorWrapper(behavior));

        }
      }
    }
    local void init(istTexturizable textureInfo) {

      if (textureInfo == null) {
        myState = new istTexturizable();
      } else {
        myState = textureInfo;
      }

      if (environment.hostPresenceDeflector instanceof pkTexturizableHost$kind) {
        myHost = (pkTexturizableHost$kind)environment.hostPresenceDeflector;
      } else if (IAmTheHost) {
        myHost = this;

        /* Did the state bundle provide textures to use at create time?
           if so, we set the textures here. */
        if (myState.myTextureStructures != null) {
          String textureName;
          for (Enumeration e=myState.myTextureStructures.keys(); e.hasMoreElements();)  {
            textureName = (String) e.nextElement();
            hostSetTexture((TextureStructure) myState.myTextureStructures.get(textureName),
                                              textureName);
          }
        }
      }
                
      /**  Notify peer compositable that we want to hear about the presenter;
       *  This is where we iPresenterAvailable() should get called back.
       */
      ((jiRegisterPresenterInterest)iinCompositable).iRegisterInterest
        ((jiPresenterInterest)this);
    }
    emethod uSetTexture(TextureStructure textureStructure, String part) {
      if (theTrace.debug && Trace.ON) { theTrace.debugm("myHost="+myHost+" part='"+part+"' tex="+textureStructure); }      
      if (IAmTheHost) {
        hostSetTexture(textureStructure, part);
      } else {
        if (myState.clientTexturizationEnabled) {
          myHost <- pHostSetTexture(textureStructure, part);
        }
      }
    }
    emethod pHostSetTexture(TextureStructure textureStructure, String part) {
      if (theTrace.debug && Trace.ON) { theTrace.debugm("myHost="+myHost+" hostPresenceDeflector="+environment.hostPresenceDeflector+" part='"+part+"' tex="+textureStructure); }      
      if (environment.hostPresenceDeflector != null) {
        environment.hostPresenceDeflector <- (pkTexturizableHost$kind).pHostSetTexture(textureStructure, part);
        return;
      }
    
      // if clients are enabled, the host ingredient will do it.
      // otherwise, someone is sending the pHostSetTexture message illegally.
      if (myState.clientTexturizationEnabled) {
        hostSetTexture(textureStructure, part);
      }
    }
}

public interface iiTexturizer$iijif
{
    void init(istTexturizer textureInfo);
    jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorMenus, jVerbContext context);
    Object jiGetClientState();
    void setNeighbors(ikVerbManager$kind in_iinVerbManager);
}

public eclass iiTexturizer$iicode
extends Ingredient
implements IngredientJif, iskTexturizer$kind, iiTexturizer$iijif, jiVerbPeer
{
    private static final Trace theTrace =  new Trace("texturizer");
    private static final String myBehaviorString =  new String("isTexturizable");
    pkTexturizerHost$kind myHost;
    //KSSHack Put here as a quick fix for Plubar test
    PresenceEnvironment environment = null;

    /* State bundle 'myState' */
    private ec.cosm.ingredients.texturize.istTexturizer myState;
    /* Neighbors */
    ikVerbManager$kind iinVerbManager;

    public iiTexturizer$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istTexturizer)state);
    }

    local void setNeighbors(ikVerbManager$kind in_iinVerbManager) {
        iinVerbManager = in_iinVerbManager;
    }

    public jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorMenus, jVerbContext context) {

      /** only if we are in hand and we are not the direct object and the
       * direct object is texturizable.
       */
      if (theTrace.debug && Trace.ON) theTrace.debugm("context is "+context+", context.behaviors() is "+context.behaviors());
      if (context.amInHand() && !context.amDO() &&
          context.behaviors().containsKey(myBehaviorString) ){

        /** Construct a jTexturizerVerbHandler */
        jiVerbHandler handler = new jTexturizerVerbHandler(this);

        /** Get the texturizable behavior from the verb context */
        eTexturizableBehaviorWrapper behavior =
          (eTexturizableBehaviorWrapper)(context.behaviors()).get(myBehaviorString);

        Vector parts = null;
        int numParts = 0;
        jMenuEntry entry = null;

        /** If we found a texturizable behavior, get the parts (if any) */
        if (behavior != null) {
          parts = ((jTexturizableBehaviorIntf)behavior).getParts();
        }

        if (theTrace.debug && Trace.ON) theTrace.debugm("parts is "+parts);

        if (parts != null) {
          //KSSHack The submenu should not default to 8; how can we figure it
          //KSSHack out from the slot numbers?
          jPieMenuRecord subMenu = new jPieMenuRecord(8);
          String part;

          /** Build a submenu from the parts */
          numParts = parts.size();
     
          /** Add the submenu texturize menu */
          if (numParts > 1) {
            for (int i = 0; i < numParts; i++) {
              String command = new String("COMMAND:") +
                ((String)parts.elementAt(i)).toUpperCase();
              entry = new jMenuEntry(command, handler);
              subMenu.forceEntry(entry);
            }
            entry = new jMenuEntry("COMMAND:TEXTURIZEMENU", handler, true, subMenu);
          } else if (numParts == 1) {
            /** There are no or one parts, so it's just a texturize menu with no submenu */
            entry = new jMenuEntry("COMMAND:TEXTURIZE",
                          (String) parts.elementAt(0),
                                   handler);
          } else {
            entry = new jMenuEntry("COMMAND:TEXTURIZE", handler);           
          }
        } else {
          /** There are no parts, so it's just a texturize menu with no submenu */
          entry = new jMenuEntry("COMMAND:TEXTURIZE", handler);
        }

        priorMenus.forceEntry(entry);
      }
      return priorMenus;
    }
    void setTexture(ukTexturizable$kind theTarget, String part) {
      if (theTrace.debug && Trace.ON) { theTrace.debugm("target="+theTarget); }
      if (theTarget != null) {
      if (theTrace.debug && Trace.ON) { theTrace.debugm("part='"+part+"', myState.myTextureStructure "+myState.myTextureStructure); }
        theTarget <- uSetTexture(myState.myTextureStructure, part);
      }
    }
    void iSetTextureInfo(istTexturizer textureInfo) {
      if (myState != null) {
        myState = textureInfo;
      } else {
        myState = new istTexturizer(textureInfo);
      }
    }
    public Object jiGetClientState() {
      return myState;
    }
    void sendToClients(RtEnvelope env) {
      PresenceRouter.sendEnvelopeToOthers(environment.otherPresences, env);
    }
    local void init(istTexturizer textureInfo) {
      iSetTextureInfo(textureInfo);
    
      ((jiVerbManager)iinVerbManager).registerVerbPeer((jiVerbPeer)this);
      if (environment.hostPresenceDeflector instanceof pkTexturizerHost$kind) {
        myHost = (pkTexturizerHost$kind)environment.hostPresenceDeflector;
      } else if (IAmTheHost) {
        myHost = this;
      }
    }
    emethod uTexturize(ukTexturizable$kind theTarget, String part, Object alertobj) {
      ukAvatarAlertBehavior$kind alert = (ukAvatarAlertBehavior$kind)alertobj;
      if (theTrace.debug && Trace.ON) { theTrace.debugm("myHost="+myHost+" part='"+part+"' target="+theTarget); }      
      etry {
      
        setTexture(theTarget, part);

      } ecatch (eeTOSAddUnumFailed addFailed) { // message LINES 6 FREEVARS 2 CLASSVARS 0
        String message =
          "You can't use that swatch in this region.";
        
        alert <-
          uShowSimpleAlert("Terms of Service violation",
                           message,
                           "Continue",
                           addFailed);
      }
    }
}

