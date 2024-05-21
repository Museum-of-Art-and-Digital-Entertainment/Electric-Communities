/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.texturize;

import java.util.Hashtable;
import ec.cosm.objects.TextureStructure;
import ec.e.hold.DataHolder;
import ec.misc.graphics.RGB;
import ec.plubar.istBase;

public class istTexturizable extends istBase {

    public boolean clientTexturizationEnabled;
    // this hashtable has migrated into PresentationState
    // (objects/ingredients/Support/j_Utilities.java) since the
    // textures applied to an object are properly subject to TOS.
    // HOWEVER, we still have it here so as to simply use it in
    // the ingredient factory.
    public Hashtable myTextureStructures;
    
    /** Null constructor, creates empty state */
    public istTexturizable() {
      myTextureStructures = null;
      clientTexturizationEnabled = false;
    }

    /**
     * Create from data holder
     */
    public istTexturizable(String part, DataHolder holder) {
      clientTexturizationEnabled = false;
      if (holder != null) {
        myTextureStructures = new Hashtable(1);
        if (part != null) {
            myTextureStructures.put(part, new TextureStructure(holder));
        } else {
            myTextureStructures.put("default", new TextureStructure(holder));
        }
      }
    }
    
    /**
     * Create from RGB value
     */
    public istTexturizable(String part, RGB rgb) {
      clientTexturizationEnabled = false;
      if (rgb != null) {
        myTextureStructures = new Hashtable(1);
        if (part != null) {
          myTextureStructures.put(part, new TextureStructure(rgb));
        } else {
          myTextureStructures.put("default", new TextureStructure(rgb));
        }
      }
    }

    /**
     * Create from basic TextureStructure
     */
    public istTexturizable(String part, TextureStructure textureStructure) {
      clientTexturizationEnabled = false;
      if (textureStructure != null) {
        myTextureStructures = new Hashtable(1);
        if (part != null) {
            myTextureStructures.put(part, textureStructure);
        } else {
            myTextureStructures.put("default", textureStructure);
        }
      }
    }
    
    /**
     * Create from a Hashtable of TextureStructures
     */
    public istTexturizable(Hashtable textureStructures) {
      clientTexturizationEnabled = false;
      myTextureStructures = textureStructures;
    }
    
    /**
     * Create from another istTexturizable
     */
    public istTexturizable(istTexturizable textureInfo) {
      clientTexturizationEnabled = false;
      if (textureInfo != null) {
        myTextureStructures = textureInfo.myTextureStructures;
      } else {
        myTextureStructures = null;
      }
    }
  }
    public class istTexturizer
    extends istBase {
    
    public TextureStructure myTextureStructure;
    
    /** Null constructor, creates empty state */
    public istTexturizer() {
      myTextureStructure = null;
    }
    
    /**
     * Create from data holder
     */
    public istTexturizer(DataHolder holder) {
      myTextureStructure = new TextureStructure(holder);
    }
    
    /**
     * Create from RGB value
     */
    public istTexturizer(RGB rgb) {
      myTextureStructure = new TextureStructure(rgb);
    }

    /**
     * Create from basic TextureStructure
     */
    public istTexturizer(TextureStructure textureStructure) {
      myTextureStructure = textureStructure;
    }
    
    /**
     * Create from another istTexturizer
     */
    public istTexturizer(istTexturizer textureInfo) {
      if (textureInfo != null) {
        myTextureStructure = textureInfo.myTextureStructure;
      } else {
        myTextureStructure = null;
      }
    }
  }
