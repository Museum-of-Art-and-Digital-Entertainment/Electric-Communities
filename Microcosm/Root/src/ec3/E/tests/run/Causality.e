package ec.tests.run;

/* test ewhen, forward, catch. intermediate java methods */
// run this as:
//   java ec.e.start.EBoot ec.tests.run.CauseTest CausalityTracing=true

import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.util.Humanity;

eclass CauseTest
implements ELaunchable
{
    CauseTest me;

    static private void label(String msg) {
        System.out.println("----------------------------------------");
        System.out.println(msg);
    }

    emethod go(EEnvironment env) {
        label("Initial Trace:");
        System.out.println(RtCausality.getCausalityTraceString());
        etry {
            me <- emethod3();
        } ecatch (Throwable t) {
            label("In go/ecatch:");
            System.out.println(RtCausality.getCausalityTraceString());
            System.out.println("Caught exception: " + t);
            System.out.println(Humanity.humanizeException(t));
        }
        javaMethod1();
    }

    private void javaMethod1() {
        label("In javaMethod1():");
        System.out.println(RtCausality.getCausalityTraceString());
        this <- emethod1();
    }

    emethod emethod1() {
        label("In emethod1():");
        System.out.println(RtCausality.getCausalityTraceString());
        etry {
            javaMethod2();
        } ecatch (Throwable t) {
            label("In emethod1/ecatch:");
            System.out.println(RtCausality.getCausalityTraceString());
            System.out.println("Caught exception: " + t);
            System.out.println(Humanity.humanizeException(t));
            javaMethod4();
        }
        &me <- forward(this);
        me <- emethod4();
    }

    private void javaMethod2() {
        label("In javaMethod2():");
        System.out.println(RtCausality.getCausalityTraceString());
        javaMethod3();
    }

    private void javaMethod3() {
        label("In javaMethod3():");
        System.out.println(RtCausality.getCausalityTraceString());
        this <- emethod2();
    }

    emethod emethod2() {
        label("In emethod2():");
        System.out.println(RtCausality.getCausalityTraceString());
        EBoolean b;
        &b <- forward(etrue);
        ewhen b (boolean ignored) {
            label("In emethod2/ewhen:");
            System.out.println(RtCausality.getCausalityTraceString());
            javaMethod5();
        }
        ethrow new RuntimeException("Too Many Boogers");
    }

    emethod emethod3() {
        label("In emethod3():");
        System.out.println(RtCausality.getCausalityTraceString());
        throw new RuntimeException("Too many Hoogerbeets");
    }

    emethod emethod4() {
        label("In emethod4():");
        System.out.println(RtCausality.getCausalityTraceString());
    }

    private void javaMethod4() {
        label("In javaMethod4():");
        System.out.println(RtCausality.getCausalityTraceString());
    }

    private void javaMethod5() {
        label("In javaMethod5():");
        System.out.println(RtCausality.getCausalityTraceString());
        this <- emethod5();
    }

    emethod emethod5() {
        label("In emethod5():");
        System.out.println(RtCausality.getCausalityTraceString());
    }
}
