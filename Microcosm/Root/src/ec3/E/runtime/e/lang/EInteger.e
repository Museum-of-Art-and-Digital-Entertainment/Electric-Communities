package ec.e.lang;

import java.lang.Math;

public eclass EInteger  {
    int myValue;

    /** Constructor */
    public EInteger(int in_value) {
        myValue = in_value;
    }

    /** The mandatory value function */
    int value() {
        return(myValue);
    }

    /** Add two EIntegers, resulting in a new EInteger */
    emethod add(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue + operandValue));
        }
    }

    /** Subtract two EIntegers, resulting in a new EInteger */
    emethod sub(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue - operandValue));
        }
    }

    /** Multiply two EIntegers, resulting in a new EInteger */
    emethod mul(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue * operandValue));
        }
    }

    /** Divide two EIntegers, resulting in a new EInteger */
    emethod div(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue / operandValue));
        }
    }

    /** Take the modulus two EIntegers, resulting in a new EInteger */
    emethod mod(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue % operandValue));
        }
    }

    /** Bitwise AND of two EIntegers, resulting in a new EInteger */
    emethod band(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue & operandValue));
        }
    }

    /** Bitwise OR of two EIntegers, resulting in a new EInteger */
    emethod bor(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue | operandValue));
        }
    }

    /** Bitwise XOR of two EIntegers, resulting in a new EInteger */
    emethod bxor(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(myValue ^ operandValue));
        }
    }

    /** Max of two EIntegers, resulting in a new EInteger */
    emethod max(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(Math.max(myValue, operandValue)));
        }
    }

    /** Min of two EIntegers, resulting in a new EInteger */
    emethod min(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            result <- forward(new EInteger(Math.min(myValue, operandValue)));
        }
    }

    /** Negative of an EInteger, resulting in a new EInteger */
    emethod neg(EResult result) {
        result <- forward(new EInteger(-myValue));
    }

    /** Bitwise NOT of an EInteger, resulting in a new EInteger */
    emethod bnot(EResult result) {
        result <- forward(new EInteger(~myValue));
    }

    /** Absolute value of an EInteger, resulting in a new EInteger */
    emethod abs(EResult result) {
        result <- forward(new EInteger(Math.abs(myValue)));
    }

    /** Cast an EInteger to EDouble, resulting in a new EDouble */
    emethod asDouble(EResult result) {
        result <- forward(new EDouble((double) myValue));
    }

    /** < compare two EIntegers, resulting in an EBoolean */
    emethod lt(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue < operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** <= compare two EIntegers, resulting in an EBoolean */
    emethod leq(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue <= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** > compare two EIntegers, resulting in an EBoolean */
    emethod gt(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue > operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** >= compare two EIntegers, resulting in an EBoolean */
    emethod geq(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue >= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** == compare two EIntegers, resulting in an EBoolean */
    emethod eq(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue == operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** != compare two EIntegers, resulting in an EBoolean */
    emethod neq(EInteger operand, EResult result) {
        ewhen operand (int operandValue) {
            if (myValue != operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }
}
