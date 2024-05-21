

public class Main {
  public static void main(String argv[])
    {
      /* allocate a parser object */
      parser parse_obj = new parser();

      /* prompt the user */
      System.out.println("Reading expressions from standard input...");

      /* deterimine if we doing debug or normal parse, and do it */
      if (argv.length >= 1 && argv[0].equals("-debug"))
	{
	  parse_obj.debug_parse();
	}
      else
	{
	  parse_obj.parse();
	}
    }
};
