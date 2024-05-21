/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_SendStatement extends SimpleNode {
  ASTE_SendStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_SendStatement(id);
  }
}
