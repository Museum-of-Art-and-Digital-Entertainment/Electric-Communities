package ec.e.run;

public class LocalWeakCell extends WeakCell
{
    public LocalWeakCell (Object target)
    {
	setLocal (true);
	set (target);
    }
}


