/*
 *  Java/E assertions.  Written by Brian Marick,
 *  June 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */


package ec.util.assertion;

/* DANGER                                                       DANGER

   The 'unassert' program will remove any invocation of ANY method
   of this class.  If you add a new non-private method, it MUST be
   one which can be removed without changing the semantics of the 
   invoking program.

   There has been talk of adding an "assertNoException" assertion that
   would act as a try...catch around the method.  That is an assertion
   the unassert program should not remove.  If that method is added,
   the code in ec.transform.assertion.UnAsserter#isAssertionToRemove
   will have to be changed.

   The unassert program also assumes that all assertions are static,
   and that they do not return a value.

   DANGER                                                       DANGER
*/

/** 
 * This class provides assertions you can add to your program.  If the
 * assertions fail, unchecked exceptions of class AssertionFailed are
 * thrown. 
 * <p>
 * Note:  an assertion that is true (does not throw) nevertheless
 * incurs the overhead of the method invocation.
 * Assertions can be removed
 * from the class file by a transforming class loader or a program
 * that rewrites the class file, as described in
 * <a href="stripping.html">Ways to Strip Assertions</a>.
 * That page also describes some 'gotchas' to beware when using
 * assertions. 
 * @see AssertionFailed
 */
public class Assertion
{
  static private final String prefix = "Internal program error: ";

  /* 
   * This variable is used to discourage a java compiler from inlining 
   * an assertion.  An inlined assertion would prevent the unasserter
   * from working.
   * I can't offhand think of a way to *prevent* inlining.
   */
  static private boolean preventInlining = false;

  /**
   * If the argument is false, throws an AssertionFailed with the message
   * "Internal program error: Assertion failed."
   */
  public static void test(boolean mustBeTrue) throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + "Assertion failed.");
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation, prefixed by "Internal program error: ".
   */
  public static void test(boolean mustBeTrue, String explanation)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation);
    }
    if (preventInlining) test(!mustBeTrue, explanation);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 + explanation1);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2,
                          Object explanation3)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2 +
                                explanation3);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2,
                          Object explanation3, Object explanation4)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2 +
                                explanation3 + explanation4);
    }
    if (preventInlining) test(!mustBeTrue);
  }


  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2,
                          Object explanation3, Object explanation4,
                          Object explanation5)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2 +
                                explanation3 + explanation4 +
                                explanation5);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2,
                          Object explanation3, Object explanation4,
                          Object explanation5, Object explanation6)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2 +
                                explanation3 + explanation4 +
                                explanation5 + explanation6);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If the argument is false, throws an AssertionFailed with the
   * given explanation arguments, concatenated as strings and prefixed
   * by "Internal program error: ".
   * <p> Use this routine when you need to avoid paying the overhead of
   * string concatenation ("+") on every test.  It does the concatenation
   * only if the test fails.
   */
  public static void test(boolean mustBeTrue, Object explanation0,
                          Object explanation1, Object explanation2,
                          Object explanation3, Object explanation4,
                          Object explanation5, Object explanation6,
                          Object explanation7)
       throws AssertionFailed {
    if (mustBeTrue == false) {
      throw new AssertionFailed(prefix + explanation0 +
                                explanation1 + explanation2 +
                                explanation3 + explanation4 +
                                explanation5 + explanation6 +
                                explanation7);
    }
    if (preventInlining) test(!mustBeTrue);
  }

  /**
   * If this method is executed, it throws an AssertionFailed with the
   * message "Internal program error:  'Unreachable' code was reached."  Plant
   * such assertions in places the program should never reach (such as
   * the default case in a switch).
   */
  public static void fail() throws AssertionFailed {
    if (preventInlining) fail();
    throw new AssertionFailed(prefix + "'Unreachable' code was reached.");
  }

  /**
   * If this method is executed, it throws an AssertionFailed with the
   * given explanation, prefixed by "Internal program error: ".
   */
  public static void fail(String explanation) throws AssertionFailed {
    if (preventInlining) fail(explanation);
    throw new AssertionFailed(prefix + explanation);
  }
}
