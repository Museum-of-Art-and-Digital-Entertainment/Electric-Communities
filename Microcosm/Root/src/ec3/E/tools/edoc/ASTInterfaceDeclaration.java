/* JJT: 0.2.2 */

package ec.edoc;


public class ASTInterfaceDeclaration extends SimpleNode {
  ASTInterfaceDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTInterfaceDeclaration(id);
  }
}
