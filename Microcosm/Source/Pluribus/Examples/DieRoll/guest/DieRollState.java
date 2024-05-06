package ec.pl.examples.dieroll;

import ec.e.start.Vat;

public class istDieRoll extends ec.pl.runtime.istBase {
  Vat     myVat;
  Integer myValue;

  public istDieRoll() {
    myVat = null;
    myValue = new Integer(0);
  }

  public istDieRoll(Vat vat, Integer value) {
    myVat = vat;
    myValue = value;
  }
}
