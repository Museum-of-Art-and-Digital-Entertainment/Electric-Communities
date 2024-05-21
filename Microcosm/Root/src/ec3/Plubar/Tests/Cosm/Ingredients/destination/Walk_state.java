/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.destination;

import java.util.Hashtable;
import java.util.Vector;
import ec.cosm.objects.jiVerbHandler;
import ec.cosm.objects.agent.UnumCreationContext;
import ec.plubar.istBase;
import ec.misc.graphics.Point2DInt;
import ec.e.run.Trace;


public class istDestination extends istBase {

    /**
     * HACK XXX FRF This holds the bogus 2D walk destination
     */
    public Point2DInt p2D;
    
    // Override walkto code.
    public boolean reachable = true;  
    
    public istDestination () {
      // empty constructor for bogus Randy "create with no state" idea...
      // XXX RANDY how did you check in this file removing this constructor
      // without breaking the references to it elsewhere??? did you not do
      // a full rebuild before checking in?????  are you on the list for a
      // new home machine for yourself???????? -- RobJ
    }

    public istDestination (UnumCreationContext context) {

      // XXX Rob's hack: floors get -1, -1 anchor points, which get
      // passed into them as props.

      String type = context.getStringOrDefault("type", null);
      if ("floor".equals(type) || "ecmffloor".equals(type)) {
        p2D = new Point2DInt(-1, -1);
      } else {
        p2D = context.getPoint2DIntOrNull("anchorpoint");
      }
      reachable  = context.getBooleanOrDefault("reachable", true);
    }
  }

class jMoveToVerbHandler extends jiVerbHandler {

    Vector myActorBehaviorsNeeded = null;
    private ikDestination$kind myDest;
    private static final Trace theTrace = new Trace("movetoverbhandler");
    
    jMoveToVerbHandler (ikDestination$kind dest) {
      myDest = dest;
    }
    
    jMoveToVerbHandler(ikDestination$kind dest,
                       String[] extraActorBehaviorsNeeded) {
      this(dest);
      myActorBehaviorsNeeded =
        createBehaviorVector(extraActorBehaviorsNeeded);
    }
    
    public Vector aoBehaviorsNeeded () {
      return myActorBehaviorsNeeded;
    }

    public Vector bodyBehaviorsNeeded() {
      return createBehaviorVector("moveto");
    }

    public void performVerb (Hashtable aoBehaviorFacets,
                             Hashtable doBehaviorFacets,
                             Hashtable bodyBehaviorFacets,
                             Object directObject,
                             Object param) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("Calling iMoveTo on "+myDest);
      myDest <- iMoveTo(aoBehaviorFacets, bodyBehaviorFacets, param);
    }
  }
