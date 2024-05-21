// HTMLElement.java
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.
//
//

package netscape.application;


/** An interface that describes what should implement an object to store
  * an HTML element.
  * @private
  */
public interface HTMLElement {
    /* This method is called with the marker in argument if appropriate. */
    public void setMarker(String aString);

    /* This method is called with the attributes in argument if appropriate */
    public void setAttributes(String attributes);

    /* This method is called with the string in argument if
     * the HTML element is a String
     */
    public void setString(String aString);

    /* This method is called if the HTMLElement is a container.
     * child[] contains some other HTML elements.
     * null is sent to this method if the container has no children
     */
    public void setChildren(Object child[]);
}

