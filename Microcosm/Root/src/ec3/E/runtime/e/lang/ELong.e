package ec.e.lang;

import java.lang.Math;

public eclass ELong  {
    long myValue;

    /** Constructor */
    public ELong(long in_value) {
        myValue = in_value;
    }

    /** The mandatory value function */
    long value() {
        return(myValue);
    }

    /** Add two ELongs, resulting in a new ELong */
    emethod add(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue + operandValue));
        }
    }

    /** Subtract two ELongs, resulting in a new ELong */
    emethod sub(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue - operandValue));
        }
    }

    /** Multiply two ELongs, resulting in a new ELong */
    emethod mul(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue * operandValue));
        }
    }

    /** Divide two ELongs, resulting in a new ELong */
    emethod div(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue / operandValue));
        }
    }

    /** Take the modulus of two ELongs, resulting in a new ELong */
    emethod mod(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue % operandValue));
        }
    }

    /** Bitwise AND of two ELongs, resulting in a new ELong */
    emethod band(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue & operandValue));
        }
    }

    /** Bitwise OR of two ELongs, resulting in a new ELong */
    emethod bor(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue | operandValue));
        }
    }

    /** Bitwise XOR of two ELongs, resulting in a new ELong */
    emethod bxor(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(myValue ^ operandValue));
        }
    }

    /** Max of two ELongs, resulting in a new ELong */
    emethod max(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(Math.max(myValue, operandValue)));
        }
    }

    /** Min of two ELongs, resulting in a new ELong */
    emethod min(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            result <- forward(new ELong(Math.min(myValue, operandValue)));
        }
    }

    /** Negative of an ELong, resulting in a new ELong */
    emethod neg(EResult result) {
        result <- forward(new ELong(-myValue));
    }

    /** Bitwise NOT of an ELong, resulting in a new ELong */
    emethod bnot(EResult result) {
        result <- forward(new ELong(~myValue));
    }

    /** Absolute value of an ELong, resulting in a new ELong */
    emethod abs(EResult result) {
        result <- forward(new ELong(Math.abs(myValue)));
    }

    /** Cast an ELong to EDouble, resulting in a new EDouble */
    emethod asDouble(EResult result) {
        result <- forward(new EDouble((double) myValue));
    }

    /** < compare two ELongs, resulting in an EBoolean */
    emethod lt(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue < operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** <= compare two ELongs, resulting in an EBoolean */
    emethod leq(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue <= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** > compare two ELongs, resulting in an EBoolean */
    emethod gt(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue > operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** >= compare two ELongs, resulting in an EBoolean */
    emethod geq(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue >= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** == compare two ELongs, resulting in an EBoolean */
    emethod eq(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue == operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** != compare two ELongs, resulting in an EBoolean */
    emethod neq(ELong operand, EResult result) {
        ewhen operand (long operandValue) {
            if (myValue != operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }
}
