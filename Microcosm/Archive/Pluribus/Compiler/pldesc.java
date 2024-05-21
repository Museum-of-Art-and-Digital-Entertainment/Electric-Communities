package ec.plcompile;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.util.*;
import ec.e.hab.*;

/*

  The classes in this file are element descriptors. Element
  descriptors are objects which describe Pluribus elements that may be
  produced by the Pluribus compiler and used by the Unum Fabricator.

  Element descriptors may be characterized along two dimensions. They
  may be "external" or "internal" and they may be "terminal" or
  "non-terminal".

External vs. Internal:

  "External" element descriptors correspond to the definable entities
  which a programmer declares in Pluribus. They are top-level unum
  elements which may be used in a compositional fashion by the Unum
  Fabricator.  External element descriptors may be retrieved from the
  Haberdashery by context-independent references, that is, by an external
  key (e.g., by name). They may also be retrieved en-masse and filtered
  according to their assigned attributes.

  "Internal" element descriptors are lower-level descriptor objects
  which exist simply because Java lacks abstractions for describing
  complex data structures. In essence, all you get with Java are simple
  arrays and structs. If you want to compose a more complex organization
  (such as a struct field which is itself an array of some other kind of
  struct), you need to introduce additional classes. Internal element
  descriptors are these additional classes. Each internal element
  descriptor class is subsidiary to some external element descriptor
  whose data structure it is a part of. Internal element descriptors may
  only be retrieved by reference from other element descriptors which
  make use of them. Conversely, retrieving a external element descriptor
  object from the Haberdashery implicitly also retrieves any internal
  element descriptor objects which it directly or indirectly refers to.

  External and internal element descriptor classes are distinguishable
  from each other by their names:

  type          name
  ----          ----
  external      ElemXxx.
  internal      DescXxx.

Terminal vs. Non-Terminal:

  Some varieties of element are composed by reference to other
  elements (e.g., a presence impl refers to the ingredient impls of the
  ingredients from which it is made). In such cases it is possible to
  have element descriptors which do not fully specify the elements they
  describe because their set of references to constituent elements is
  incomplete. Such partially specified elements are useful because they
  permit deferred binding of the constituents.  Other varieties of
  element are not composed by reference to other elements and so are
  always fully specified.

  A "terminal" element descriptor describes an element which is never
  composed of other possibly unspecified elements. All internal element
  descriptors are terminal by definition and are stored in the Haberdashery
  as part of the object reference graph of the element descriptor which
  refers to them. References to external element descriptors, on the other
  hand, are stored as Haberdashery Designator objects.

  A "non-terminal" element descriptor describes an element whose
  composition may be left undetermined. Any unbound compositional
  element must first be determined before the element can be used in
  the actual creation of an unum; locating appropriate elements to do
  this is the job of the Unum Fabricator. A bound compositional
  element reference is stored as a Haberdashery Designator object. An
  unbound compositional element reference is stored (depending on
  context) either as an UnknownDesignator object or is simply not
  represented at all (e.g., a hash table entry that is simply not
  present).

  In the declarations below, each descriptor class is marked by a
  comment indicating whether it is terminal or not. In the case of
  non-terminal descriptor classes, the instance variables that describe
  the elements that may be left unbound are marked in a comment as
  "bindable". In all such cases, the collection of bindings is
  represented as a Vector which may specify the relevant bindings
  completely, incompletely, or not at all. All such element descriptors
  also have a boolean 'complete' flag which indicates whether or not the
  Vector of bindings is complete or not.

*/

/* ========================= Support Descriptors =========================== */

interface ElemAny {
    final static int BIND_TYPE = 0;
    final static int BIND_ATTRIBUTE = 1;
    final static int BIND_KIND = 2;
    final static int BIND_INGRDIENT_IMPL = 3;
    final static int BIND_PRESENCE_STRUCTURE = 4;
    final static int BIND_UNUM_STRUCTURE = 5;
    final static int BIND_PRESENCE_IMPL = 6;
    final static int BIND_UNUM_IMPL = 7;
    final static int BIND_UNIT = 8;
    final static int NAMESPACE_COUNT = 9;
}

/**
  An expression to be evaluated. This is a placeholder class until we figure
  out how to really encode these. XXX
*/
class DescExpr implements ElemAny {
    /**
    * Construct a new expression descriptor.
    */
    public DescExpr() {
    }
}

