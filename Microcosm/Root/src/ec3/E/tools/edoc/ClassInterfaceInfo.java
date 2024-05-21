/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

import ec.edoc.HTMLGenerator;

/** Used to store information about Classes or interfaces */
abstract class ClassInterfaceInfo extends Info {

    /** Constructor for ClassInterfaceInfo matches Info
     *  @see ec.edoc.Info#Info */
    ClassInterfaceInfo(String name, Comment comment, int modifiers) {
        super(name, comment, modifiers);
        /* since we now hold a TypeTable for doc generation later, we want
         * to make sure it isn't null. since it's not actually relevent
         * when using this constructor, we just create an empty one. */
        myTypeTable = new TypeTable();
    }

    /** Set a super class for this class.
     *  this is implementation dependant on whether an interface (multiple
     *  extends) or a class is being declared.
     *  @param name, nullFatal; the (fully qualified) type of the supertype.
     *  @see ec.edoc.TypeTable *
     */
    abstract void addExtends(String name);
    /** Set a interface which this class / interface implements / decends from.
     *  this is implementation dependant on whether an interface (multiple
     *  extends) or a class is being declared.
     *  @param name, nullFatal; the (fully qualified) type of the interfoce.
     *  @see ec.edoc.TypeTable *
     */
    abstract void addImplements(String name);

    /** Get the supertype of this class / intorface.
     *  @return; the supertype, in fully qualified form.
     *  in the case of interface this should be Object */
    abstract String getExtends();

    /** Get the list of interfaces this class / interface implements.
     *  @return nullFatal; is Vector(String), the FQ types of all interfaces */
    abstract Vector getImplements();

    /** Get a list of fields, if they are supported by this class or interface
     *  returns null if there are none, throws an exception if this class /
     *  interface type does not support them. (eg. JavaClassInfo does not
     *  have any emethods())
     *
     *  This is notionally abstract, but if something doesn't support fields
     *  then it can inherit this implementation, which throws the exception.
     *
     *  @exception ec.edoc.MemberNotSupportedException; thrown if this class /
     *  interface type does not support fields.
     *  @return nullFatal; the collection of fields, possibly empty
     */
    Vector fields() throws MemberNotSupportedException {

        throw new MemberNotSupportedException();
    }
    /** Get a list of methods, if they are supported by this class or interface
     *  returns null if there are none, throws an exception if this class /
     *  interface type does not support them. (eg. JavaClassInfo does not
     *  have any emethods())
     *
     *  This is notionally abstract, but if something doesn't support methods
     *  then it can inherit this implementation, which throws the exception.
     *
     *  @exception ec.edoc.MemberNotSupportedException; thrown if this class /
     *  interface type does not support methods.
     *  @return nullOK; the collection of methods, null if there are none.
     */
    Vector methods() throws MemberNotSupportedException {

        throw new MemberNotSupportedException();
    }
    /** Get a list of emethods, if they are supported by this class or interface
     *  returns null if there are none, throws an exception if this class /
     *  interface type does not support them. (eg. JavaClassInfo does not
     *  have any emethods())
     *
     *  This is notionally abstract, but if something doesn't support emethods
     *  then it can inherit this implementation, which throws the exception.
     *
     *  @exception ec.edoc.MemberNotSupportedException; thrown if this class /
     *  interface type does not support emethods.
     *  @return nullFatal; the collection of emethods, possibly empty
     */
    Vector emethods() throws MemberNotSupportedException {

        throw new MemberNotSupportedException();
    }
    /** Get a list of constructors, if they are supported by this class or
     *  interface
     *  returns null if there are none, throws an exception if this class /
     *  interface type does not support them. (eg. JavaClassInfo does not
     *  have any emethods())
     *
     *  This is notionally abstract, but if something doesn't support
     *  constructors then it can inherit this implementation, which throws
     *  the exception.
     *
     *  @exception ec.edoc.MemberNotSupportedException; thrown if this class /
     *  interface type does not support constructors.
     *  @return nullFatal; the collection of constructors, possibly empty
     */
    Vector constructors() throws MemberNotSupportedException {

        throw new MemberNotSupportedException();
    }
    /** Check whether this class / interface has a static initialiser.
     *
     *  This is notionally abstract, but if something doesn't support SIs
     *  then it can inherit this implementation, which returns false.
     *
     *  @return; true if this does have a SI, false if not, or false if this
     *   type does not support SIs.
     */
    boolean hasStaticInitialiser() {

        return false;
    }

