package ec.e.run;

/* abstract */ public class WeakCell 
{
  private int target;      // actually a pointer to a target object
  private int myEntry;     // actually a pointer to an entry queue struct
  private boolean isLocal; // whether this is a local or network weak cell

  native public Object get ();
  native public void set (Object object);
  native protected void setLocal (boolean isLocal);
  
  static 
  {
	System.loadLibrary("run");
  }
}


