//package ec.tests.sizechk;

public eclass Check2a
{
  static int count = 0;

  emethod hello2a ()
  {
	count++;
	if (count == 2000)
	{
	  System.out.println ("Hello from Check2");
	  SysUtil.javaPs ();
	}
  }
}

public eclass Check2b
{
  emethod hello2b (EObject c2a)
  {
	c2a <- hello2a ();
  }
}

public class Check2
{
  public static void main (String args[]) 
  {
	Check2a c2a;
	Check2b c2b = new Check2b ();
	for (int i = 0; i < 2000; i++)
	{
	  c2b <- hello2b (c2a);
	}
	&c2a <- forward (new Check2a ());
  }
}
			  
