/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_WhenStatement extends SimpleNode {
  ASTE_WhenStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_WhenStatement(id);
  }
}
