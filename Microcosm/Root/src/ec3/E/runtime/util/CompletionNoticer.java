package ec.util;

import java.util.Hashtable;
import java.util.Enumeration;

/**
   Target for callback when an operation is completed.
*/
public interface CompletionNoticer 
{
    void noticeCompletion(Object arg);
}

/**
   Generate a callback when all of a set of operations have been
   completed.  A CompletionNoticer will have it's noticeCompletion
   method called on it (with the argument you specify) when all of the
   elements of a set have indicated completion of an operation by
   being passed to the CompletionNoticerJoin as the argument to it's
   noticeCompletion method.<p>

   There are two ways to specify the set of objects whose completion
   is joined into the final notification: 1) pass in a Hashtable with
   the elements of the set as it's keys; 2) call addElement with each
   element of the set, followed by a call to enable().  If you did not
   specify an initial Hashtable, you must call enable() before you
   will receive notification.
*/
public class CompletionNoticerJoin implements CompletionNoticer 
{
    private CompletionNoticer myJoinedNoticer;
    private Object myJoinedNoticerArg;
    private Hashtable mySet;
    private boolean myEnabled;

    /**
       Create a new CompletionNoticerJoin specifing the arg to notify
       with, and an initial Hashtable of elements whose completion
       must be joined.  If the set is not null, this calls enable()
       for you.

       @param joinedNoticer the CompletionNoticer to be notified when
       all elements have reported completion.

       @param arg nullOk the argument that will be handed to joinedNoticer.

       @param set nullOk the initial set of objects to be joined.  If
       null, you will want to call addElement with the elements to be
       joined, and you must call enable() to receive your
       notification.
    */
    public CompletionNoticerJoin(CompletionNoticer joinedNoticer, Object arg, Hashtable set) {
        myJoinedNoticer = joinedNoticer;
        myJoinedNoticerArg = arg;
        if (set != null) {
            mySet = set ;
            enable();
        }
        else {
            mySet = new Hashtable();
        }
    }

    /**
       Create a new CompletionNoticerJoin.  joinedNoticer will be
       notified with noticeCompletion(null).  You must add any
       elements to join using addElement, and you must call enable()
       in order to receive your notification.

       @param joinedNoticer the CompletionNoticer to be notified when
       all elements have reported completion.
    */
    public CompletionNoticerJoin(CompletionNoticerJoin joinedNoticer) {
        this(joinedNoticer, null, null);
    }

    /**
       Extend the set of elements whose completion are required before
       we have completed.  Cannot be called after we've notified.  You
       may need to call enable() to receive your notification.  You
       can add more elements after enable()ing, and before
       notification, but it's probably not a good idea.

       @param element nullOk if non-null, you must arrange for
       this.noticeCompletion(element) to be called before the final
       notification will be triggered.
    */
    public void addElement(Object element) {
        if (mySet == null) {
            throw new Error("tried to add element after notification sent");
        }
        if (element != null) {
            mySet.put(element, element);
        }
    }

    /**
       Indicate that you're finished adding elements to the join.
       This must be called before a notification will be issued.  If
       you specify a non-null set when constructing the
       CompletionNoticerJoin, enable() will be called for you.
       Otherwise, you must call enable() yourself to receive your
       notification.
    */
    public void enable() {
        myEnabled = true;
        noticeCompletion(null);
    }

    /**
       Remove an element from the join.  When all elements have been
       removed, and enable() has been called, the final notification
       will be issued.  Only one notification will be issued.

       @param arg nullOk if non-null, the element that has completed.
    */
    public void noticeCompletion(Object arg) {
        if (arg != null && mySet != null) {
            mySet.remove(arg);
        }
        if (myEnabled && mySet != null && mySet.size() == 0 && myJoinedNoticer != null) {
            myJoinedNoticer.noticeCompletion(myJoinedNoticerArg);
            myJoinedNoticer = null;
            myJoinedNoticerArg = null;
            mySet = null;
        }
    }

    public String toString() {
        String ret = "CompletionNoticerJoin, still waiting on: " ;
        if (mySet == null) {
            ret += "nothing" ;
        }
        else {
            Enumeration en = mySet.keys();
            while (en.hasMoreElements()) {
                ret += " " + en.nextElement().toString() ;
            }
        }
        return ret;
    }
}
