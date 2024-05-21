/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.describe;

import ec.plubar.istBase;
import ec.cosm.objects.agent.UnumCreationContext;

public class istDescriber extends istBase {

    /**
     * A Unicode string representing the object description.  This
     * doesn't address international concerns (only international
     * character sets). A new protocol will need to be developed to
     * support localized descriptions.
     */
    public String theDescription;

    /* a brief name, e.g. as used in labels */
    public String theShortDescription;
    
    /**
     * the thumbnail of the object, if any
     * XXX this comes only out of "Thumbnail" realmtext entry, and doesn't
     * dynamically update
     * suitable for use by catalog, inventory, or container window
     */
    public String theThumbnail;
  
    public istDescriber() {  // RealmText constructor  
        theShortDescription = null;
        theDescription      = new String("No description.");
        theThumbnail        = null;
    }
    
    public istDescriber(UnumCreationContext context) {  // RealmText constructor  
        theShortDescription = (String)context.getStringOrDefault("name", null);
        theDescription      = (String)context.getStringOrDefault("description",
                                                            "No description.");
        theThumbnail        = (String)context.getStringOrDefault("thumbnail",
                                                                 null);
    }
  }
