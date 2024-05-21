import java.util.Vector;


// This class provides some hope that we'll notice if a GC happens
// asynchronously within a benchmark.
class DetectGC
{
  String message; 

  DetectGC(String message)
  {
    this.message = message;
  }

  protected void finalize() throws Throwable
  {
    System.out.println("=== Finalization happened.");
    System.out.println("=== " + message);
    super.finalize();
  }
}

eclass EClosureGarbageDriver
{
  Benchmark javaObject; // Java object that represents this benchmark.

  EClosureGarbageDriver(Benchmark javaObject)
    {
      this.javaObject = javaObject;
    }

  emethod run(int firstTestToRun)
    {
      EBoolean done = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor done_dist = EUniChannel.getDistributor(done);
      EBounceBack bounceBack = new EBounceBack();
      bounceBack <- bounce(done_dist);  // start the "recursion.
      ewhenever done (boolean dummy)
    {
      switch(firstTestToRun++)
        {
        case 0:
          System.out.println("GC of closures.");
          this <- runClosures(javaObject.getSampleCount(), 10, done_dist);
          break;
        case 1:
          System.out.println("GC without closures");
          this <- runNoClosures(javaObject.getSampleCount(), 10, done_dist);
          break;

        default:
          javaObject.finishTest();
        }
    }
    }

//========================================


  /*
   * This method starts an Erecursive computation, each step of which
   * creates a closure.  Contrast to runNoClosures, which produces
   * roughly the same garbage, except that it does not build
   * closures.  The magic is all in the implementation of
   * 'closureGarbage'. 
   *
   * Garbage created in this routine is in the noise.
   */
  emethod runClosures(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      System.gc();
      DetectGC iLiveToFinalize = new DetectGC("Sample " + samples + " of runBaseline");
      iLiveToFinalize = null;

      if (--samples >= 0)
    {
      System.out.println("runClosures: sample " + samples);
      EBoolean startComputation = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor startComputation_dist = EUniChannel.getDistributor(startComputation);
      EBoolean computationFinished = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor computationFinished_dist = EUniChannel.getDistributor(computationFinished);
      this <- closureGarbage(startComputation,
                 computationFinished_dist,
                 iterationsPerSample);
      System.out.println("runClosures: forwarding");
      startComputation_dist <- forward(etrue);  // Go!
      System.out.println("runClosures: about to create ewhen");
      ewhen computationFinished (boolean done)
        {
          System.out.println("runClosures: in ewhen");
          // Measure GC time.
          javaObject.startTimer(false);
          System.out.println("runClosures: GC begins");
          System.gc();
          System.out.println("runClosures: GC ends");
          javaObject.stopTimer(samples, iterationsPerSample); // report iterations for this sample
          this <- runClosures(samples, iterationsPerSample,
                  allSamplesDoneDistributor);  // do next sample
        }
    }
      else
    {
      // All samples have been collected.
      javaObject.reportDetail(iterationsPerSample + " garbage closures created");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }

  /* 
   * This is where the bulk of the garbage is created.
   * For each iteration:
   * 1 ewhen/closure
   * 1 channel
   * 1 message send with the channel, a distributor, and an int.
   * 1 forwarding of a distributor.
   *
   * Compare to noClosureGarbage, which is the same except that no
   * closure is created.
   */
  emethod closureGarbage(EBoolean start,
             EResult sampleDoneDistributor,
                int count)
    {
      ewhen start (boolean dummy)
    {
      if (--count >= 0)
        {
          System.out.println("closureGarbage: Iterate");
          EBoolean startNext = (EBoolean) EUniChannel.construct(EBoolean.class);
          EUniDistributor startNext_dist = EUniChannel.getDistributor(startNext);
          this <- closureGarbage(startNext,
                     sampleDoneDistributor, 
                     count);
          startNext_dist <- forward(etrue);
        }
      else
        {
          System.out.println("closureGarbage: done");
          sampleDoneDistributor <- forward(etrue);
        }
    }
    }

//========================================

/*
     This set of methods creates a forwarding chain of E message
     sends.  No closures are created.  Contrast to runClosures, which
     produces roughly the same amount of garbage, plus closures.

     runNoClosures
         - set globals, call startClosurelessComputation.
     startClosurelessComputation
         - call noClosureGarbage first time in a given sample.
     noClosureGarbage
         - iterate, generating garbage but no closures
     runRestNoClosures
         - GC, tally up times, start next sample.
*/

  // The following variables must be available to runRestNoClosures,
  // but we don't want to pass them because that might create
  // garbage. 
  int samples;
  int iterationsPerSample;
  EResult allSamplesDoneDistributor; 



  emethod runNoClosures(int samples, int iterationsPerSample,  EResult allSamplesDoneDistributor)
    {
      this.samples = samples;
      this.iterationsPerSample = iterationsPerSample;
      this.allSamplesDoneDistributor = allSamplesDoneDistributor;
      startClosurelessComputation();
    }

  private void startClosurelessComputation()
    {
      System.out.println("Start Closureless computation");
      System.gc();
      DetectGC iLiveToFinalize = new DetectGC("Sample " + samples + " of runBaseline");
      iLiveToFinalize = null;

      // See noClosureGarbage for reason behind these vars.  They're
      // actually unused (in that no one ewhens on them).
      EBoolean startComputation = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor startComputation_dist = EUniChannel.getDistributor(startComputation);
      EBoolean computationFinished = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor computationFinished_dist = EUniChannel.getDistributor(computationFinished);
      this <- noClosureGarbage(startComputation,
                   computationFinished_dist,
                   iterationsPerSample);
      startComputation_dist <- forward(etrue);
    }
  
  /* 
   * This is where the bulk of the garbage is created.
   * For each iteration:
   * 1 channel
   * 1 message send with the channel, a distributor, and an int.
   * 1 forwarding of a distributor.
   *
   * Compare to noClosureGarbage, which is the same except that a
   * closure is created.
   * 
   * This routine takes two args which have no role except to create
   * the same garbage as in closureGarbage.
   *    ignored is unused; in closureGarbage it's the ewhen's arg.
   *    passed is passed to next invocation; in closureGarbage it's
   *      forwarded when the computation bottoms out.  Here we instead
   *      call the callback.
   * NOTE:  there are many forwardings of distributors here.  When the
   * callback is called, we don't know that they've all completed.
   * But probably most of them have.
   */
  emethod noClosureGarbage(EBoolean ignored,
                EResult passed,
                int count)
    {
      if (--count >= 0)
    {
      System.out.println("Iterate with " + count);
      EBoolean startNext = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor startNext_dist = EUniChannel.getDistributor(startNext);
      this <- noClosureGarbage(startNext, allSamplesDoneDistributor,
                   count);
      startNext_dist <- forward(etrue);
    }
      else 
    {
      this <- runRestNoClosures();
    }
    }

  emethod runRestNoClosures()
    {
      System.out.println("RestNoClosures");
      javaObject.startTimer(false);
      System.gc();
      javaObject.stopTimer(samples, iterationsPerSample); // report iterations for this sample
      System.out.println("Done with GC");

      if (--samples > 0)
    {
      startClosurelessComputation();
    }
      else
    {
      // All samples have been collected.
      javaObject.reportDetail("No garbage closures created");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }
}

//========================================


class EClosureGarbageBenchmark extends Benchmark {
    private static boolean      firstTime = true;

    
    EClosureGarbageBenchmark (BenchmarkRunner runner) {
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
        (new EClosureGarbageDriver(this)) <- run(0);
        return -1;
    }

}


