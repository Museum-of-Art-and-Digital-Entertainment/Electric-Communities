package ec.e.run;

public eclass ENull implements RtCodeable {
    public final static ENull nullValue = new ENull ();

    private ENull () {
    }   
    local String classNameToEncode (RtEncoder encoder) {
        return this.getClass().getName();
    }   
    local final void encode (RtEncoder coder) {
    }
    local final Object decode (RtDecoder coder) {
        return nullValue;
    }
    
    Object value() {
        return null;
    }
}    

