/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;
import java.util.Enumeration;

/** EInterfaceInfo is a class used to represent pertinent information about
 *  an E interface.
 *  @see ec.edoc.JavaInterfaceInfo
 *  @see ec.edoc.InterfaceInfo
 */
class EInterfaceInfo extends InterfaceInfo {
    /** Constructor for EInterfaceInfo matches Info
     *  @see ec.edoc.Info#Info */
    EInterfaceInfo(String name, Comment comment, int mods) {
        super(name, comment, mods);
        myEMethods = new Vector();
    }

    /** Constructor for EInterfaceInfo matches ClassInterfaceInfo
     *  @see ec.edoc.ClassInterfaceInfo#ClassInterfaceInfo */
    EInterfaceInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super(n, t);
        myEMethods = new Vector();
    }

    /** Add a member to this interface
     *  This handler overrides ClassInterfaceInfo.addMember()
     *  and handles class specific members.
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException
     *  @see ec.edoc.ClassInterfaceInfo.html#addMember
     *  @see ec.edoc.MemberInfo.html
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {
        //System.out.println("addMember");

        if (i instanceof EMethodInfo) {
            this.addMember((EMethodInfo)i);
        } else {
            super.addMember(i);
        }
    }

    /** Used to store any emethods belonging to this interface */
    protected Vector myEMethods = null;
    /** Add an EMethod to this interface
     *  @param i; an EMethodInfo representing the method.
     *  @see ec.edoc.EMethodInfo
     */
    void addMember(EMethodInfo i) {
        //System.out.println("addMember for emethod");

        myEMethods.addElement(i);
    }
    /** Accessor method used to get a list of emethods
     *  @return nullFatal; return Vector(EMethodInfo), possibly empty */
    Vector emethods() {
        return myEMethods;
    }

    /** This method is used to turn an einterface, containing emethods,
     *  into a collection of java classes containing suitably mangled names for
     *  each method in the class.
     *
     *  If the output vector contains this einterface, then it will be removed,
     *  thus replacing itself by it's java equivalent.
     *
     *  @param output, nullFatal; is Vector(ClassInterfaceInfo), is the
     *   collection to which the appropriate classes will be added to.
     *
     *  @note; there is still a serious problem here, caused by the way
     *   eclasses are handled. in a source file any reference to the type
     *   of an eclass is transformed into a reference to the interface
     *   foo_$_Intf. Since it is impossible to look at a source file in
     *   isolation and tell whether classes are java or e, we cannot know
     *   whether to replace a type with type_$_Intf. Dan is talking to germany
     *   about a workaround to this problem, but it's not going to work properly
     *   until either germany do that, or edoc learns about rummaging through
     *   the file system to look for type information.
     */
    void explode(Vector output, TypeGuesser guesser) {

        //System.out.println("Explode interface");

        String newName;
        String intfName = myName + "_$_Intf";

        /* Generate interface */
        JavaInterfaceInfo intf = new JavaInterfaceInfo(intfName, myComment,
                                                    PUBLIC);
        intf.addImplements("ec.e.run.EObject_$_Intf");


        /* Generate unnamed class */
        JavaInterfaceInfo unnamed = new JavaInterfaceInfo(myName, myComment,
                                                    PUBLIC);
        unnamed.addImplements("ec.e.run.EObject_$_Intf");
        unnamed.addImplements(intfName);


        /* Generate impl */
        JavaInterfaceInfo impl = new JavaInterfaceInfo(myName + "_$_Impl",
                                            myComment, PUBLIC);
        /* XXX might need to deal with constructors here */


        /* Generate Deflector */
        JavaClassInfo deflector = new JavaClassInfo(myName + "_$_Deflector",
                                myComment, PUBLIC | myModifiers);
        deflector.addExtends("ec.e.run.EObject_$_Deflector");
        deflector.addImplements(intfName);


        /* Generate Sealer */
        JavaClassInfo sealer = new JavaClassInfo(myName + "_$_Sealer",
                                myComment, PUBLIC | myModifiers);
        sealer.addExtends("ec.e.run.RtSealer");

        /* for each superinterface of this interface, we need to add to some
         * of the generated clases.
         *
         * This doesn't suffer the same problem that the eclass explode does,
         * since an einterface can only ever extend an einterface.
         * XXX (is this true ? - certainly it can't extend anything with
         *      java methods...)
         */
        for (Enumeration e = myImplements.elements(); e.hasMoreElements();) {
            String intface = (String) e.nextElement();
            unnamed.addImplements(intface);
            intf.addImplements(intface + "_$_Intf");
            impl.addImplements(intface + "_$_Impl");
        }

        //System.out.println("Emethods; " + myEMethods);
        /* For each emethod */
        for (Enumeration e = myEMethods.elements(); e.hasMoreElements();) {

            //System.out.println("Explode emethods");

            EMethodInfo emi = (EMethodInfo) e.nextElement();
            JavaMethodInfo asyncMethod;

            try {

                /* unnamed */
                asyncMethod = new JavaMethodInfo(emi, guesser,
                    emi.name() + "$async", PUBLIC | ABSTRACT, unnamed);
                unnamed.addMember(asyncMethod);
                //System.out.println(emi);
                //System.out.println(asyncMethod);
                //System.out.println(unnamed.methods());
                /* intf */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(intf);
                intf.addMember(asyncMethod);

                /* impl */
                JavaMethodInfo method = new JavaMethodInfo(emi, guesser,
                    emi.name(), PUBLIC | ABSTRACT, unnamed);
                method.containingClass(impl);
                impl.addMember(method);

                /* deflector */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(deflector);
                deflector.addMember(asyncMethod);

                /* sealer */
                newName = mangleSealer(emi.name(), emi.parameterTypes());
                FieldInfo field = new FieldInfo("sealer_$_" + newName, null,
                    PUBLIC | STATIC | FINAL, sealer.name(), sealer);
                sealer.addMember(field);
                JavaMethodInfo sealMethod = new JavaMethodInfo(
                    "seal_$_" + newName, null, PUBLIC | STATIC | FINAL,
                    "ec.e.run.RtEnvelope", sealer);
                for (Enumeration types = emi.parameterTypes().elements(),
                        names = emi.parameterNames().elements();
                        types.hasMoreElements(); ) {

                    String t = (String) types.nextElement();
                    String n = (String) names.nextElement();

                    if (guesser.guess(t)) {
                        t = t + "_$_Intf";
                    }

                    sealMethod.addParameter(t, n);
                }
                sealer.addMember(sealMethod);

                /* XXX possibly some other methods .. ? */

            } catch (CloneNotSupportedException x) {
                throw new RuntimeException();
            }

        }

        /* einterfaces do not have methods */

        /* do constructors & stuff */

        /* deflectors must have a constructor which takes a tether as
         * arguments. Since this is only for class files, not docs, it
         * doesn't matter that it's called aardvark... */
        ConstructorInfo dc = new ConstructorInfo(myName + "_$_Deflector",
            null, PUBLIC, "void", deflector);
        dc.addParameter("ec.e.run.RtTether", "aardvark");
        deflector.addMember(dc);

        /* sealer stuff */
        ConstructorInfo sc = new ConstructorInfo(myName + "_$_Sealer",
            null, PUBLIC, "void", sealer);
        sc.addParameter("int", "george");
        sc.addParameter("java.lang.String", "geoffrey");
        sealer.addMember(sc);

        JavaMethodInfo invoke = new JavaMethodInfo("invoke", null, PUBLIC,
            "void", sealer);
        invoke.addParameter("java.lang.Object", "zippy");
        invoke.addParameter("java.lang.Object[]", "bungle");
        sealer.addMember(invoke);

        JavaMethodInfo other = new JavaMethodInfo("otherSealer", null,
            PROTECTED, "ec.e.run.RtSealer", sealer);
        other.addParameter("int", "zebbedee");
        sealer.addMember(other);

        /* possibly some public static method ..._$_Sealer(int) */


        /* remove this eclass from the vector if present */
        output.removeElement(this);
        /* replace it with the java versions .... */
        output.addElement(unnamed);
        output.addElement(impl);
        output.addElement(intf);
        output.addElement(deflector);
        output.addElement(sealer);
    }



}

