package ec.e.run;

public class NetworkWeakCell extends WeakCell
{
    public NetworkWeakCell (Object target)
    {
	setLocal (false);
	set (target);
    }
}


