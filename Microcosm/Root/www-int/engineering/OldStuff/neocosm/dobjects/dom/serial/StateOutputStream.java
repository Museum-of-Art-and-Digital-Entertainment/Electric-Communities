/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/serial/StateOutputStream.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/

package dom.serial;

import java.io.*;

/**
 * @author Scott Lewis
 */
public class StateOutputStream extends ObjectOutputStream
    implements StateOutput
{
    public StateOutputStream(OutputStream outstream)
        throws IOException
    {
        super(outstream);
    }

    // TODO
}
