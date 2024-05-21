/* JJT: 0.2.2 */

package ec.edoc;


public class ASTVariableInitializer extends SimpleNode {
  ASTVariableInitializer(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTVariableInitializer(id);
  }
}
