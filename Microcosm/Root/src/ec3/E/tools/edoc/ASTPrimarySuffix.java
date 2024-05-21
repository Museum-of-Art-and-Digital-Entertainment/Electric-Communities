/* JJT: 0.2.2 */

package ec.edoc;


public class ASTPrimarySuffix extends SimpleNode {
  ASTPrimarySuffix(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTPrimarySuffix(id);
  }
}
