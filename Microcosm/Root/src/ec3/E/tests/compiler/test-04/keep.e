package ec.tests.keep;

public class Main
{
    static public void main(String[] args) {
        RtRun.bootCheat();
        ekeep (null) {
            Keep k = new Keep();
            k <- go();
        }
    }
}

eclass Keep
{
    RtExceptionEnv nullEnv;
    RtExceptionEnv envA;
    RtExceptionEnv envB;

    emethod throwAgain(String msg) {
        ethrow new RuntimeException(msg);
    }

    emethod go() {
        ethrow new RuntimeException("1/nullEnv");
        this <- throwAgain("3/nullEnv");
        nullEnv = myKeeper();
        etry {
            envA = myKeeper();
            ethrow new RuntimeException("2/envA");
            ekeep (nullEnv) {
                this <- throwAgain("4/nullEnv");
            }
            this <- throwAgain("5/envA");
            this <- go2();
        } ecatch (RuntimeException e) {
            System.err.println("Caught in envA: " + e);
            e.printStackTrace();
        }
    }

    emethod go2() {
        ethrow new RuntimeException("6/envA");
        this <- throwAgain("8/envA");
        etry {
            envB = myKeeper();
            ethrow new RuntimeException("7/envB");
            this <- throwAgain("9/envB");
            ekeep (null) {
                this <- go3();
            }
        } ecatch (RuntimeException e) {
            System.err.println("Caught in envB: " + e);
            e.printStackTrace();
        }
    }

    emethod go3() {
        ethrow new RuntimeException("10/nullEnv");
        this <- throwAgain("15/nullEnv");
        ekeep (envA) {
            ethrow new RuntimeException("11/envA");
            this <- throwAgain("16/envA");
            ekeep (envB) {
                ethrow new RuntimeException("12/envB");
                this <- throwAgain("17/envB");
            }
            ethrow new RuntimeException("13/envA");
            this <- throwAgain("18/envA");
        }
        ethrow new RuntimeException("14/nullEnv");
        this <- throwAgain("19/nullEnv");
    }
}
