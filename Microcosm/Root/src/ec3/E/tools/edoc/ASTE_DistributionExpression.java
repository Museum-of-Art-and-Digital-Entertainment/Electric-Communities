/* JJT: 0.2.2 */

package ec.edoc;


public class ASTE_DistributionExpression extends SimpleNode {
  ASTE_DistributionExpression(String id) {
    super(id);
  }

  public static Node jjtCreate(String id) {
    return new ASTE_DistributionExpression(id);
  }
}
