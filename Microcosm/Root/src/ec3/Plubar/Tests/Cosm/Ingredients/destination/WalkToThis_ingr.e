/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.destination;

import java.util.Hashtable;
import ec.cosm.objects.ikVerbManager$kind;
import ec.cosm.objects.iskCompositable$kind;
import ec.cosm.objects.jiBehaviorWrapper;
import ec.cosm.objects.jIdentity;
import ec.cosm.objects.jiPresenterInterest;
import ec.cosm.objects.jiRegisterPresenterInterest;
import ec.cosm.objects.jiVerbHandler;
import ec.cosm.objects.jiVerbManager;
import ec.cosm.objects.jiVerbPeer;
import ec.cosm.objects.jMenuEntry;
import ec.cosm.objects.jPieMenuRecord;
import ec.cosm.objects.jVerbContext;
import ec.cosm.objects.Location;
import ec.cosm.objects.PresentationState;
import ec.cosm.objects.SurfaceLocation;
import ec.cosm.objects.ukfCompositableOp$kind;
import ec.cosm.ui.presenter.CompositablePresenter;
import ec.cosm.ui.presenter.PropPresenter;
import ec.cosm.ui.presenter.UnumUIPresenter;
import ec.e.file.EStdio;
import ec.e.run.Trace;
import ec.misc.graphics.Point3D;
import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;

public interface iiWalkToThis$iijif
{
    void iPresenterAvailable(CompositablePresenter presenter, UnumUIPresenter uiPresenter, PresentationState pState, Hashtable behaviors, jIdentity identity);
    jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorRecord, jVerbContext context);
}

public eclass iiWalkToThis$iicode extends Ingredient implements IngredientJif, ikDestination$kind, iiWalkToThis$iijif, jiPresenterInterest, jiVerbPeer
{
    private static final Trace theTrace =  new Trace("walktothis");
    PropPresenter myPresenter;

    /* State bundle 'myState' */
    private ec.cosm.ingredients.destination.istDestination myState;
    /* Neighbors */
    iskCompositable$kind iinCompositable;
    ikVerbManager$kind iinVerbManager;

    public iiWalkToThis$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istDestination)state);
    }

    local void setNeighbors(iskCompositable$kind in_iinCompositable, ikVerbManager$kind in_iinVerbManager) {
        iinCompositable = in_iinCompositable;
        iinVerbManager = in_iinVerbManager;
    }

    public Object jiGetClientState() {return(myState);}
    public void iPresenterAvailable(CompositablePresenter presenter, UnumUIPresenter uiPresenter, PresentationState pState, Hashtable behaviors, jIdentity identity) {
      if (presenter instanceof PropPresenter || presenter == null)
        myPresenter = (PropPresenter)presenter;                
    }
    public jPieMenuRecord contextualizeVerbs(jPieMenuRecord priorRecord, jVerbContext context) {
      if (context.amDO() && !context.amAvatarBody()) {
        jiVerbHandler handler = new jMoveToVerbHandler(this);
        jMenuEntry entry = new jMenuEntry("COMMAND:WALKTO", handler);
        priorRecord.forceEntry(entry);
      }
      return priorRecord;
    }
    local void init(istDestination externalState) {
      if (null == externalState)  {     // Was any state provided?
        myState = new istDestination(); // No? Create one now.
      } else {
        myState = externalState;        // Use provided state block.
      }

      // Is the state valid?

      // Notify peer compositable that we want to hear about the presenter
      ((jiRegisterPresenterInterest)iinCompositable).iRegisterInterest
        ((jiPresenterInterest)this);
        
      // Notify peer verbmanager that we have a verb or two
      ((jiVerbManager)iinVerbManager).registerVerbPeer((jiVerbPeer)this);
    }
    local void init() {
      this.init(null);
    }
    emethod iMoveTo(Hashtable aoBehaviors, Hashtable bodyBehaviors, Object param) {
      EBoolean ignore;
      this <- iMoveToWithWait(aoBehaviors, bodyBehaviors, param, &ignore);
    }
    emethod iMoveToWithWait(Hashtable aoBehaviors, Hashtable bodyBehaviors, Object param, EResult waitDistributor) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("myState.reachable "+myState.reachable+", myPresenter "+myPresenter);
                           
      // if this unum isn't reachable, always pretend that walking to it fails                           
      if (!myState.reachable) {
        waitDistributor <- forward(efalse);
        return;
      }
      // For normal foreground objects, the object position is the destination.
      // Actually, there is supposed to be a dest. registration point!

      if (myPresenter == null) {
        // ACK!  No presenter means we shouldn't have gotten invoked here!
        EStdio.err().println("iMoveToWithWait invoked on "+this+", no presenter, ignoring.");
        return;
      }

      Point3D move_to_point = myPresenter.getPosition();

      if (theTrace.debug && Trace.ON) theTrace.debugm("move_to_point "+move_to_point);

      if (null == move_to_point) {
        // No place to go. This should probably throw an exception.
        waitDistributor <- forward(etrue);
        return;
      }

      // Get the moveto behavior out of here.
      jiBehaviorWrapper movetoBehavior =
        (jiBehaviorWrapper)bodyBehaviors.get("moveto");
      if (movetoBehavior != null) {
        ((ukfCompositableOp$kind)movetoBehavior) <-
          (ukfCompositableOp$kind).uRequestMoveTo(new SurfaceLocation(
            new Location(move_to_point), 0), false, waitDistributor);
      }
    }
}

