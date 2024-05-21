package ec.e.lang;

import java.lang.Math;

public eclass EFloat  {
    float myValue;

    /** Constructor */
    public EFloat(float in_value) {
        myValue = in_value;
    }

    /** The mandatory value function */
    float value() {
        return(myValue);
    }

    /** Add two EFloats, resulting in a new EFloat */
    emethod add(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(myValue + operandValue));
        }
    }

    /** Subtract two EFloats, resulting in a new EFloat */
    emethod sub(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(myValue - operandValue));
        }
    }

    /** Multiply two EFloats, resulting in a new EFloat */
    emethod mul(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(myValue * operandValue));
        }
    }

    /** Divide two EFloats, resulting in a new EFloat */
    emethod div(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(myValue / operandValue));
        }
    }

    /** Take the modulus two EFloats, resulting in a new EFloat */
    emethod mod(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(myValue % operandValue));
        }
    }

    /** Max of two EFloats, resulting in a new EFloat */
    emethod max(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(Math.max(myValue, operandValue)));
        }
    }

    /** Min of two EFloats, resulting in a new EFloat */
    emethod min(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat(Math.min(myValue, operandValue)));
        }
    }

    /** Rectangular arc tangent of two EFloats, resulting in a new EFloat */
    emethod atan2(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat((float)Math.atan2(myValue, operandValue)));
        }
    }

    /** Power of two EFloats, resulting in a new EFloat */
    emethod pow(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            result <- forward(new EFloat((float)Math.pow(myValue, operandValue)));
        }
    }

    /** Negative of an EFloat, resulting in a new EFloat */
    emethod neg(EResult result) {
        result <- forward(new EFloat(-myValue));
    }

    /** Absolute value of an EFloat, resulting in a new EFloat */
    emethod abs(EResult result) {
        result <- forward(new EFloat(Math.abs(myValue)));
    }

    /** Sine of an EFloat, resulting in a new EFloat */
    emethod sin(EResult result) {
        result <- forward(new EFloat((float)Math.sin(myValue)));
    }

    /** Cosine of an EFloat, resulting in a new EFloat */
    emethod cos(EResult result) {
        result <- forward(new EFloat((float)Math.cos(myValue)));
    }

    /** Tangent of an EFloat, resulting in a new EFloat */
    emethod tan(EResult result) {
        result <- forward(new EFloat((float)Math.tan(myValue)));
    }

    /** Arc sine of an EFloat, resulting in a new EFloat */
    emethod asin(EResult result) {
        result <- forward(new EFloat((float)Math.asin(myValue)));
    }

    /** Arc cosine of an EFloat, resulting in a new EFloat */
    emethod acos(EResult result) {
        result <- forward(new EFloat((float)Math.acos(myValue)));
    }

    /** Arc tangent of an EFloat, resulting in a new EFloat */
    emethod atan(EResult result) {
        result <- forward(new EFloat((float)Math.atan(myValue)));
    }

    /** Exponential of an EFloat, resulting in a new EFloat */
    emethod exp(EResult result) {
        result <- forward(new EFloat((float)Math.exp(myValue)));
    }

    /** Natural logarithm of an EFloat, resulting in a new EFloat */
    emethod log(EResult result) {
        result <- forward(new EFloat((float)Math.log(myValue)));
    }

    /** Square root of an EFloat, resulting in a new EFloat */
    emethod sqrt(EResult result) {
        result <- forward(new EFloat((float)Math.sqrt(myValue)));
    }

    /** Floor of an EFloat, resulting in a new EFloat */
    emethod floor(EResult result) {
        result <- forward(new EFloat((float)Math.floor(myValue)));
    }

    /** Ceiling of an EFloat, resulting in a new EFloat */
    emethod ceil(EResult result) {
        result <- forward(new EFloat((float)Math.ceil(myValue)));
    }

    /** Integer value of an EFloat, resulting in a new EFloat */
    emethod rint(EResult result) {
        result <- forward(new EFloat((float)Math.rint(myValue)));
    }

    /** Cast an EFloat to EInteger, resulting in a new EInteger */
    emethod asInteger(EResult result) {
        result <- forward(new EInteger((int) myValue));
    }

    /** < compare two EFloats, resulting in an EBoolean */
    emethod lt(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue < operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** <= compare two EFloats, resulting in an EBoolean */
    emethod leq(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue <= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** > compare two EFloats, resulting in an EBoolean */
    emethod gt(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue > operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** >= compare two EFloats, resulting in an EBoolean */
    emethod geq(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue >= operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** == compare two EFloats, resulting in an EBoolean */
    emethod eq(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue == operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }

    /** != compare two EFloats, resulting in an EBoolean */
    emethod neq(EFloat operand, EResult result) {
        ewhen operand (float operandValue) {
            if (myValue != operandValue)
                result <- forward(etrue);
            else
                result <- forward(efalse);
        }
    }
}
