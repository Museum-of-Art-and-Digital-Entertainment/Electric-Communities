package minimal;

eclass Simplest
{
    emethod yo ()
    {
        System.out.println ("Ome ni kakarimasu.");
        EBoolean bchan = (EBoolean) EUniChannel.construct(EBoolean.class);
        EUniDistributor bchan_dist = EUniChannel.getDistributor(bchan);
        bchan_dist <- forward (etrue);
        ewhen bchan (boolean ignored)
        {
            System.out.println ("Dozo yoroshiku.");
            System.exit(0);
        }
    }
}

public class Test
{
    static public void main (String[] args)
    {
        RtRun.bootCheat ();
        Simplest s = new Simplest ();
        s <- yo ();
    }
}
