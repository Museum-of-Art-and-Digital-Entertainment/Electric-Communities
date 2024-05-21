/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/CreateDObjectData.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.serial.Data;

import java.io.*;
import java.net.URL;

/**
 * Wrapper to hold data necessary for creating a remote instance of a 
 * DObject.  This class is Serializable, so that it can be sent across
 * the wire to the remote view that will create the new DObject.
 *
 * @see View#sendCreateMsg
 * @see DObject#getDataToCreateClient
 * @see View#handleCreateMsg
 * @author Scott Lewis
 */
public class CreateDObjectData implements Serializable {

    private DObjectID myID;
    private SessionViewID myHomeID;
    private String myClassName;
    private URL myCodeBase;
    private Serializable myParam;
    private Data myData;
    private boolean myActivate;
    
    public CreateDObjectData(DObjectID id, SessionViewID home, 
                             String className, URL cb, Serializable param,
                             Data data, boolean activate)
    {
        myID = id;
        myHomeID = home;
        myClassName = className;
        myCodeBase = cb;
        myParam = param;
        myData = data;
        myActivate = activate;
    }
    
    public CreateDObjectData(DObjectID id, SessionViewID home, 
                             String className, URL cb, Serializable param)
    {
        this(id, home, className, cb, param, null, true);
    }
    
    public DObjectID getID()
    {
        return myID;
    }
    
    public SessionViewID getHomeID()
    {
        return myHomeID;
    }
    
    public String getClassName()
    {
        return myClassName;
    }
    
    public URL getCodeBase()
    {
        return myCodeBase;
    }
    
    public Serializable getParam()
    {
        return myParam;
    }
    
    public Data getData()
    {
        return myData;
    }
    
    public boolean getActivate()
    {
        return myActivate;
    }
    
}
