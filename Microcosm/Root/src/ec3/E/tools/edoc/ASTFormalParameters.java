/* JJT: 0.2.2 */

package ec.edoc;


public class ASTFormalParameters extends SimpleNode {
  ASTFormalParameters(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTFormalParameters(id);
  }
}
