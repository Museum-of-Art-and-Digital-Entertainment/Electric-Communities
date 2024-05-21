import java.util.Vector;

eclass EClosureDriver
{
  Benchmark javaObject; // Java object that represents this benchmark.

  EClosureDriver(Benchmark javaObject)
    {
      this.javaObject = javaObject;
    }

  emethod run(int firstTestToRun)
    {
      EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor done_dist = EUniChannel.getDistributor(done);
      EBounceBack bounceBack = new EBounceBack();
      bounceBack <- bounce(done_dist);  // start the "recursion".
      ewhenever done (boolean dummy)
    {
      switch(firstTestToRun++)
        {
        case 0:
          System.out.println("Empty Ordinary Loop Overhead.");
          this <- runOrdinaryOverhead(javaObject.getSampleCount(), 100000, done_dist);
          break;
        case 1:
          System.out.println("Starting Closure Overhead test.");
          this <- runClosureOverhead(javaObject.getSampleCount(), 100000, done_dist);
          break;

        case 2:
          System.out.println("Starting Ordinary Primitive Local Update test.");
          this <- runOrdinaryPrimitiveLocalUpdate(javaObject.getSampleCount(), 100000, done_dist);
          break;
        case 3:
          System.out.println("Starting Closure Primitive Local Update test.");
          this <- runClosurePrimitiveLocalUpdate(javaObject.getSampleCount(), 100000, done_dist);
          break;

        case 4:
          System.out.println("Starting Ordinary Primitive Data Member Update test.");
          this <- runOrdinaryPrimitiveDataMemberUpdate(javaObject.getSampleCount(), 100000, done_dist);
          break;
        case 5:
          System.out.println("Starting Closure Primitive Data Member Update test.");
          this <- runClosurePrimitiveDataMemberUpdate(javaObject.getSampleCount(), 100000, done_dist);
          break;

        case 6:
          System.out.println("Starting Ordinary Vector Update test.");
          this <- runOrdinaryVectorUpdate(javaObject.getSampleCount(), 3000, done_dist);
          break;
        case 7:
          System.out.println("Starting Closure Vector Update test.");
          this <- runClosureVectorUpdate(javaObject.getSampleCount(), 3000, done_dist);
          break;

        default:
          javaObject.finishTest();
        }
    }
    }

  // CHECKING OVERHEAD

  // OPERATIONS NOT WITHIN CLOSURES

  // This measures loop overhead of the form used in the remaining benchmarks. 
  emethod runOrdinaryOverhead(int samples, int iterationsPerSample, EResult doneDistributor)
    {
      int cnt;          // loop index
      int incrementMe = 0;
      int sample;

      for (sample = samples; --sample >= 0; )
    {
      javaObject.startTimer(false);
      for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
        }
      javaObject.stopTimer(sample, iterationsPerSample);
    }
      javaObject.reportDetail("loop overhead for non-closed loop");

      doneDistributor <- forward(etrue);
    }

  // Java update of a local variable.
  emethod runOrdinaryPrimitiveLocalUpdate(int samples, int iterationsPerSample, EResult doneDistributor)
    {
      int cnt;          // loop index
      int incrementMe = 0;
      int sample;

      for (sample = samples; --sample >= 0; )
    {
      javaObject.startTimer(false);
      for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          incrementMe++;
        }
      javaObject.stopTimer(sample, iterationsPerSample);
    }
      javaObject.reportDetail("ordinary local variable increment");

      doneDistributor <- forward(etrue);
    }

  // Java update of object data member. 
  int dataMemberIncrementMe = 0;
  emethod runOrdinaryPrimitiveDataMemberUpdate(int samples, int iterationsPerSample, EResult doneDistributor)
    {
      int cnt;          // loop index
      int sample;

      for (sample = samples; --sample >= 0; )
    {
      javaObject.startTimer(false);
      for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          dataMemberIncrementMe++;
        }
      javaObject.stopTimer(sample, iterationsPerSample);
    }
      javaObject.reportDetail("ordinary data member increment");

      doneDistributor <- forward(etrue);
    }

  // Java update of a Vector.
  emethod runOrdinaryVectorUpdate(int samples, int iterationsPerSample, EResult doneDistributor)
    {
      Vector incrementMyElement = new Vector();
      int sample;

      incrementMyElement.addElement(new Integer(0));

      for (sample = samples; --sample >= 0; )
    {
      int cnt;          // loop index
      incrementMyElement.setElementAt(new Integer(0), 0);
      javaObject.startTimer(false);
      for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          int curval = ((Integer)(incrementMyElement.elementAt(0))).intValue();
          incrementMyElement.setElementAt(new Integer(curval + 1), 0);
        }
      javaObject.stopTimer(sample, iterationsPerSample);
    }
      javaObject.reportDetail("Incrementing an ordinary Vector element.");
      doneDistributor <- forward(etrue);
    }

  // OPERATIONS WITHIN CLOSURES

  // This checks for oddities like a compiler that optimizes out useless
  // loops, and whether loop overhead is higher inside a closure than
  // the enclosing benchmark assumes.
  // Note that iterationsPerSample is closed over, but that doesn't
  // affect the loop runtime.  I suspect that's because it gets put in
  // a "register", so is only accessed once.  Don't know the JVM.
  emethod runClosureOverhead(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      if (--samples >= 0)
    {
      // Collect remaining samples.
      EBoolean bounceDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor bounceDone_dist = EUniChannel.getDistributor(bounceDone);
      EBounceBack ebounce = new EBounceBack(); // just a way to invoke the closure.

      ebounce<- bounce(bounceDone_dist);
      ewhen bounceDone (boolean dummy)
        {
          int cnt;
          javaObject.startTimer(false);
          for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
        }
          javaObject.stopTimer(samples, iterationsPerSample);
          this <- runClosureOverhead(samples, iterationsPerSample, allSamplesDoneDistributor);
        }
    }
      else
    {
      // All samples have been collected.
      // System.out.println("XXX DONE");
      javaObject.reportDetail("an empty loop within a closure (should be roughly zero)");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }


  // frame-allocated variable.
  emethod runClosurePrimitiveLocalUpdate(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      if (--samples >= 0)
    {
      // Collect remaining samples.
      EBoolean bounceDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor bounceDone_dist = EUniChannel.getDistributor(bounceDone);
      int incrementMe = 0;
      EBounceBack ebounce = new EBounceBack(); // just a way to invoke the closure.

      ebounce<- bounce(bounceDone_dist);
      ewhen bounceDone (boolean dummy)
        {
          int cnt;
          javaObject.startTimer(false);
          for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          incrementMe++;
        }
          javaObject.stopTimer(samples, iterationsPerSample);
          this <- runClosurePrimitiveLocalUpdate(samples, iterationsPerSample, allSamplesDoneDistributor);
        }
    }
      else
    {
      // All samples have been collected.
      // System.out.println("XXX DONE");
      javaObject.reportDetail("incrementing a closed-over int local to the method");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }

  // Data member - expect no difference due to being in a closure.
  int dataMemberIncrementMeToo = 0;
  emethod runClosurePrimitiveDataMemberUpdate(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      if (--samples >= 0)
    {
      // Collect remaining samples.
      EBoolean bounceDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor bounceDone_dist = EUniChannel.getDistributor(bounceDone);
      EBounceBack ebounce = new EBounceBack(); // just a way to invoke the closure.

      ebounce<- bounce(bounceDone_dist);
      ewhen bounceDone (boolean dummy)
        {
          int cnt;
          javaObject.startTimer(false);
          for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          dataMemberIncrementMeToo++;
        }
          javaObject.stopTimer(samples, iterationsPerSample);
          this <- runClosurePrimitiveDataMemberUpdate(samples, iterationsPerSample, allSamplesDoneDistributor);
        }
    }
      else
    {
      // All samples have been collected.
      // System.out.println("XXX DONE");
      javaObject.reportDetail("incrementing a closed-over int that's a data member");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }


  // Incrementing a vector.  Expect no difference from being in a closure.
  emethod runClosureVectorUpdate(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      Vector incrementMyElement = new Vector();
      incrementMyElement.addElement(new Integer(0));

      if (--samples >= 0)
    {
      // Collect remaining samples.
      EBoolean bounceDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor bounceDone_dist = EUniChannel.getDistributor(bounceDone);
      EBounceBack ebounce = new EBounceBack(); // just a way to invoke the closure.

      ebounce<- bounce(bounceDone_dist);
      ewhen bounceDone (boolean dummy)
        {
          int cnt;
          incrementMyElement.setElementAt(new Integer(0), 0);
          javaObject.startTimer(false);
          for (cnt = 0; cnt < iterationsPerSample; cnt++)
        {
          int curval = ((Integer)(incrementMyElement.elementAt(0))).intValue();
          incrementMyElement.setElementAt(new Integer(curval + 1), 0);
        }
          javaObject.stopTimer(samples, iterationsPerSample);
          this <- runClosureVectorUpdate(samples, iterationsPerSample, allSamplesDoneDistributor);
        }
    }
      else
    {
      // All samples have been collected.
      // System.out.println("XXX DONE");
      javaObject.reportDetail("incrementing a closed-over vector");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }
}

class EClosureBenchmark extends Benchmark {
    private static boolean      firstTime = true;

    
    EClosureBenchmark (BenchmarkRunner runner) {
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

        setSampleCount(3);
        startTest();
        (new EClosureDriver(this)) <- run(0);
        return -1;
    }

}


