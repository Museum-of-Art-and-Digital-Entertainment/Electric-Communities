/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_MethodDeclaration extends SimpleNode {
  ASTE_MethodDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_MethodDeclaration(id);
  }
}
