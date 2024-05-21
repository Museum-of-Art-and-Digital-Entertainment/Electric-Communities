/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;
/* JJT: 0.2.2 */

/** This class provides a supertype for all nodes used in the system.
 *  it as the default SimpleNode providing methods required by javacc, 
 *  but has been extended (see the end) to add some methods for holding
 *  a token, used to pass information from the parser to the rest of the 
 *  program.
 */
public class SimpleNode implements Node {
    protected Node parent;
    protected java.util.Vector children;
    protected String identifier;
    protected Object info = null;
    
    public SimpleNode(String id) {
        identifier = id;
    }
  
    public static Node jjtCreate(String id) {
        return new SimpleNode(id);
    }
    
    public void jjtOpen() {}
    public void jjtClose() {
        if (children != null) {
            children.trimToSize();
        }
    }
    
    public void jjtSetParent(Node n) { parent = n; }
    public Node jjtGetParent() { return parent; }
  
    public void jjtAddChild(Node n) {
        if (children == null) {
            children = new java.util.Vector();
        }
        children.addElement(n);
    }
    public Node jjtGetChild(int i) {
        return (Node)children.elementAt(i);
    }
  
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.size();
    }
  
    /* These two methods provide a very simple mechanism for attaching
       arbitrary data to the node. */

    public void setInfo(Object i) { info = i; }
    public Object getInfo() { return info; }

    /* You can override these two methods in subclasses of SimpleNode to
       customize the way the node appears when the tree is dumped.  If
       your output uses more than one line you should override
       toString(String), otherwise overriding toString() is probably all
       you need to do. */

    public String toString() { 
        return ( (info == null) ? identifier : identifier +":"+ info.toString()); 
    }

    public String toString(String prefix) { return prefix + toString(); }

    /* Override this method if you want to customize how the node dumps
         out its children. */

    public void dump(String prefix) {
        System.out.println(toString(prefix) + 
            (info != null ? info.toString() : ""));
        if (children != null) {
            for (java.util.Enumeration e = children.elements();
                    e.hasMoreElements();) {
                SimpleNode n = (SimpleNode)e.nextElement();
                n.dump(prefix + " ");
            }
        }
    }

    /* Manually added code starts here */

    /* Added a token field into SimpleNodes. This is used to correllate the
       list of tokens with the parse tree */
  
    protected Token token;
    public void setToken(Token t) {
        token = t;
    }
    public Token getToken() {
        return token;
    }
    
    /* *This method returns the line number of the first token of this 
     *  node, if known, as a string. Otherwise returns "unknown line"*/
    public String lineNumberString() {
        return (token == null) ? "unknown line" : "line "+token.beginLine;
    }
  
}

