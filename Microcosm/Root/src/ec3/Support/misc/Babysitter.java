// This is a test of some thread scheduling things... Java has no defined
// scheduling for threads of different priority - it relies on the host OS's
// own threads (which I guess is reasonable).  The downside is that different
// operating systems can do different things.  
//
// The aim is to write a thread which will do things in the background, but
// which will be guaranteed to get at least some of the action.
//
// Unfortunately Windows does not let you do this through the priority
// mechanism, since low priority threads are not guaranteed to be run at all.
// 
// Harry Richardson 7/4/96


package ec.misc;

import ec.e.start.crew.CrewCapabilities;
import java.util.Vector;

//
// This class should be inherited by threads that wish to run at a low
// priority, but which wish to be guaranteed that they will be run at some
// point.
// This is achieved by running in conjunction with a BabysitterThread, which
// runs at a high priority, and which makes sure that the BackgroundThread
// is run periodically (even if not scheduled) by bumping up the priority of
// the BackgroundThread temporarily.
// The reason for doing things this way rather than just writing a high
// priority thread that sleeps from time to time, is that this thread can
// work flat out when there is nothing else going on, but will do it at
// a low priority, and so can yeild control to another thread automatically.
//
// The constructor takes a time in milliseconds which specifies for how long
// the thread can be run for at maximum priority before reverting to minium
// priority.
//
abstract public class BackgroundThread extends Thread {
    // Protected fields.
    protected boolean my_just_bumped_up = true; 
    protected long my_last_time;
    protected int my_time_when_bumped;
    protected int my_frequency;

    // Private fields.
    private boolean my_finish = false;


    public BackgroundThread(int frequency, int time_when_bumped, String name) {
        super(name);
        setPriority(Thread.MIN_PRIORITY);
        my_time_when_bumped = time_when_bumped;
        my_frequency = frequency;

        // Record the current time (otherwise we'll get run by the babysitter
        // immediately since it'll think that we haven't run since 1970!)
        my_last_time = System.currentTimeMillis();
    }

    public void finish() { my_finish = true; }

    public void run() {
        while (!my_finish) {
            // If we're running at max-priority then we need to check how long
            // we've been going, and yield if necessary (by setting the
            // priority back to normal)

            long time = System.currentTimeMillis();

            if (getPriority() == Thread.MAX_PRIORITY) {
                if (my_just_bumped_up) {
                    // Store the time so that we know how long there is to go
                    // at this priority level.
                    my_last_time = System.currentTimeMillis();
                    my_just_bumped_up = false;
                }

                // This is the method that actually does things!
                doSomething();

                if (time - my_last_time > my_time_when_bumped) {
                    // We've overstayed our welcome - record the last time at
                    // which we ran, and then yield

                    my_last_time = time;
                    my_just_bumped_up = true;
                    setPriority(Thread.MIN_PRIORITY);                           
                }
            } else {
                // We're at a low priority and should just do something and
                // then record the time. 
                
                doSomething();
                my_last_time = time;                
            }
        }

    }

    abstract public void doSomething();
        // This method should be overriden with the one that you want to
        // actually do something...
}


//
// This class exists only to be a low-priority thread.  The Babysitter needs
// to have at least one low-priority thread around in order to determine when
// the system is mostly idle.  In the absence of any other threads to manager
// it will create one of these.
//
public class BackgroundGCThread extends BackgroundThread {
    public BackgroundGCThread(int frequency, int time_when_bumped, String name) {
        super(frequency, time_when_bumped, name);
    }
    
    public void doSomething() {
        try {
            Thread.currentThread().sleep(150);
        } catch (InterruptedException ie) {}    
    }   
}



//
// This class makes sure that the BackgroundThread that it has been given
// to look after gets run from time to time.  It enforces this by bumping
// up the baby's priority if it hasn't been run. 
// It has the ability to force garbage collections if it senses that the CPU
// is idling - whenever it notices that it has not had to bump up anything
// for XXX ticks (where XXX is a passed in integer), it gc's.
// It takes three arguments: the checking period (in ms), the number of ticks
// after which it will force gc (-1 => no forced gc), and a thread name.
//
public class BabysitterThread extends Thread {
    private static Trace the_trace = new Trace("background");
    private static BabysitterThread the_instance = null;
    
    private Vector  my_children, my_add, my_remove;
    private int     my_checking_frequency;
    private boolean my_pause = false;
    private Object  my_pause_lock = new Object(); // Exists just to be a lock 
    private int     my_counter = 0;
    private int     my_garbage_collect = -1;
    private BackgroundGCThread my_foo_thread = null;

    private BabysitterThread(int checking_frequency,
                             int garbage_collect,
                             String name) {
        // Private constructor - we only want one of these things!

        super(name);
        
        my_garbage_collect = garbage_collect;
        my_checking_frequency = checking_frequency;
        setPriority(Thread.MAX_PRIORITY);

        my_children = new Vector(3, 1);
        my_add      = new Vector(1, 1);
        my_remove   = new Vector(1, 1);

        if (my_garbage_collect >= 0) {
            my_foo_thread = new BackgroundGCThread(500, 10, "BackgroundGCThread");
            my_foo_thread.start();
            my_children.addElement(my_foo_thread);
        }

        if (the_trace.debug && Trace.ON) {
            the_trace.debugm("New babysitter thread (" + name + "), gc every " +
                garbage_collect + " background ticks");
        }
    }


