/* JJT: 0.2.2 */

package ec.edoc;


public class ASTPrimitiveType extends SimpleNode {
  ASTPrimitiveType(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTPrimitiveType(id);
  }
}
