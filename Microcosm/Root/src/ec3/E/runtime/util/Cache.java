/*
    Cache
*/

package ec.util;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;


public class Cache {

  private Hashtable h, nodetable;
  public List l;
  public int size, maxsize;

  public Cache(int s) {       // s == maximum number of elements in cache
     size = 0;
     maxsize = s;
     h = new Hashtable(maxsize);
     nodetable = new Hashtable(maxsize);
     l = new List();
  }

  public Object get(Object key) {

    // bring this element to tail of list (== Most Recently Used)
    ListNode ln = (ListNode)nodetable.get(key);
    if (ln != null && ln != l.tail) {
      l.delete((ListNode)nodetable.get(key));
      l.add(key);
      nodetable.put(key, (Object)l.tail);
    }

    return h.get(key);
  }

  public Object put(Object key, Object data) {

    Object deleted_object = null;

    // add data to cache
    if (h.put(key, data) != null) {
      // just over-wrote something in the cache. 
      // remove the original entry from the linked list.
      l.delete((ListNode)nodetable.get(key));
    } else {
      size++;
    } 
    l.add(key);
    nodetable.put(key, l.tail);

    // have we reached cache capacity?  
    // Get rid of head element (== least recently used)
    if (size > maxsize) {
      deleted_object = h.get(l.head.data);
      h.remove(l.head.data);
      nodetable.remove(l.head.data);
      l.delete(l.head);
      size--;
    }

    return deleted_object;

  }

  public void flush() {
     h = new Hashtable(maxsize);
     nodetable = new Hashtable(maxsize);
     l = new List();
  }

  public Enumeration dump() { // for debugging, return enum of cache contents
    Vector v = new Vector();
    ListNode ln = l.head;
    while (ln != null){
      v.addElement(h.get(ln.data));
      ln = ln.next;
      }
    return v.elements();
  }
     
}
