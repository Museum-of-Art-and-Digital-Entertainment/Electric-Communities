/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;
import java.util.Enumeration;

/** EClassInfo is a class used to represent pertinent information about
 *  an E class.
 *  @see ec.edoc.JavaClassInfo
 *  @see ec.edoc.ClassInfo
 */
class EClassInfo extends ClassInfo {
    /** Constructor for EClassInfo matches Info
     *  @see ec.edoc.Info#Info */
    EClassInfo(String name, Comment comment, int modifiers) {
        super(name, comment, modifiers);
        myEMethods = new Vector();
    }

    /** Constructor for EClassInfo matches ClassInterfaceInfo
     *  @see ec.edoc.ClassInterfaceInfo#ClassInterfaceInfo */
    EClassInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super(n, t);
        myEMethods = new Vector();
    }

    /** Used to store any emethods belonging to this class */
    protected Vector myEMethods = null;
    /** Add an EMethod to this class
     *  @param i; an EMethodInfo representing the method.
     *  @see ec.edoc.EMethodInfo
     */
    protected void addMember(EMethodInfo i) {
        myEMethods.addElement(i);
    }
    /** Accessor method used to get a list of emethods
     *  @return nullFatal; return Vector(EMethodInfo), possibly empty  */
    Vector emethods() {
        return myEMethods;
    }

    /** Add a member to this class
     *  This handler overrides ClassInterfaceInfo.addMember()
     *  and handles eclass specific members.
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException
     *  @see ec.edoc.ClassInterfaceInfo.html#addMember
     *  @see ec.edoc.MemberInfo.html
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {

        if (i instanceof EMethodInfo) {
            this.addMember((EMethodInfo)i);
        } else {
            super.addMember(i);
        }
    }

    /** Get the supertype of this class
     *  This overrides getExtends in ClassInfo, since an eclass which is
     *  not specified to extend anything else, should extend EObject,
     *  not Object
     *  @return; the supertype, in fully qualified form.
     */
    String getExtends() {

        if (myExtends == null) {
            /* then it hasn't been set */
            return "ec.e.run.EObject";
        }
        return myExtends;
    }


    /** This method is used to turn an eclass, containing emethods,
     *  into a collection of classes containing suitably mangled names for
     *  each method in the class.
     *
     *  If the output vector contains this eclass, then it will be removed,
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

        String newName;
        String intfName = myName + "_$_Intf";

        /* Generate interface */
        JavaInterfaceInfo intf = new JavaInterfaceInfo(intfName, myComment,
                                            PUBLIC | myModifiers);
        intf.addImplements("ec.e.run.EObject_$_Intf");


        /* Generate unnamed class */
        JavaClassInfo unnamed = new JavaClassInfo(myName, myComment,
                                                    PUBLIC);
        unnamed.addExtends("ec.e.run.EObject");
        unnamed.addImplements(intfName);


        /* Generate impl */
        JavaClassInfo impl = new JavaClassInfo(myName + "_$_Impl", myComment,
                                        PUBLIC | myModifiers);
        impl.addExtends(this.getExtends() + "_$_Impl");
        impl.addImplements(intfName);
        /* XXX might need to deal with constructors here */


        /* Generate Deflector */
        JavaClassInfo deflector = new JavaClassInfo(myName + "_$_Deflector",
                                myComment, PUBLIC | myModifiers);
        deflector.addExtends(this.getExtends() + "_$_Deflector");
        deflector.addImplements(intfName);


        /* Generate Sealer */
        JavaClassInfo sealer = new JavaClassInfo(myName + "_$_Sealer",
                                myComment, PUBLIC | myModifiers);
        sealer.addExtends("ec.e.run.RtSealer");


        /* XXX XXX XXX */
        /* Channel and proxy are deprecated */
        /* channel */
        JavaClassInfo channel = new JavaClassInfo(myName + "_$_Channel",
                                myComment, PUBLIC | myModifiers);
        channel.addExtends(this.getExtends() + "_$_Channel");
        channel.addImplements(intfName);
        /* proxy */
        JavaClassInfo proxy = new JavaClassInfo(myName + "_$_Proxy",
                                myComment, PUBLIC | myModifiers);
        proxy.addExtends(this.getExtends() + "_$_Proxy");
        proxy.addImplements(intfName);
        /* XXX end XXX */


        /* for each superinterface of this class, we need to add
         * it as a superinterface of some
         * of the generated classes.
         */
        for (Enumeration e = myImplements.elements(); e.hasMoreElements();) {

            String intface = (String) e.nextElement();

            if (guesser.guess(intface)) {
                intface = intface + "_$_Intf";
            }

            unnamed.addImplements(intface);
            intf.addImplements(intface);
        }

        /* For each emethod */
        for (Enumeration e = myEMethods.elements(); e.hasMoreElements();) {

            EMethodInfo emi = (EMethodInfo) e.nextElement();
            JavaMethodInfo asyncMethod;
            JavaMethodInfo method;

            try {

                /* unnamed */
                asyncMethod = new JavaMethodInfo(emi, guesser,
                    emi.name() + "$async", PUBLIC, unnamed);
                method = new JavaMethodInfo(emi, guesser,
                    emi.name(), PUBLIC, unnamed);

                unnamed.addMember(asyncMethod);
                unnamed.addMember(method);

                /* intf */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(intf);
                intf.addMember(asyncMethod);

                /* impl */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(impl);
                method = (JavaMethodInfo) method.clone();
                method.containingClass(impl);
                impl.addMember(asyncMethod);
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

                /* XXX XXX XXX */
                /* channel and proxy will die RSN */
                /* channel */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(channel);
                channel.addMember(asyncMethod);
                /* proxy */
                asyncMethod = (JavaMethodInfo) asyncMethod.clone();
                asyncMethod.containingClass(proxy);
                proxy.addMember(asyncMethod);


                /* XXX possibly some other methods .. ? */

            } catch (CloneNotSupportedException x) {
                throw new RuntimeException();
            }

        }

        /* for each method */
        for (Enumeration e = myMethods.elements(); e.hasMoreElements();) {

            JavaMethodInfo jmi = (JavaMethodInfo) e.nextElement();

            try {
                jmi = (JavaMethodInfo) jmi.clone();
                jmi.containingClass(impl);
                impl.addMember(jmi);

                jmi = (JavaMethodInfo) jmi.clone();
                jmi.containingClass(unnamed);
                unnamed.addMember(jmi);
            } catch (CloneNotSupportedException x) {
                throw new RuntimeException();
            }
        }

        //System.out.println("Pling!");

        /* do constructors & stuff */
        for (Enumeration e = myConstructors.elements(); e.hasMoreElements();) {
            //System.out.println("Step");
            ConstructorInfo ci = (ConstructorInfo) e.nextElement();

            /* eclass constructors are present on unnamed & impls */
            try {
                ci = (ConstructorInfo) ci.clone();
                ci.containingClass(impl);
                impl.addMember(ci);

                ci = (ConstructorInfo) ci.clone();
                ci.containingClass(unnamed);
                unnamed.addMember(ci);

            } catch (CloneNotSupportedException x) {
                throw new RuntimeException();
            }
        }

        /* deflectors must have a constructor which takes a tether
         * and an Object as
         * arguments. Since this is only for class files, not docs, it
         * doesn't matter that it's called aardvark... */
        ConstructorInfo dc = new ConstructorInfo(myName + "_$_Deflector",
            null, PUBLIC, "void", deflector);
        dc.addParameter("ec.e.run.RtTether", "aardvark");
        dc.addParameter("java.lang.Object", "brian");
        deflector.addMember(dc);

        ConstructorInfo cc = new ConstructorInfo(myName + "_$_Channel",
            null, PUBLIC, "void", deflector);
        cc.addParameter("boolean", "aardvark");
        channel.addMember(cc);

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
            PROTECTED, "ec.e.run.RtTether", sealer);
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
        /* XXX */
        output.addElement(channel);
        output.addElement(proxy);
    }



    private static String manglesealname(String name, Vector types) {

        StringBuffer sb = new StringBuffer(name);
        sb.append('(');
        int size = types.size();
        for(int i = 0; i < size; ) {
            sb.append((String) types.elementAt(i));
            if (++i < size) {
                sb.append(',');
            }
        }

        String signature = sb.toString();

        char[] result = new char[5*signature.length()];
        char[] chars = signature.toCharArray();
        int j = 0;
        for (int i = 0; i < signature.length(); i++) {
          switch (chars[i]) {
          case '(':
            result[j++] = '$';
            break;
          case ',':
            result[j++] = '$';
            break;
          case '.':
            result[j++] = '_';
            result[j++] = 'd';
            result[j++] = 'o';
            result[j++] = 't';
            result[j++] = '_';
            break;
          case '_':
            if (signature.length() >= (i+6)) {
              // skip _$_Intf, don't copy anything into result
              if (   (chars[i+1] == '$')
                  && (chars[i+2] == '_')
                  && (chars[i+3] == 'I')
                  && (chars[i+4] == 'n')
                  && (chars[i+5] == 't')
                  && (chars[i+6] == 'f')) {
                i += 6;
                break;
              }
            }
            result[j++] = '_';
            result[j++] = 'u';
            result[j++] = '_';
            break;
          case '[':
            result[j++] = '_';
            result[j++] = 'a';
            result[j++] = 'r';
            result[j++] = 'r';
            result[j++] = '_';
            break;
          case '$':
            if (signature.length() >= (i+5)) {
              // skip $async, don't copy anything into result
              if (   (chars[i+1] == 'a')
                  && (chars[i+2] == 's')
                  && (chars[i+3] == 'y')
                  && (chars[i+4] == 'n')
                  && (chars[i+5] == 'c')) {
                i += 5;
                break;
              }
            }
            result[j++] = '_';
            result[j++] = 'd';
            result[j++] = 'l';
            result[j++] = 'r';
            result[j++] = '_';
            break;
          case ' ':
            break;
          case ')':
            break;
          case ']':
            break;
          default:
            result[j++] = chars[i];
          }
        }
        return String.valueOf(result, 0, j);
    }

}