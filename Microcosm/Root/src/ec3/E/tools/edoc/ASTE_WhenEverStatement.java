/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_WhenEverStatement extends SimpleNode {
  ASTE_WhenEverStatement(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_WhenEverStatement(id);
  }
}
