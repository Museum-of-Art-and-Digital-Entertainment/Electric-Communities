/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_IfStatement extends SimpleNode {
  ASTE_IfStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_IfStatement(id);
  }
}
