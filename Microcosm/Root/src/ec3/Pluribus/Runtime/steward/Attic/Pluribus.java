package ec.pl.runtime;

import ec.vcache.ClassCache;
import java.lang.reflect.Field;
import java.util.Hashtable;

public class Plu {
    static public Object getSelf(String name) {
        return(getSelfForClass(forName(name)));
    }

    static public Object getSelfForClass(Class theClass) {
        try {
            Field f = theClass.getField("self");
            return f.get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static public Class forName(String className) {
        try {
            return(ClassCache.forName(className));
        } catch (ClassNotFoundException ex) {
            return(null);
        }
    }
}

public class pl$_deliverAtt {
    boolean presenceScope;
    String from;
    String toIngredient;
    String toMethod;

    public pl$_deliverAtt(boolean presenceScope, String from, String toIngredient,
                   String toMethod) {
        this.presenceScope = presenceScope;
        this.from = from;
        this.toIngredient = toIngredient;
        this.toMethod = toMethod;
    };
}

public class pl$_ingredient {
    pl$_kind kind;
    String name;

    public pl$_ingredient(pl$_kind kind, String name) {
        this.kind = kind;
        this.name = name;
    }
}

public class pl$_ingredientImpl {
    Hashtable attributes;
    pl$_kind ingredientKind;
    pl$_neighbor neighbors[];
    String stateBundles[];
    Class code;

    public pl$_ingredientImpl(Hashtable attributes,
                       pl$_kind ingredientKind,
                       pl$_neighbor neighbors[],
                       String stateBundles[],
                       Class code) {
        this.attributes = attributes;
        this.ingredientKind = ingredientKind;
        this.neighbors = neighbors;
        this.stateBundles = stateBundles;
        this.code = code;
    }

    public pl$_ingredientImpl() { }
}

public class pl$_ingredientRole {
    String name;
    pl$_template template;

    public pl$_ingredientRole(String name, pl$_template template) {
        this.name = name;
        this.template = template;
    }
}

public class pl$_kind {
    Hashtable attributes;
    String stateBundles[]; //KSS
    Class kindInterface;

  public pl$_kind(Hashtable attributes, String stateBundles[], //KSS
            Class kindInterface) {
        this.attributes = attributes;
        this.stateBundles = stateBundles; //KSS
        this.kindInterface = kindInterface;
    }

    public pl$_kind() { }
}

public class pl$_mapAtt {
    boolean neighborScope;
    String from;
    String to;

    public pl$_mapAtt(boolean neighborScope, String from, String to) {
        this.neighborScope = neighborScope;
        this.from = from;
        this.to = to;
    }
}

public class pl$_neighbor {
    String name;
    pl$_kind neighborKind;
    boolean isPlural;
    boolean isPresence;

    public pl$_neighbor(String name, pl$_kind neighborKind, boolean isPlural,
                 boolean isPresence) {
        this.name = name;
        this.neighborKind = neighborKind;
        this.isPlural = isPlural;
        this.isPresence = isPresence;
    }
}

public class pl$_presence {
    String name;
    pl$_presenceStructure structure;
    boolean isPrime;

    public pl$_presence(String name, pl$_presenceStructure structure,
            boolean isPrime) {
        this.name = name;
        this.structure = structure;
        this.isPrime = isPrime;
    }
}

public class pl$_presenceImpl {
    Hashtable attributes;
    pl$_presenceStructure structure;
    pl$_ingredientRole[] roles;

    public pl$_presenceImpl(Hashtable attributes, pl$_presenceStructure structure,
                     pl$_ingredientRole[] roles) {
        this.attributes = attributes;
        this.structure = structure;
        this.roles = roles;
    }

    public pl$_presenceImpl() { }
}

public class pl$_presenceRole {
    String name;
    pl$_presenceImpl presenceImpl;

    public pl$_presenceRole(String name, pl$_presenceImpl presenceImpl) {
        this.name = name;
        this.presenceImpl = presenceImpl;
    }
}

public class pl$_presenceStructure {
    Hashtable attributes;
    pl$_kind presenceKind;
    pl$_ingredient ingredients[];
    pl$_deliverAtt deliverAtts[];

    public pl$_presenceStructure(Hashtable attributes,
                          pl$_kind presenceKind,
                          pl$_ingredient ingredients[],
                          pl$_deliverAtt deliverAtts[]) {
        this.attributes = attributes;
        this.presenceKind = presenceKind;
        this.ingredients = ingredients;
        this.deliverAtts = deliverAtts;
    }

    public pl$_presenceStructure() { }
}

public class pl$_template {
    pl$_ingredientImpl ingredientImpl;
    pl$_mapAtt[] mapAtts;

    public pl$_template(pl$_ingredientImpl ingredientImpl,
                 pl$_mapAtt[] mapAtts) {
        this.ingredientImpl = ingredientImpl;
        this.mapAtts = mapAtts;
    }

    public pl$_template() { }
}

public class pl$_unumImpl {
    Hashtable attributes;
    pl$_unumStructure structure;
    pl$_presenceRole[] roles;

    public pl$_unumImpl(Hashtable attributes, pl$_unumStructure structure,
                 pl$_presenceRole[] roles) {
        this.attributes = attributes;
        this.structure = structure;
        this.roles = roles;
    }

    public pl$_unumImpl() { }

    static public PresenceRouter makePresenceRouter (Object unumKey,
                                                     String roleName) {
      return null;
    }
}

public class pl$_unumStructure {
    Hashtable attributes;
    pl$_kind unumKind;
    pl$_presence presences[];

    public pl$_unumStructure(Hashtable attributes, pl$_kind unumKind,
                      pl$_presence presences[]) {
        this.attributes = attributes;
        this.unumKind = unumKind;
        this.presences = presences;
    }

    public pl$_unumStructure() { }
}

