// This class just relays messages among instances of itself.

eclass ERelay
{
  ERelay next;  // The next element in the relay chain.

  emethod setNext(ERelay next)
    {
      this.next = next;
    }

  // Causes 'count' sequential message sends + one forwarding of the
  // distributor when all sends complete.  Note that the count of
  // message sends includes the one to this method.
  emethod relay (int count, EResult done)
    {
      // System.out.println("ERelay:relay " + count);
      if (--count <= 0)
    {
      done <- forward(etrue);
    }
      else
    {
      next <- relay(count, done);
    }
    }
}

/* 
 * Note:  it probably should not matter whether messages are being
 * sent from an object to itself or to another object, but it
 * doesn't hurt to check.  This routine sets up a circular list of
 * ERelay objects, each of which sends to the next until enough
 * messages have been sent.  If the list is length 1, the EObject
 * will send to itself.
 *
 * Note that all E object live in the same process, so this is not
 * a benchmark of interprocess communication.
*/

eclass EMessageBenchmarkDriver
{
  Benchmark javaObject; // Java object that represents this benchmark.
  ERelay relayers[];    // E objects which relay messages among themselves.

  EMessageBenchmarkDriver(Benchmark javaObject)
    {
      // System.out.println("Driver constructed.");
      this.javaObject = javaObject;
      relayers = new ERelay[100];
      for (int i = 0; i < relayers.length; i++)
    {
      relayers[i] = new ERelay();
    }
    }

  private void linkRelayChain(EResult done)
    {
      // Link last to first.
      relayers[relayers.length-1] <- setNext(relayers[0]);
      for (int i = 0; i < relayers.length-1; i++)
    {
      // System.out.println("linkRelayChain links " + i);
      relayers[i] <- setNext(relayers[i+1]);
    }
      // The relayers have been told to link up, but they might not
      // have finished yet.  Sending a message through the entire
      // chain won't succeed until they've finished, so that's how we
      // wait for completion.
      // System.out.println("linkRelayChain making sure relay list is open.");
      relayers[0] <- relay(relayers.length, done);
    }

  emethod runSamples(int samples, int callsPerSample,
             EResult allSamplesDoneDistributor)
    {
      if (--samples >= 0)
    {
      // collect remaining samples
      // System.out.println("Collecting sample " + samples + 
      //             " of size " + callsPerSample);
      EBoolean sampleDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor sampleDone_dist = EUniChannel.getDistributor(sampleDone);
      javaObject.startTimer(false);
      relayers[0] <- relay(callsPerSample, sampleDone_dist);
      ewhen sampleDone (boolean done)
        {
          javaObject.stopTimer(samples, callsPerSample);
          this <- runSamples(samples, callsPerSample,
                 allSamplesDoneDistributor);
        }
    }
      else
    {
      // all samples have been collected.
      // System.out.println("All samples have been collected.");
      javaObject.reportDetail("message relay race");
      allSamplesDoneDistributor <- forward(etrue);
    }
    }

  emethod run()
    {
      // System.out.println("In Run Method.");
      EBoolean linkingDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor linkingDone_dist = EUniChannel.getDistributor(linkingDone);
      linkRelayChain(linkingDone_dist);
      ewhen linkingDone (boolean dummy)
    {
      EBoolean allSamplesDone = (EBoolean) EUniChannel.construct(EBoolean.class);
      EUniDistributor allSamplesDone_dist = EUniChannel.getDistributor(allSamplesDone);
      this <- runSamples(javaObject.getSampleCount(), 1000, allSamplesDone_dist);
      ewhen allSamplesDone (boolean junk)
        {
          // System.out.println("XXX Finished all samples of message relay race");
          javaObject.finishTest();
        }
    }
    }
}

class EMessageBenchmark extends Benchmark {
    private static boolean      firstTime = true;

    
    EMessageBenchmark (BenchmarkRunner runner) {
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

        // System.out.println("Preparing to run.");
        setSampleCount(3);
        startTest();
        // System.out.println("Test started.");
        (new EMessageBenchmarkDriver(this)) <- run();
        return -1;
    }

}


