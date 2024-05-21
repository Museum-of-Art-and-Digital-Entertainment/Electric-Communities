/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_TryStatement extends SimpleNode {
  ASTE_TryStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_TryStatement(id);
  }
}
