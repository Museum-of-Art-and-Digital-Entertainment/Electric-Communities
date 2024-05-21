/* JJT: 0.2.2 */

package ec.edoc;


public class ASTInformation extends SimpleNode {
  ASTInformation(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTInformation(id);
  }
}
