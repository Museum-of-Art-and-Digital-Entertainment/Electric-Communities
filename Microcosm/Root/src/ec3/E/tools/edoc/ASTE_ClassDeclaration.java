/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_ClassDeclaration extends SimpleNode {
  ASTE_ClassDeclaration(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_ClassDeclaration(id);
  }
}
