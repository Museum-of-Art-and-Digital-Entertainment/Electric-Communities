/* JJT: 0.2.2 */

package ec.edoc;


public class ASTClassDeclaration extends SimpleNode {
  ASTClassDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTClassDeclaration(id);
  }
}
