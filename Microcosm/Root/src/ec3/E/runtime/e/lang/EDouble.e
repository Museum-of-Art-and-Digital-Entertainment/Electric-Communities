package ec.e.lang;

import java.lang.Math;

public eclass EDouble  {
    double myValue;

    /** Constructor */
    public EDouble(double in_value) {
        myValue = in_value;
    }

    /** The mandatory value function */
    double value() {
        return(myValue);
    }

    /** Add two EDoubles, resulting in a new EDouble */
    emethod dadd(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(myValue + operandValue));
        }
    }

    /** Subtract two EDoubles, resulting in a new EDouble */
    emethod dsub(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(myValue - operandValue));
        }
    }

    /** Multiply two EDoubles, resulting in a new EDouble */
    emethod dmul(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(myValue * operandValue));
        }
    }

    /** Divide two EDoubles, resulting in a new EDouble */
    emethod ddiv(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(myValue / operandValue));
        }
    }

    /** Take the modulus two EDoubles, resulting in a new EDouble */
    emethod dmod(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(myValue % operandValue));
        }
    }

    /** Max of two EDoubles, resulting in a new EDouble */
    emethod dmax(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(Math.max(myValue, operandValue)));
        }
    }

    /** Min of two EDoubles, resulting in a new EDouble */
    emethod dmin(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(Math.min(myValue, operandValue)));
        }
    }

    /** Rectangular arc tangent of two EDoubles, resulting in a new EDouble */
    emethod atan2(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(Math.atan2(myValue, operandValue)));
        }
    }

    /** Power of two EDoubles, resulting in a new EDouble */
    emethod pow(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            result <- forward(new EDouble(Math.pow(myValue, operandValue)));
        }
    }

    /** Negative of an EDouble, resulting in a new EDouble */
    emethod dneg(EResult result) {
        result <- forward(new EDouble(-myValue));
    }

    /** Absolute value of an EDouble, resulting in a new EDouble */
    emethod dabs(EResult result) {
        result <- forward(new EDouble(Math.abs(myValue)));
    }

    /** Sine of an EDouble, resulting in a new EDouble */
    emethod sin(EResult result) {
        result <- forward(new EDouble(Math.sin(myValue)));
    }

    /** Cosine of an EDouble, resulting in a new EDouble */
    emethod cos(EResult result) {
        result <- forward(new EDouble(Math.cos(myValue)));
    }

    /** Tangent of an EDouble, resulting in a new EDouble */
    emethod tan(EResult result) {
        result <- forward(new EDouble(Math.tan(myValue)));
    }

    /** Arc sine of an EDouble, resulting in a new EDouble */
    emethod asin(EResult result) {
        result <- forward(new EDouble(Math.asin(myValue)));
    }

    /** Arc cosine of an EDouble, resulting in a new EDouble */
    emethod acos(EResult result) {
        result <- forward(new EDouble(Math.acos(myValue)));
    }

    /** Arc tangent of an EDouble, resulting in a new EDouble */
    emethod atan(EResult result) {
        result <- forward(new EDouble(Math.atan(myValue)));
    }

    /** Exponential of an EDouble, resulting in a new EDouble */
    emethod exp(EResult result) {
        result <- forward(new EDouble(Math.exp(myValue)));
    }

    /** Natural logarithm of an EDouble, resulting in a new EDouble */
    emethod log(EResult result) {
        result <- forward(new EDouble(Math.log(myValue)));
    }

    /** Square root of an EDouble, resulting in a new EDouble */
    emethod sqrt(EResult result) {
        result <- forward(new EDouble(Math.sqrt(myValue)));
    }

    /** Floor of an EDouble, resulting in a new EDouble */
    emethod floor(EResult result) {
        result <- forward(new EDouble(Math.floor(myValue)));
    }

    /** Ceiling of an EDouble, resulting in a new EDouble */
    emethod ceil(EResult result) {
        result <- forward(new EDouble(Math.ceil(myValue)));
    }

    /** Integer value of an EDouble, resulting in a new EDouble */
    emethod rint(EResult result) {
        result <- forward(new EDouble(Math.rint(myValue)));
    }

    /** Cast an EDouble to EInteger, resulting in a new EInteger */
    emethod asInteger(EResult result) {
        result <- forward(new EInteger((int) myValue));
    }

    /** < compare two EDoubles, resulting in an EBoolean */
    emethod dlt(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue < operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** <= compare two EDoubles, resulting in an EBoolean */
    emethod dleq(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue <= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** > compare two EDoubles, resulting in an EBoolean */
    emethod dgt(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue > operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** >= compare two EDoubles, resulting in an EBoolean */
    emethod dgeq(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue >= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** == compare two EDoubles, resulting in an EBoolean */
    emethod deq(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue == operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** != compare two EDoubles, resulting in an EBoolean */
    emethod dneq(EDouble operand, EResult result) {
        ewhen operand (double operandValue) {
            if (myValue != operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }
}
