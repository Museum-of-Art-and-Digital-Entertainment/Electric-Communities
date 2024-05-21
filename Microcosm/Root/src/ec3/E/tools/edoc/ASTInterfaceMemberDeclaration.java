/* JJT: 0.2.2 */

package ec.edoc;


public class ASTInterfaceMemberDeclaration extends SimpleNode {
  ASTInterfaceMemberDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTInterfaceMemberDeclaration(id);
  }
}
