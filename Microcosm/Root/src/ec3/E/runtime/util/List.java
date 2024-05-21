/*
    Doubly-linked list
*/

package ec.util;


public class List {

  public ListNode head;
  public ListNode tail;

  public void List() {
     head = null;
     tail = null;
  }

  public void add(Object o) {
    put(o);
  }

  public void put(Object o) {
    if (head == null) {
       head = new ListNode();
       head.data = o;
       tail = head;
     } else {
       addAfter(tail, o);
     }
  }

  public Object get() {
    if (head == null) {
       return null;
     } else {
       ListNode ln = tail;
       delete(ln);
       return ln.data;
     }
  }

  public void addAfter(ListNode n, Object o) {
    ListNode newNode = new ListNode();
    newNode.data = o;
    newNode.next = n.next;
    newNode.prev = n;
    if (n.next != null) {
      n.next.prev = newNode;
    }
    n.next = newNode;
    if (n == tail) {
      tail = newNode;
    }
  }

  public void addBefore(ListNode n, Object o) {
    ListNode newNode = new ListNode();
    newNode.data = o;
    newNode.prev = n.prev;
    newNode.next = n;
    n.prev = newNode;
    if (n.prev != null) {
      n.prev.next = newNode;
    }
    if (n == head) {
      head = newNode;
    }
  }

  public boolean Delete(Object o) {
    ListNode ln = head;
    while (ln != null) {
      if (ln.data == o) {
        delete(ln);
        return true;
      }
      ln = ln.next;
    }
    return false;
  }

  public void delete(ListNode ln) {
    if (ln == head && ln == tail) {
      head = null;
      tail = null;
    } else if (ln == head) {
      ln.next.prev = null;
      head = ln.next;
    } else if (ln == tail) {
      ln.prev.next = null;
      tail = ln.prev;
    } else {
      ln.prev.next = ln.next;
      ln.next.prev = ln.prev;
    }
    ln.prev = null;
    ln.next = null;
    ln.data = null;
  }

  public void delete() {
    delete(head);
  }

  public boolean isEmpty() {
    return (head == null);
  }

}
