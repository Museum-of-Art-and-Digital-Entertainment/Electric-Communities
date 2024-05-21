import ec.util.assertion.*;
import java.util.*;

class Tests
{
  public static void main (String[] args)
  {
    new Tests().tests();
  }

  void tests()
  {
    loop: for (int test = 1; ; test++)
      {
        try
          {
            switch(test)
              {
              case 1: test(true); break;
              case 2: test(false); break;
              case 3: testNoString(true); break;
              case 4: testNoString(false); break;
              case 5: fail(); break;
              case 6: failNoString(); break;
              case 7: /* defunct */ break;
              case 8: /* defunct */ break;
              case 9: /* defunct */ break;
              case 10: /* defunct */ break;
              case 11: /* defunct */ break;
              case 12: /* defunct */ break;
              case 13: /* defunct */ break;
              case 14: test2(true); break;
              case 15: test3(true); break;
              case 16: test4(true); break;
              case 17: test5(true); break;
              case 18: test6(true); break;
              case 19: test7(true); break;
              case 20: test8(true); break;
              case 21: test2(false); break;
              case 22: test3(false); break;
              case 23: test4(false); break;
              case 24: test5(false); break;
              case 25: test6(false); break;
              case 26: test7(false); break;
              case 27: test8(false); break;
              default: break loop;
              }
          }
        catch (AssertionFailed e) { System.out.println(e.getMessage()); }
      }
    // This one is uncaught, to check the stack trace.
    // Note that this assumes all printlns are flushed before
    // assertion messages are printed to stderr.
    System.out.println("=== Finishing: Expect stack trace.");
    Assertion.fail();
  }

  void test(boolean b)
  {
    System.out.println("== test");
    Assertion.test(b, "Test assertion fires.");
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void testNoString(boolean b)
  {
    System.out.println("== testNoString");
    Assertion.test(b);
    System.out.println("testNoString: Assertion does not fire because b is " + b);
  }

  void test2(boolean b)
  {
    System.out.println("== test with 2 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test3(boolean b)
  {
    System.out.println("== test with 3 explanation args");
    Assertion.test(b, "Test assertion fires.",
                   new Integer(2), new Integer(3));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test4(boolean b)
  {
    System.out.println("== test with 4 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2),
                   new Integer(3), new Integer(4));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test5(boolean b)
  {
    System.out.println("== test with 5 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2),
                   new Integer(3), new Integer(4), new Integer(5));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test6(boolean b)
  {
    System.out.println("== test with 6 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2),
                   new Integer(3), new Integer(4), new Vector(),
                   new Integer(6));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test7(boolean b)
  {
    System.out.println("== test with 7 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2),
                   new Integer(3), new Integer(4), new Vector(),
                   new Integer(6), new Long(7));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void test8(boolean b)
  {
    System.out.println("== test with 8 explanation args");
    Assertion.test(b, "Test assertion fires.", new Integer(2),
                   new Integer(3), new Integer(4), new Vector(),
                   new Integer(6), new Long(7), new Float(8));
    System.out.println("test: Assertion does not fire because b is " + b);
  }

  void fail()
  {
    System.out.println("== fail");
    Assertion.fail("Test assertion fires.");
    System.out.println("==== FAILURE ===== fail: should not be reached.");
  }

  void failNoString()
  {
    System.out.println("== failNoString");
    Assertion.fail();
    System.out.println("==== FAILURE ===== failNoString: should not be reached.");
  }

}
