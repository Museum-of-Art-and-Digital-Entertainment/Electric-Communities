package ec.e.run;

class eMessage
{
    public RtSealer mySealer;
    public Object[] myArgs;
    public RtExceptionEnv myEE;
    public eMessage next = null;

    eMessage()  {
    }   
}
