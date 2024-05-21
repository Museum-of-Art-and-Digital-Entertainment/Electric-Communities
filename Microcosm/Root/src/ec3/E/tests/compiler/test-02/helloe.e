package ec.tests.helloe;

public class Main
{
    public static void main(String args[])
    {
        RtRun.bootCheat();
        EHello eh = new EHello();
        ekeep (null) {
            eh <- phoneHome();
        }
    }
}

eclass EHello 
{
    emethod phoneHome ()
    {
        System.out.println("Home has been phoned.");
        System.exit(0);
    }
}