    /** Add a member to this class / interface
     *  This is effectively an abstract method, which should be overridden
     *  in each subclass by a handler which deals with anything that class
     *  understands, and calls super.addMember to pass off anything else.
     *  If the member propagates this far, thun nothing understood it, so we
     *  throw a suitable exception.
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException
     *  @see ec.edoc.MemberInfo
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {
        throw new MemberNotSupportedException();
    }

    /*********************************************************/

    /** This is used to store a pointer to the TypeTable which should
     *  be used for this class. The only reason we need this is so that
     *  we can still lookup unqualified types when we want to check
     *  the AtSee commands in the comments.
     *  @see HTMLGenerator *
     *  @see AtSee *
     */
    private TypeTable myTypeTable = null;

    /** Method to retrieve this class or interface's TypeTable.
     *  this is currently only used in the documentaion generation
     *  phase.
     *  @returns nullFatal; the TypeTable in question.
     */
    TypeTable typeTable() {
        return myTypeTable;
    }

    /*********************************************************/

    /** This constructor is used only by the subclasses (of course - this
     *  is an abstract class) It knows about the Syntax Tree (AST) for class
     *  and interface declaration.
     *
     *  @see #deferredConstructor
     */
    protected ClassInterfaceInfo() {}

    /** This method is used to perform necessary initialisation, which is
     *  logically part of the constructor, but cannot be in the constructor
     *  due to the way java handles abstract methods.
     *
     *  This should always be called during a constructor of a subclass which
     *  intends to build from an AST.
     *
     *  @see #ClassInterfaceInfo
     *  @param n, nullFatal; is expected to be class / eclass / interface /
     *   einterface decl from the syntax tree. It will have its life blood
     *   sucked out of it. :-)
     *  @param validator, nullFatal; is a TypeTable which will be used to
     *   validate names, ie. a namespace.
     *  @see ec.edoc.TypeTable
     */
    protected void deferredConstructor(SimpleNode n, TypeTable validator)
            throws MalformedASTException {

        myTypeTable = validator;

        PointerToInteger childIndex = new PointerToInteger(0);

        /* get modifiers */
        myModifiers = TreeHelper.modifiersFromFirstChildren(n, childIndex);

        /* get my name */
        myName = validator.validate(
            TreeHelper.nameFromASTIdentifier(n, childIndex));

        /* if we have any extends / implements, we'd better do them */
        if (childIndex.datum < n.jjtGetNumChildren()) {

            Node child = n.jjtGetChild(childIndex.datum++);
            if (child instanceof ASTExtends) {
                try {
                    ASTNameList nl = (ASTNameList)(child.jjtGetChild(0));
                    int numChildren = nl.jjtGetNumChildren();
                    for (int i = 0; i < numChildren; i++ ) {
                        this.addExtends(
                            validator.validate(
                                ((ASTName)(nl.jjtGetChild(i))).getName()));
                    }
                } catch (ClassCastException e) {
                    throw new MalformedASTException();
                }
                /* if we did the extends AND we still have another child,
                 * then it'll be the implements.
                 * if we didn't have an extends, then child / childIndex
                 * will already point to the implements if there is one.
                 */
                if (childIndex.datum < n.jjtGetNumChildren()) {
                    child = n.jjtGetChild(childIndex.datum++);
                }
            }

            if (child instanceof ASTImplements) {
                try {
                    ASTNameList nl = (ASTNameList)(child.jjtGetChild(0));
                    int numChildren = nl.jjtGetNumChildren();
                    for (int i = 0; i < numChildren; i++ ) {
                        this.addImplements(
                            validator.validate(
                                ((ASTName)(nl.jjtGetChild(i))).getName()));
                    }
                } catch (ClassCastException e) {
                    throw new MalformedASTException();
                }
            }
        }
    }

    protected static String mangleSealer(String name, Vector types) {

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

        //System.out.println("Mangled " + signature + " to "
        //  + String.valueOf(result, 0, j));

        return String.valueOf(result, 0, j);
    }

}

