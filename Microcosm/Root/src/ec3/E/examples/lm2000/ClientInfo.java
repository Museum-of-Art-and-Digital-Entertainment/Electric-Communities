
package ec.examples.lm2000;

public class ClientInfo  {
  public EClient eClient = null;
  public Person person; 
  public ClientInfo(EClient e, Person p) {
    eClient = e;
    person = p;
  }
}
