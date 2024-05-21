package ec.ez.syntax;

/**
 * For a Parser temporary
 */
/*package*/ class Assoc {

    private Object myOptKey;
    private Object myOptValue;

    public Assoc(Object optKey, Object optValue) {
        myOptKey   = optKey;
        myOptValue = optValue;
    }

    public Object optKey()   { return myOptKey; }

    public Object optValue() { return myOptValue; }
}
