package ec.util;

public interface NestedThrowableVector
{
    public void addThrowable(String label, Throwable t);
    public int size();
    public String getLabel(int i);
    public Throwable getThrowable(int i);
}
