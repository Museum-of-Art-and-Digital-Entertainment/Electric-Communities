/* Command-line interface to Benchmark application.  Crude, but few will use it.
 * Brian Marick for Electric Communities, March 1997 
 * Derived from code that has the following copyright.
 */

/*
 * Copyright (c) 1996, 1997 by Doug Bell <dbell@shvn.com>.  All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.io.StreamTokenizer;
import java.io.IOException;

class OrigBenchmarkTTY implements BenchmarkRunner
{
  public static void main(String[] args)
  {
    OrigBenchmarkTTY bench = new OrigBenchmarkTTY();
    bench.go();
  }


  static final String   spaces =
                            "                                                       ";
  private int   estimatatedSeconds;
  private Thread  testTask;
  private Benchmark[]   tests = {
    new Benchmark(this),
    new MixedBenchmark(this),
    new LoopBenchmark(this),
    new VariableBenchmark(this),
    new MethodBenchmark(this),
    new OperatorBenchmark(this),
    new CastingBenchmark(this),
    new InstantiationBenchmark(this),
    new ExceptionBenchmark(this),
    new ThreadBenchmark(this),
  };

  OrigBenchmarkTTY()
  {
  }


  public synchronized void go () 
  {
    
    try
      {
    for(;;)
      runTest(getChoice());
      }
    catch (UserDone e)
      {
    System.exit(0);
      }
  }

  private int getChoice() throws UserDone
  {
    println("Pick a benchmark or type 'exit' to exit.");
    for (int i = 0; i < tests.length; i++)
      {
    String front = i + ": " + tests[i].getName();
    String back = "(~" + tests[i].getTestTime() + " secs runtime)";
    int index = front.length() + back.length();
    String  space = (index >= spaces.length()) ? " " : spaces.substring(index);
    println(front + space + back);
      }
    return getChoiceHelper();
  }

  private int getChoiceHelper() throws UserDone
  {
    print("> ");    // hack
    StreamTokenizer st = new StreamTokenizer(System.in);
    int tokenType;
    try
      {
    tokenType = st.nextToken();
      }
    catch (IOException e)
      {
    System.err.println("I/O failure");
    throw new UserDone();
      }
    switch(tokenType)
      {
      case StreamTokenizer.TT_EOF:
    throw new UserDone();
      case StreamTokenizer.TT_EOL:
    return getChoiceHelper();
      case StreamTokenizer.TT_NUMBER:
    if (0 <= st.nval && st.nval < tests.length)
      {
        return (int) st.nval;
      }
    else
      {
        println((int)st.nval + " is an invalid number.  Please try again.");
        return getChoiceHelper();
      }
      case StreamTokenizer.TT_WORD:
    if (   st.sval.equals("exit")
        || st.sval.equals("e")
        || st.sval.equals("q")
        || st.sval.equals("quit"))
      {
        throw new UserDone();
      }
    else
      {
        println("I don't understand '" + st.sval + "'.  Exit with 'exit'.");
        return getChoiceHelper();
      }
      default:
    throw new Error("Program error:  unexpected StreamTokenizer value.");
      }
  }

  private void runTest(int which)
  {
    Benchmark.recalibrate();

    int     cnt, testSeconds = 0;
    long    begin = System.currentTimeMillis();
    
    Benchmark.gc();
    println("Benchmark tests:  [mem=" + Runtime.getRuntime().freeMemory() +"/" +
        Runtime.getRuntime().totalMemory() +"]");

    long total = tests[which].runTest();
    
    println("*** done: " +
        timeString((int) (System.currentTimeMillis() + 500 - begin) / 1000) +
        " (tests " +
        timeString((int) ((total + 500) / 1000)) +
        ") ***");
  }


  static String timeString (int seconds) {
    int sec = seconds % 60;
    return (seconds / 60) + ((sec < 10) ? ":0" : ":") + sec;
  }


  public void report (String msg, long nanos) {
    if (msg != null) {
      String    time = Long.toString(nanos);
      int       index = msg.length() + time.length();
      String    space = (index >= spaces.length()) ? " " : spaces.substring(index);
      println(msg + space + time + Benchmark.resolutionName);
    }
  }

  public void println (String s) {
    System.out.println(s);
    System.out.flush();
  }

  public void print (String s) {
    System.out.print(s); 
    System.out.flush();
  }

}
