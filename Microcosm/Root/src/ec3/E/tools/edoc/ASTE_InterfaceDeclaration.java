/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_InterfaceDeclaration extends SimpleNode {
  ASTE_InterfaceDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_InterfaceDeclaration(id);
  }
}
