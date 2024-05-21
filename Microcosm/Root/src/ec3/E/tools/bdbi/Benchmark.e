package ec.tools.bdbi;

eclass Benchmark
{
    emethod run(Bdbi after) {
        System.err.println("This benchmark has nothing to do.");
        after <- doOne();
    }
}
