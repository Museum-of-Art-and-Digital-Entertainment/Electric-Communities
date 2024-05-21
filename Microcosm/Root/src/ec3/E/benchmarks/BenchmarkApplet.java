
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


import java.awt.*;

public class BenchmarkApplet extends java.applet.Applet
    implements BenchmarkRunner, Runnable
{
    static final String spaces =
                            "                                                       ";
    static final String newline = System.getProperty("line.separator");

    private volatile boolean    running;
    private int                 estimatatedSeconds;
    private ThreadGroup         tgroup;
    private Thread              testTask;
    private List                testList;
    private TextArea            out;
    private Label               timeEstimate;
    private Button              doit, abort, clear;
    private Checkbox            console;
    private Benchmark[]         tests = {
                                    new Benchmark(this),
                                    new MixedBenchmark(this),
                                    new LoopBenchmark(this),
                                    new VariableBenchmark(this),
                                    new MethodBenchmark(this),
                                    new OperatorBenchmark(this),
                                    new CastingBenchmark(this),
                                    new InstantiationBenchmark(this),
                                    new ExceptionBenchmark(this),
                                    new ThreadBenchmark(this)
                                };


    public void init () {
        tgroup = Thread.currentThread().getThreadGroup();

        Font        font = new Font("Courier", Font.PLAIN, 10);
        FontMetrics fm = getFontMetrics(font);
        int         lines = Math.max(10, size().height / fm.getHeight() - 4);

        out = new TextArea(lines, spaces.length() + Benchmark.resolutionName.length());
        out.setFont(font);
        out.setEditable(false);
        add(out);

        boolean toobig;
        do {
            testList = new List(--lines, true);
            add(testList, 0);
            validate();
            if (toobig = testList.size().height - out.size().height > 2)
                remove(testList);
        } while (toobig);
        for (int ii = 0;  ii < tests.length;  ii++)
            testList.addItem(tests[ii].getName());
        testList.select(0);     // Calibration benchmark
        testList.select(1);     // Mixed benchmark

        timeEstimate = new Label(getTimeEstimate());
        add(timeEstimate);

        add(doit  = new Button("Run Benchmark"));
        add(abort = new Button("Stop"));
        add(clear = new Button("Clear"));
        abort.disable();
        clear.disable();

        add(console = new Checkbox("Console"));

        validate();
    }


    public void start () {
        Benchmark.recalibrate();
    }


    public synchronized void run () {
        try {
            running = true;
            timingTests();
        }
        finally {
            running = false;
            doit.enable();
            abort.disable();
        }
    }


    public boolean action (Event evt, Object arg) {
        if (evt.target == doit) {
            if (!running) {
                testTask = new Thread(tgroup, this);
                testTask.start();
            }
            return true;
        }
        else if (evt.target == abort) {
            if (running) {
                testTask.stop();
                println("*** aborted by user ***");
            }
            return true;
        }
        else if (evt.target == clear) {
            out.setText("");
            clear.disable();
            return true;
        }
        return false;
    }


    public boolean handleEvent (Event evt) {
        if (evt.target == testList) {
            if (evt.id == Event.LIST_SELECT  ||  evt.id == Event.LIST_DESELECT)
                if (timeEstimate != null)
                    timeEstimate.setText(getTimeEstimate());
        }
        return super.handleEvent(evt);
    }


    private void timingTests () {
        int     cnt, testSeconds = 0;
        long    begin = System.currentTimeMillis();

        doit.disable();
        abort.enable();
        Benchmark.gc();
        println(newline +
                "Benchmark tests:  [mem=" + Runtime.getRuntime().freeMemory() +"/" +
                                            Runtime.getRuntime().totalMemory() +"]");

        for (cnt = 0;  cnt < testList.countItems();  cnt++)
            if (testList.isSelected(cnt))
                testSeconds += tests[cnt].getTestTime();
        println("Estimated time: " + timeString(estimatatedSeconds) + " (tests " +
                timeString(testSeconds) + ")");

        long total = 0;
        for (cnt = 0;  cnt < testList.countItems();  cnt++)
            if (testList.isSelected(cnt))
                total += tests[cnt].runTest();

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

    private String getTimeEstimate () {
        estimatatedSeconds = 0;
        for (int cnt = 0;  cnt < testList.countItems();  cnt++)
            if (testList.isSelected(cnt))
                estimatatedSeconds += tests[cnt].getRunningTime();
        return "Estimated running time: " + timeString(estimatatedSeconds);
    }


    public void report (String msg, long nanos) {
        if (msg != null) {
            String  time = Long.toString(nanos);
            int     index = msg.length() + time.length();
            String  space = (index >= spaces.length()) ? " " : spaces.substring(index);
            println(msg + space + time + Benchmark.resolutionName);
        }
    }

    public synchronized void println (String s) {
        if (console.getState())
            System.out.println(s);
        out.appendText(s + newline);
        clear.enable();
        Thread.yield();  // give interface a chance to process events
    }

  /* Executed only when this program runs as an application. */
  /* Brian Marick for Electric Communities, March, 1997 */
  public static void main(String[] args) {
    BenchmarkFrame f = new BenchmarkFrame("Java Benchmark");
    BenchmarkApplet benchmarkApplet = new BenchmarkApplet();
    benchmarkApplet.init();
    
    // Add the applet to the frame
    f.add("Center", benchmarkApplet);
    f.pack();
    f.show();
  }
}  // class BenchmarkApplet

// EOF
