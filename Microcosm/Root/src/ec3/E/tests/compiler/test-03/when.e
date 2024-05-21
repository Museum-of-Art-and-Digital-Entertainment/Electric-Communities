package ec.tests.when;

eclass When
{
    emethod yo ()
    {
        System.out.println ("Ome ni kakarimasu.");
        EBoolean bchan;
        &bchan <- forward (etrue);
        ewhen bchan (boolean ignored)
        {
            System.out.println ("Dozo yoroshiku.");
            System.exit(0);
        }
    }
}

public class Main
{
    static public void main (String[] args)
    {
        RtRun.bootCheat ();
        When w = new When ();
        w <- yo ();
    }
}
