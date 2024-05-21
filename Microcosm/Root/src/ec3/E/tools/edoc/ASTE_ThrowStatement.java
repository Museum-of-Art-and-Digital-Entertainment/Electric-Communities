/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_ThrowStatement extends SimpleNode {
  ASTE_ThrowStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_ThrowStatement(id);
  }
}
