/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_MessageDeclaration extends SimpleNode {
  ASTE_MessageDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_MessageDeclaration(id);
  }
}
