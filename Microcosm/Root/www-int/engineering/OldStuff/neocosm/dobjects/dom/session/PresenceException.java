package dom.session;

import dom.util.DOMException;

public class PresenceException extends DOMException
{
    public PresenceException(String aString) 
    {
        super(aString);
    }
    
    public PresenceException()
    {
        super();
    }
}