/********** Attribute **********/

/**
  Attribute value descriptor.
  Terminal.
*/
class DescAttribute implements ElemAny, IndirectElement {
    private Descriptor type; /* -> ElemAttributeType */
    private Object value;

    /**
    * Construct a new attribute.
    */
    public DescAttribute(Descriptor type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
    * Provide my type when I'm a member of an IndirectEnumeration.
    */
    public Object getElement() {
        return(type);
    }
}

/**
  Descriptor for a Pluribus 'attribute' type element.
  Terminal.
*/
class ElemAttributeType extends HaberdasheryObject implements ElemAny {
    private String name;
    private String type; /* XXX Eventually should be a Java Class object */

    /**
    * Construct a new attribute type descriptor.
    *
    * @param name The attribute type name.
    * @param type Name of a Java class identifying attribute value type.
    */
    public ElemAttributeType(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

/********** Kind **********/

/**
  Descriptor for a Pluribus 'kind' element.
  Terminal.
*/
class ElemKind extends HaberdasheryObject implements ElemAny {
    private Designator attributes[];  /* -> ElemAttribute */
    private Designator extendKinds[]; /* -> ElemKind */
    private String kindInterface;     /* Class name */
    /* XXX Eventually, the kindInterface should be stored as a Java interface
       Class object, not referenced by name. */
    
    /**
    * Construct a new kind element descriptor.
    *
    * @param extendKinds Array of Designators of the kinds this kind extends
    * @param kindInterface Name of a class describing this kind's methods
    */
    public ElemKind(Designator attributes[], Designator extendKinds[],
                    String kindInterface) {
        this.attributes = attributes;
        this.extendKinds = extendKinds;
        this.kindInterface = kindInterface;
    }

    /**
    * Enumerate the elements this kind designates.
    */
    public Enumeration designators() {
        return(new CompoundEnumeration(new ArrayEnumeration(attributes),
                                       new ArrayEnumeration(extendKinds)));
    }
}

/* Unit */

class DescUnitSymbolDef {                           /* terminal */
    boolean isImported;
    boolean isExported;
    ElemAny binding;

    DescUnitSymbolDef(boolean isImported,
                          boolean isExported,
                          ElemAny binding) {
        this.isImported = isImported;
        this.isExported = isExported;
        this.binding = binding;
    }
}

class ElemUnit implements ElemAny {             /* terminal */
    Hashtable defines[]; /* String -> DescUnitSymbolDef */
    
    ElemUnit(Hashtable defines[]) {
        this.defines = defines;
    }
}

/* ============================== Ingredients ============================== */

/* Ingredient Impl */

class DescIngredientImplNeighbor {                  /* terminal */
    String nameInIngredient;
    ElemKind neighborKind;
    boolean isPlural;
    boolean isPresence;
    
    DescIngredientImplNeighbor(String nameInIngredient,
                                   ElemKind neighborKind,
                                   boolean isPlural,
                                   boolean isPresence) {
        this.nameInIngredient = nameInIngredient;
        this.neighborKind = neighborKind;
        this.isPlural = isPlural;
        this.isPresence = isPresence;
    }
}

class ElemIngredientImpl implements ElemAny {   /* terminal */
    Hashtable attributes; /* ElemAttribute -> Object */
    ElemKind ingredientKind;
    DescIngredientImplNeighbor neighbors[];
    Class implClass;
    
    ElemIngredientImpl(Hashtable attributes,
                           ElemKind ingredientKind,
                           DescIngredientImplNeighbor neighbors[],
                           Class implClass) {
        this.attributes = attributes;
        this.ingredientKind = ingredientKind;
        this.neighbors = neighbors;
        this.implClass = implClass;
    }
}

/* =============================== Presences =============================== */

/********** Presence Structure **********/

/**
  Describe a message/interface delivery for a presence structure ingredient.
  Terminal.
*/
class DescPresenceStructureDeliver {
    private boolean presenceScope;
    private String source;
    private String target;
    
    /**
    * Construct a new delivery descriptor.
    *
    * @param presenceScope true->deliver presence msg, false->deliver unum msg
    * @param source The name of the message/interface to deliver
    * @param target The name of the method/interface to deliver it to
    */
    public DescPresenceStructureDeliver(boolean presenceScope,
                                        String source,
                                        String target) {
        this.presenceScope = presenceScope;
        this.source = source;
        this.target = target;
    };
}

/**
  Describe an ingredient within a presence structure.
  Terminal.
*/
class DescPresenceStructureIngredient implements ElemAny, IndirectElement {
    private String name;
    private Designator kind; /* -> ElemKind */
    private DescPresenceStructureDeliver delivers[];

    /**
    * Construct a new presence structure ingredient descriptor.
    *
    * @param name The role name of the ingredient within the presence.
    * @param kind What kind of ingredient it is.
    * @param delivers An array of message delivery info.
    */
    public DescPresenceStructureIngredient(String name,
                                           Designator kind,
                                           DescPresenceStructureDeliver
                                               delivers[]) {
        this.name = name;
        this.kind = kind;
        this.delivers = delivers;
    }

    /**
    * Provide my kind when I'm a member of an IndirectEnumeration.
    */
    public Object getElement() {
        return(kind);
    }
}

/**
  Descriptor for a Pluribus 'presence structure' element.
  Non-terminal.
*/
class ElemPresenceStructure extends HaberdasheryObject implements ElemAny {
    private DescAttribute attributes[];
    private Designator presenceKind; /* -> ElemKind */
    private DescPresenceStructureIngredient ingredients[]; /* bindable */
    private boolean complete;
    
    /**
    * Construct a new presence structure descriptor.
    *
    * @param attributes A mapping of attributes to their values.
    * @param presenceKind Designator of this presence's kind.
    * @param ingredients Collection of presence structure ingredients.
    * @param complete true iff structure is "complete.
    */
    public ElemPresenceStructure(DescAttribute attributes[],
                                 Designator presenceKind,
                                 DescPresenceStructureIngredient ingredients[],
                                 boolean complete) {
        this.attributes = attributes;
        this.presenceKind = presenceKind;
        this.ingredients = ingredients;
        this.complete = complete;
    }

    /**
    * Enumerate the elements this presence structure designates.
    */
    public Enumeration designators() {
        return(new CompoundEnumeration(
            new IndirectEnumeration(new ArrayEnumeration(attributes)),
            new DiscreteEnumeration(presenceKind),
            new IndirectEnumeration(new ArrayEnumeration(ingredients))));
    }
}

/********** Presence Impl **********/

class DescPresenceImplMap {                         /* terminal */
    boolean neighborScope;
    String ingredientName;
    String presenceName;
    
    DescPresenceImplMap(boolean neighborScope,
                            String ingredientName,
                            String presenceName) {
        this.neighborScope = neighborScope;
        this.ingredientName = ingredientName;
        this.presenceName = presenceName;
    }
}

class DescPresenceImplIngredient {                  /* terminal */
    String names[];
    ElemIngredientImpl ingredientImpl;
    DescPresenceImplMap maps[];
    
    DescPresenceImplIngredient(String names[],
                                   ElemIngredientImpl ingredientImpl,
                                   DescPresenceImplMap maps[]) {
        this.names = names;
        this.ingredientImpl = ingredientImpl;
        this.maps = maps;
    }
}

/**
  Descriptor for a Pluribus 'presence impl' element.
  Non-terminal.
*/
class ElemPresenceImpl extends HaberdasheryObject implements ElemAny {
    private DescAttribute attributes[];
    private Designator structure; /* -> ElemPresenceStructure */
    private DescPresenceImplIngredient ingredients[]; /* bindable */
    private String implClassName;
    private boolean complete;

    /**
    * Construct a new presence impl descriptor.
    *
    * @param attributes A mapping of attributes to their values.
    * @param structure Designator of this presence's structure.
    * @param ingredients Descriptions of this presence's ingredient impls.
    * @param implClassName Name of the Java class of presence-level ops.
    * @param complete true iff impl is "complete".
    */
    ElemPresenceImpl(DescAttribute attributes[],
                     Designator structure,
                     DescPresenceImplIngredient ingredients[],
                     String implClassName,
                     boolean complete) {
        this.attributes = attributes;
        this.structure = structure;
        this.ingredients = ingredients;
        this.implClassName = implClassName;
        this.complete = complete;
    }
}

/* ================================== Una ================================== */

/********** Unum Structure **********/

/**
  Describe a conditional presence spawning of a presence of an unum.
  Terminal.
*/
class DescPresenceCond implements ElemAny {
    private DescExpr condition;
    private String makes;

    /**
    * Construct a new presence condition descriptor.
    *
    * @param condition An expression indicating when this condition applies.
    * @param makes The name of the presence role to be spawned in this case.
    */
    public DescPresenceCond(DescExpr condition,
                            String makes) {
        this.condition = condition;
        this.makes = makes;
    }
}

/**
  Describe a presence within an unum structure.
  Terminal.
*/
class DescUnumStructurePresence implements ElemAny, IndirectElement {
    private String name;
    private Designator kind; /* -> ElemKind */
    private boolean isPrime;
    private DescPresenceCond makes[];
    
    /**
    * Construct a new unum structure presence descriptor.
    *
    * @param name The role name of the presence within the unum.
    * @param kind What kind of presence it is.
    * @param isPrime true iff this is the prime presence of the unum.
    * @param makes An array of remote presence spawning info.
    */
    public DescUnumStructurePresence(String name,
                                     Designator kind,
                                     boolean isPrime,
                                     DescPresenceCond makes[]) {
        this.name = name;
        this.kind = kind;
        this.isPrime = isPrime;
        this.makes = makes;
    }

    /**
    * Provide my kind when I'm a member of an IndirectEnumeration.
    */
    public Object getElement() {
        return(kind);
    }
}

/**
  Descriptor for a Pluribus 'unum structure' element.
  Non-terminal.
*/
class ElemUnumStructure extends HaberdasheryObject implements ElemAny {
    private DescAttribute attributes[];
    private Designator unumKind; /* -> ElemKind */
    private DescUnumStructurePresence presences[]; /* bindable */
    private boolean complete;
    
    /**
    * Construct a new unum structure descriptor.
    *
    * @param attributes A mapping of attributes to their values.
    * @param unumKind Designator of this unum's kind.
    * @param presences Collection of unum structure presences.
    * @param complete true iff structure is "complete".
    */
    public ElemUnumStructure(DescAttribute attributes[],
                             Designator unumKind,
                             DescUnumStructurePresence presences[],
                             boolean complete) {
        this.attributes = attributes;
        this.unumKind = unumKind;
        this.presences = presences;
        this.complete = complete;
    }

    /**
    * Enumerate the elements this unum structure designates.
    */
    public Enumeration designators() {
        return(new CompoundEnumeration(
            new IndirectEnumeration(new ArrayEnumeration(attributes)),
            new DiscreteEnumeration(unumKind),
            new IndirectEnumeration(new ArrayEnumeration(presences))));
    }
}

/********** Unum Impl **********/

/**
  Describe a presence within an unum impl.
  Terminal.
*/
class DescUnumImplPresence implements ElemAny, IndirectElement {
    String names[];
    Designator presenceImpl; /* -> ElemPresenceImpl */

    /**
    * Construct a new unum impl presence descriptor.
    *
    * @param names The names of the roles this presence impl will fill.
    * @param presenceImpl Designator of the presence impl to use.
    */
    public DescUnumImplPresence(String names[],
                                Designator presenceImpl) {
        this.names = names;
        this.presenceImpl = presenceImpl;
    }

    /**
    * Provide my impl when I'm a member of an IndirectEnumeration.
    */
    public Object getElement() {
        return(presenceImpl);
    }
}

/**
  Descriptor for a Pluribus 'unum impl' element.
  Non-terminal.
*/
class ElemUnumImpl extends HaberdasheryObject implements ElemAny {
    private DescAttribute attributes[];
    private Designator structure; /* -> ElemUnumStructure */
    private DescUnumImplPresence presences[]; /* bindable */
    private boolean complete;

    /**
    * Construct a new unum impl descriptor.
    *
    * @param attributes A mapping of attributes to their values.
    * @param structure Designator of this unum's structure.
    * @param presences Descriptions of this unum's presence impls.
    * @param complete true iff impl is "complete".
    */
    public ElemUnumImpl(DescAttribute attributes[],
                        Designator structure,
                        DescUnumImplPresence presences[],
                        boolean complete) {
        this.attributes = attributes;
        this.structure = structure;
        this.presences = presences;
        this.complete = complete;
    }

    /**
    * Enumerate the elements this unum impl designates.
    */
    public Enumeration designators() {
        return(new CompoundEnumeration(
            new IndirectEnumeration(new ArrayEnumeration(attributes)),
            new DiscreteEnumeration(structure),
            new IndirectEnumeration(new ArrayEnumeration(presences))));
    }
}
