
package ec.ifc.app;

/**
 * Interface ECRootView uses to check whether the focused view wants to
 * handle certain keys, such as the return key.
 */

public interface ECKeyFilter {
    /**
     * ECRootView calls this on the focused view when the return key
     * is pressed. If the focused view returns false, the return key is
     * used to activate the default button (if any), and the focused view
     * will not receive a keyDown message.
     * @see ECRootView
     */
    public boolean wantsReturnKey();

    /**
     * ECRootView calls this on the focused view when the escape key
     * is pressed. If the focused view returns false, the escape key is
     * used to activate the cancel button (if any), and the focused view
     * will not receive a keyDown message.
     * @see ECRootView
     */
    public boolean wantsEscapeKey();
}
