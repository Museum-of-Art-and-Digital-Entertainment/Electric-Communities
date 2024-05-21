/** 
 * An E class that just forwards a distributor.  Measures the fastest
 * possible e method that's used just before an ewhen. 
 */
eclass EBounceBack
{
  emethod bounce (EResult done)
    {
      done <- forward(etrue);
    }
}

/* A class that implements a fast method call for comparison to
 * EBounceBack. 
 */
class BounceBack
{
  int bounce()
  {
    return 1;
  }
}

/* Run the benchmark */ 

eclass EBounceBackDriver
{
  Benchmark javaObject; // Java object that represents this benchmark.

  EBounceBackDriver(Benchmark javaObject)
    {
      System.out.println("Bounceback driver constructor.");
      this.javaObject = javaObject;
    }

  emethod run()
    {
      EBoolean methodTestDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor methodTestDone_dist = EUniChannel.getDistributor(methodTestDone);
      System.out.println("Here come numbers for java-style function calls.");
      this <- runOrdinaryMethodCall(javaObject.getSampleCount(), 100000, methodTestDone_dist);
      ewhen methodTestDone (boolean junk)
    {
      EBoolean memberTestDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor memberTestDone_dist = EUniChannel.getDistributor(memberTestDone);
          System.out.println("Here come numbers for E message sends.");
      this <- runMessageSend(javaObject.getSampleCount(), 554, memberTestDone_dist);
      ewhen memberTestDone (boolean junk2)
        {
          // System.out.println("XXX Finished run of emethod message-send tests");
          javaObject.finishTest();
        }
    }
    }

  /* This is how Java message calls perform. */
  emethod runOrdinaryMethodCall(int samples, int iterationsPerSample, EResult doneDistributor)
    {
      int cnt;          // loop index
      int finished = 0;     // count iterations to force method return value to be used.

      BounceBack bounce = new BounceBack();
      for (; --samples >= 0; )
    {
      finished = 0;
      javaObject.startTimer(false);
      for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          finished += bounce.bounce();
        }
      javaObject.stopTimer(samples, iterationsPerSample);
    }
      // System.out.println("XXX stopped funcall benchmark");
      javaObject.reportDetail("ordinary method calls and variable updates");
      doneDistributor <- forward(etrue);
    }


  /* This is how E message sends and Ewhens perform.
   * This method recursively "calls" itself 'samples' times.  After the last call, the distributor is 
   * forwarded so that the caller can summarize runtime statistics.
   * In each call, iterationsPerSample message sends and waits are done.
   * 
   * What's measured:  
   * 1. A message send to Ebounce, which just forwards a distributor. 
   * 2. Creation of an ewhen closure (first time).
   * 3. "Loop overhead" to check completion of a sample.
   * 4. Invocation of the closure.
   */ 
  emethod runMessageSend(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      EBounceBack ebounce = new EBounceBack();

      // System.out.println("XXX started message with " + samples);
      if (--samples >= 0)
    {
      // Collect remaining samples.
      int finished = 0;
      EBoolean bounceDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor bounceDone_dist = EUniChannel.getDistributor(bounceDone);
      javaObject.startTimer(false);

      ebounce<- bounce(bounceDone_dist);
      ewhenever bounceDone (boolean dummy)
        {
          finished++;
          if (finished == iterationsPerSample)  // done with iterations for this sample
        {
          javaObject.stopTimer(samples, iterationsPerSample); // report iterations for this sample
          // System.out.println("XXX recurse");
          this <- runMessageSend(samples, iterationsPerSample,
                     allSamplesDoneDistributor);  // do next sample
        }
          else
        {
          // send next message.
          // System.out.println("XXX iterate " + finished);
          ebounce <- bounce(bounceDone_dist);
        }
        }
    }
      else
    {
      // All samples have been collected.
      // System.out.println("XXX DONE");
      javaObject.reportDetail("sequential message sends & closure executions");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }
}

class EMessageAndEWhenBenchmark extends Benchmark {
    private static boolean      firstTime = true;

    
    EMessageAndEWhenBenchmark (BenchmarkRunner runner) {
        super(runner);
    }

    public int getTestTime () {
        return (int) (24 * getSampleCount() * getSampleMillis()) / 1000;
    }

    public int getRunningTime () {
        return (int) (1.15 * getTestTime());
    }

    public long runTest () {
        int                 dummy1 = 0, dummy2 = 0, dummy3 = 0;  // occupy implicit index slots
        int                 cnt, ii, x = 0, localVar = 0;

        System.out.println("runTest");
        setSampleCount(3);
        startTest();
        (new EBounceBackDriver(this)) <- run();
        return -1;
    }

}


