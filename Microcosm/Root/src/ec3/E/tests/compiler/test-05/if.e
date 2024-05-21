package ec.tests.eiffer;

eclass If
{
    emethod yo() {
        System.out.println("Is all good?");
        EBoolean truechan;
        EBoolean falsechan;
        truechan <- not(&falsechan);
        &truechan <- forward(etrue);
        eif (truechan) {
            System.out.println("All is indeed good.");
        } else {
            System.out.println("No, all is not good.");
        }

        eif (falsechan) {
            System.out.println("All is rather bad, actually.");
        } else {
            System.out.println("Yes, all is very very fine.");
        }
    }
}

public class Main
{
    static public void main(String[] args)
    {
        RtRun.bootCheat();
        If w = new If();
        w <- yo();
    }
}
