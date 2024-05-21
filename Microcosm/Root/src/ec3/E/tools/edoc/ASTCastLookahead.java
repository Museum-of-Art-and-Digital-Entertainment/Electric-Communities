/* JJT: 0.2.2 */

package ec.edoc;


public class ASTCastLookahead extends SimpleNode {
  ASTCastLookahead(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTCastLookahead(id);
  }
}