    public static BabysitterThread getBabysitter() {
        // Public accessor method - this returns the current babysitter thread if
        // one has already been created.  If not, it creates a new one (looking at
        // runtime properties for the settings for garbage collection and checking
        // frequency)

        if (the_instance != null) {
            return the_instance;
        }

        int force_gc = -1; // Default behaviour is no forced gc...
        int check_time = 250; // Default number of milliseconds between checks

        // Now try getting values for these from the runtime properties...

        try {
            // First get the BackgroundGC setting.
            String gc = CrewCapabilities.getTheEnvironment().getProperty("BackgroundGC");
            if (gc != null) {
                force_gc = Integer.parseInt(gc);
            }
        } catch (RuntimeException re) {
            // Not a problem - we couldn't get a property.  This isn't fatal
            // Do nothing.  Do not pass go.  Do not collect $200.   
        } catch (NumberFormatException nfe) {
            // Not an integer, use the default.
        }

        try {
            // Now get the checking time.
            String check = CrewCapabilities.getTheEnvironment().getProperty("BackgroundCheckTime");
            if (check != null) {
                check_time = Integer.parseInt(check);
            }
        } catch (RuntimeException re) {
            // Not a problem - we couldn't get a property.  This isn't fatal
            // Do nothing.  Do not pass go.  Do not collect $200.   
        } catch (NumberFormatException nfe) {
            // Not an integer, use the default.
        }

        the_instance = new BabysitterThread(check_time, force_gc, "Babysitter thread");
        the_instance.start();

        return the_instance;
    }

    public void pause() {
        synchronized(my_pause_lock) {
            my_pause = true;
        }

        if (the_trace.debug && Trace.ON) {
            the_trace.debugm("Babysitter -> pause");
        }
    }

    public void restart() {
        synchronized(my_pause_lock) {
            my_pause = false;
        }

        if (the_trace.debug && Trace.ON) {
            the_trace.debugm("Babysitter -> restart");
        }
    }

    public void addChild(BackgroundThread child) {
        synchronized (my_add) {
            if (my_add.contains(child)) {
                // Aleady been added
                return;
            }

            my_add.addElement(child);
        }

        if (the_trace.debug && Trace.ON) {
            the_trace.debugm("Babysitter -> addChild");
        }
    }

    public void removeChild(BackgroundThread child) {
        synchronized (my_remove) {
            if (my_remove.contains(child)) {
                return;
            }

            my_remove.addElement(child);
        }

        if (the_trace.debug && Trace.ON) {
            the_trace.debugm("Babysitter -> removeChild");
        }
    }

    public void run() {
        while (true) {

            boolean pause;
            synchronized (my_pause_lock) {
                pause = my_pause;
            }

            synchronized(my_add) {
                for (int i = my_add.size() - 1; i >= 0; i--) {
                    if (!(my_children.contains(my_add.elementAt(i)))) {
                        my_children.addElement(my_add.elementAt(i));
                    }   
                }

                if (my_foo_thread != null && my_add.size() > 0) {
                    if (the_trace.debug && Trace.ON) {
                        the_trace.debugm("run -> killing the fooGC thread");
                    }

                    my_children.removeElement(my_foo_thread);
                    my_foo_thread.stop();
                    my_foo_thread = null;                   
                }

                my_add.removeAllElements();
            }

            synchronized(my_remove) {
                for (int i = my_remove.size() - 1; i >= 0; i--) {
                    if (my_children.contains(my_remove.elementAt(i))) {
                        my_children.removeElement(my_remove.elementAt(i));
                    }   
                }

                // Only add the foo_thread if we i) have no other threads, and
                // ii) we are in force_gc mode...
                if (my_children.size() == 0 && my_garbage_collect >= 0) {
                    if (the_trace.debug && Trace.ON) {
                        the_trace.debugm("run -> adding a new fooGC thread");
                    }

                    my_foo_thread = new BackgroundGCThread(500, 10, "BackgroundGCThread");
                    my_foo_thread.start();
                    my_children.addElement(my_foo_thread);
                }

                my_remove.removeAllElements();
            }
            
            if (!pause) {
                for (int i = my_children.size() - 1; i >= 0; i--) {
                    long time = System.currentTimeMillis();
                    BackgroundThread baby = (BackgroundThread) my_children.elementAt(i);

                    if (baby.my_last_time < time - baby.my_frequency) {
                        if (the_trace.debug && Trace.ON) {
                            the_trace.debugm("run -> bumping up child's priority");
                        }

                        baby.setPriority(Thread.MAX_PRIORITY);
                        my_counter = 0;
                    }
                }

                if (my_garbage_collect != -1) { // gc turned on...
                    my_counter++;
      
                    if (my_counter > my_garbage_collect) { // we're idle.
                        // Force a gc.
                        if (the_trace.debug && Trace.ON) {
                            the_trace.debugm("run -> forcing a garbage collect and finalisation");
                        }

                        System.gc();
                        System.runFinalization();
                        my_counter = 0;
                    }
                }
            }

            try {
                // We MUST yield to other threads, since we are running at
                // maximum priority, which on some OS's (those without
                // thread preemption) means that other threads will NEVER
                // get run...
                Thread.currentThread().sleep(my_checking_frequency);
            } catch (InterruptedException e) {}
        }
    }
}