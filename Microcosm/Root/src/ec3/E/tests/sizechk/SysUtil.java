//package ec.tests.sizechk;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Vector;

public class SysUtil
{
  public static void printStringArray (String[] strings)
  {
	int i;
	for (i = 0; i < strings.length; i++)
	{
	  System.out.println (strings[i]);
	}
  }

  public static String oneLineSysCmd (String cmd)
  {
	String result = "";

	try
	{
	  Process proc = Runtime.getRuntime ().exec (cmd);
	  DataInputStream in = new DataInputStream (proc.getInputStream ());
	  result = in.readLine ();
	}
	catch (Exception e)
	{
	  // ignore it
	}
	return (result);
  }
  
  public static String[] sysCmd (String cmd)
  {
	Vector lines = new Vector ();

	try
	{
	  Process proc = Runtime.getRuntime ().exec (cmd);
	  DataInputStream in = new DataInputStream (proc.getInputStream ());
	  for (;;)
	  {
		String line = in.readLine ();
		if (line == null)
		{
		  break;
		}
		lines.addElement (line);
	  }
	}
	catch (Exception e)
	{
	  // ignore it
	}
	
	String[] result = new String[lines.size ()];
	lines.copyInto (result);
	return (result);
  }

  public static String[] fgrep (String[] source, String filter)
  {
	Vector lines = new Vector ();
	int i;
	for (i = 0; i < source.length; i++)
	{
	  if (source[i].indexOf (filter) != -1)
	  {
		lines.addElement (source[i]);
	  }
	}

	String[] result = new String[lines.size ()];
	lines.copyInto (result);
	return (result);
  }

  // specialized routine just for the tests
  public static void javaPs ()
  {
	String whoami = oneLineSysCmd ("/usr/ucb/whoami");
	String[] ps = sysCmd ("/bin/ps -A -o user -o vsz -o osz -o args");
	ps = fgrep (ps, "Check");
	ps = fgrep (ps, whoami);
	printStringArray (ps);
  }
}
