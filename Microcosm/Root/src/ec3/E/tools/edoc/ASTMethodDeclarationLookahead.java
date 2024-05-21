/* JJT: 0.2.2 */

package ec.edoc;


public class ASTMethodDeclarationLookahead extends SimpleNode {
  ASTMethodDeclarationLookahead(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTMethodDeclarationLookahead(id);
  }
}
