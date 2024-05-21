package ec.tests.misc;
import ec.e.comm.*;

import ec.e.lang.*;

public class FibWrap
{
    public static void main(String args[]) {
        RtLauncher.launch(new Fib(), args);
    }
}

public eclass Fib implements ELaunchable {

    emethod go (RtEEnvironment env) {
        Integer nint = env.getPropertyAsInteger("n");   
        if (nint == null) {
            System.out.println("Charles says specify n=cardinality");
            System.exit(0);
        }
        int n = nint.intValue();
        EInteger result;
        FibonaccE fibObject = new FibonaccE();
        fibObject <- fibonacci (n, &result);
        ewhen result (long fib) {
            System.out.println("The " + n + FibUtility.getSuffixForNumber(n) +
                            " number of the Fibonacci sequence is " + fib);
        }
    }

}

eclass FibonaccE {

    final static ELong EOne = new ELong(1);
    final static ELong EZero = new ELong(0);

    emethod fibStep (ELong n, ELong last, ELong current,
            EResult result) {

        EBoolean MoreIterations;
        n <- gt (EZero, &MoreIterations);

        eif (MoreIterations) {
            ELong newN;
            ELong newCurrent;
            last <- add (current, &newCurrent);
            n <- sub (EOne, &newN);
    
            this <- fibStep (newN, current, newCurrent, result);
        } else {
            result <- forward (last);
        }

    }

    emethod fibonacci (long n, EResult result) {

        this <- fibStep (new ELong(n), EZero, EOne, result);

    }

}

class FibUtility {
     public static String getSuffixForNumber (int n) {
        int m = n % 100;
        switch (m) {
            case 11:
            case 12:
                return "th";
            default:
            m = m % 10;
            switch (m) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
            }
        }
        return "th";
    }
}

