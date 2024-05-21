/* JJT: 0.2.2 */

package ec.edoc;


public class ASTArrayBrackets extends SimpleNode {
  ASTArrayBrackets(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTArrayBrackets(id);
  }
}
