/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.texturize;

import ec.cosm.objects.TextureStructure;

public einterface ukTexturizable$kind {
    uSetTexture(TextureStructure textureStructure, String part);
}

public einterface pkTexturizableHost$kind {
    pHostSetTexture(TextureStructure textureStructure, String part);
}

public einterface pkTexturizableClient$kind {
}

public einterface iskTexturizable$kind extends ukTexturizable$kind, pkTexturizableHost$kind, pkTexturizableClient$kind {
}

public einterface ukTexturizer$kind {
    uTexturize(ukTexturizable$kind theTarget, String part, Object alerter);
}

public einterface pkTexturizerHost$kind {
}

public einterface pkTexturizerClient$kind {
}

public einterface iskTexturizer$kind extends ukTexturizer$kind, pkTexturizerHost$kind, pkTexturizerClient$kind {
}

