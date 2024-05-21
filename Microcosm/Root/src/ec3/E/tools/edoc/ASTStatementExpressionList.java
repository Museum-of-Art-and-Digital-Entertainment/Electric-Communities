/* JJT: 0.2.2 */

package ec.edoc;


public class ASTStatementExpressionList extends SimpleNode {
  ASTStatementExpressionList(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTStatementExpressionList(id);
  }
}
