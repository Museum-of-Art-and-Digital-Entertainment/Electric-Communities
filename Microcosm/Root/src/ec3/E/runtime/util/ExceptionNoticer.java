package ec.util;

public interface ExceptionNoticer 
{
    public void noticeReportedException(String msg, Throwable t);
    public void noticeUncaughtException(String msg, Throwable t);
}
