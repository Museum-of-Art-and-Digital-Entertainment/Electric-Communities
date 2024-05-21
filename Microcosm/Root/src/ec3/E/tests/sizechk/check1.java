//package ec.tests.sizechk;

public class Check1a
{
  static int count = 0;

  public void hello ()
  {
	count++;
	if (count == 5000)
	{
	  System.out.println ("Hello from Check1");
	  SysUtil.javaPs ();
	}
  }
}

public class Check1b
{
  public void hello (Check1a c1a)
  {
	c1a.hello ();
  }
}

public class Check1
{
  public static void main (String args[]) 
  {
	Check1a c1a = new Check1a ();
	Check1b c1b = new Check1b ();
	for (int i = 0; i < 5000; i++)
	{
	  c1b.hello (c1a);
	}
  }
}
			  
