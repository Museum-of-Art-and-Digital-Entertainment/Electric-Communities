package ec.e.run;

public eclass EBaseBoolean implements RtCodeable {
    protected EBaseBoolean () {
    }   
    local final String classNameToEncode (RtEncoder encoder) {
        return this.getClass().getName();
    }   
    local final void encode (RtEncoder coder) {
    }
    local Object decode (RtDecoder coder) {
        throw new RtRuntimeException("Can't decode EBaseBoolean");
    }
}

public einterface EBoolean {
    and(EBoolean operand, EResult result);
    or(EBoolean operand, EResult result);
    xor(EBoolean operand, EResult result);
    eqv(EBoolean operand, EResult result);
    not(EResult result);
}

public eclass ETrue extends EBaseBoolean implements EBoolean {
    /** The one true TRUE valued EBoolean */
    public static final ETrue trueValue = new ETrue();
    private ETrue() {
    }

    /** The mandatory value function */
    protected Object value() {
        return((Object)(new Boolean(true)));
    }

    emethod and(EBoolean operand, EResult result) {
        result <- forward(operand);
    }
    emethod or(EBoolean operand, EResult result) {
        result <- forward(etrue);
    }
    emethod xor(EBoolean operand, EResult result) {
        operand <- not(result);
    }
    emethod eqv(EBoolean operand, EResult result) {
        result <- forward(operand);
    }
    emethod not(EResult result) {
        result <- forward(efalse);
    }
    
    local final Object decode (RtDecoder coder) {
        return trueValue;
    }
}    

public eclass EFalse extends EBaseBoolean implements EBoolean {
    /** The one true FALSE valued EBoolean */
    public static final EFalse falseValue = new EFalse ();

    private EFalse () {
    }
    
    /** The mandatory value function */
    protected Object value() {
        return((Object)(new Boolean(false)));
    }

    emethod and(EBoolean operand, EResult result) {
        result <- forward(efalse);
    }
    emethod or(EBoolean operand, EResult result) {
        result <- forward(operand);
    }
    emethod xor(EBoolean operand, EResult result) {
        result <- forward(operand);
    }
    emethod eqv(EBoolean operand, EResult result) {
        operand <- not(result);
    }
    emethod not(EResult result) {
        result <- forward(etrue);
    }
    
    local final Object decode (RtDecoder coder) {
        return falseValue;
    }
}    
