package ec.e.lang;

public eclass EString  {
    String myValue;

    /** Constructor */
    public EString(String in_value) {
        myValue = in_value;
    }

    /** The mandatory value function */
    String value() {
        return(myValue);
    }

    /** Take the length of an EString, resulting in a new EInteger */
    emethod length(EResult result) {
        result <- forward(new EInteger(myValue.length()));
    }

    /** < compare two EStrings, resulting in an EBoolean */
    emethod slt(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) < 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** <= compare two EStrings, resulting in an EBoolean */
    emethod sleq(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) <= 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** > compare two EStrings, resulting in an EBoolean */
    emethod sgt(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) > 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** >= compare two EStrings, resulting in an EBoolean */
    emethod sgeq(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) >= 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** == compare two EStrings, resulting in an EBoolean */
    emethod streq(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) == 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** != compare two EStrings, resulting in an EBoolean */
    emethod sneq(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            if (myValue.compareTo(operandValue) != 0)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** Compute a substring of an EString, resulting in new EString */
    emethod substring(EInteger start, EInteger end, EResult result) {
        ewhen start (int startValue) {
            ewhen end (int endValue) {
                result <- forward(new EString(myValue.substring(startValue,
                                                                endValue)));
            }
        }
    }

    /** Concatenate two EStrings, resulting in a new EString */
    emethod concat(EString operand, EResult result) {
        ewhen operand (String operandValue) {
            result <- forward(new EString(myValue + operandValue));
        }
    }
}
