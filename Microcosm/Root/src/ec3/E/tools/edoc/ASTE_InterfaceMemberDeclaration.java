/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_InterfaceMemberDeclaration extends SimpleNode {
  ASTE_InterfaceMemberDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_InterfaceMemberDeclaration(id);
  }
}
