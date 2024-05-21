package ec.util;

public class ThreadWatcher extends Thread {
    private static ThreadWatcher watcher;
    private static boolean stop = false;
    public static int waitTime = 5000;
    
    private ThreadWatcher(String name) {
        super(name);
    }
    
    public static void stopWatching() {
        if (watcher == null)
            return;
        try {
            synchronized (watcher) {
                stop = true;
                watcher.notify();
            }
        } catch (Throwable t) {
            /* XXX another one of those bad catch blocks */
        }
    }
    
    public static void startWatching(int timeToWait) {
        waitTime = timeToWait;
        startWatching();
    }
    
    public static void startWatching() {
        if (watcher == null) {
            watcher = new ThreadWatcher("Threadwatcher");
            watcher.setDaemon(true);
            watcher.start();
        }
    }
    
    public static ThreadGroup getTopThreadGroup() {
        ThreadGroup theGroup = null;
        try {
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            while (group != null) {
                theGroup = group;
                group = group.getParent();
            }
        } catch (Exception e) {
            System.out.println("Error trying to get top thread group");
        }
        return theGroup;
    }
    
    public static void showThreads() {
        try {
            int i;
            ThreadGroup theGroup = getTopThreadGroup();
            int count = theGroup.activeCount();
            System.out.println("There are " + count +
                               " threads currently running");
            
            Thread threads[] = new Thread[count];
            theGroup.enumerate(threads);
            for (i = 0; i < count; i++) {
                Thread thread = threads[i];
                System.out.println("Thread " + i + "(" +
                                   (thread.isDaemon() ? "daemon" : "normal") +
                                   (thread.isAlive() ? ", alive" : ", dead ") +
                                   "):" + thread.toString());
            }
        } catch (Throwable t) {
            System.out.println("Error occured in showThread: " + t);
        }
    }   
    
    public void run() {
        synchronized (this) {
            while (stop == false) {
                try {   
                    wait(waitTime);
                } catch (Throwable t) {
                    /* XXX another one of those bad catch blocks */
                }
                showThreads();
            }
        }
    }
}

