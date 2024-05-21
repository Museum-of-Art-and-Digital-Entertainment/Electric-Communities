/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_TypeDeclarationParameter extends SimpleNode {
  ASTE_TypeDeclarationParameter(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_TypeDeclarationParameter(id);
  }
}
