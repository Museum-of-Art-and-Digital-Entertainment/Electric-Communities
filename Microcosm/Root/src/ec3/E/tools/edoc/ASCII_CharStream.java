/* Generated By:JavaCC: Do not edit this line. ASCII_CharStream.java Version 0.6 */
package ec.edoc;

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 */

public final class ASCII_CharStream
{
  int bufsize;
  int available;
  int tokenBegin;
  public int bufpos = -1;
  private int bufline[];
  private int bufcolumn[];

  private int column = 0;
  private int line = 1;

  private boolean prevCharIsCR = false;
  private boolean prevCharIsLF = false;

  private java.io.InputStream inputStream;

  private byte[] buffer;
  private int maxNextCharInd = 0;

  private final void ExpandBuff(boolean wrapAround)
  {
     byte[] newbuffer = new byte[bufsize + 2048];
     int newbufline[] = new int[bufsize + 2048];
     int newbufcolumn[] = new int[bufsize + 2048];

     try
     {
        if (wrapAround)
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           System.arraycopy(buffer, 0, newbuffer,
                                             bufsize - tokenBegin, bufpos);
           buffer = newbuffer;

           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
           System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
           bufline = newbufline;

           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
           System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
           bufcolumn = newbufcolumn;

           maxNextCharInd = (bufpos += (bufsize - tokenBegin));
        }
        else
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           buffer = newbuffer;

           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
           bufline = newbufline;

           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
           bufcolumn = newbufcolumn;

           maxNextCharInd = (bufpos -= tokenBegin);
        }
     }
     catch (Throwable t)
     {
        System.out.println("Error : " + t.getClass().getName());
        throw new Error();
     }


     bufsize += 2048;
     available = bufsize;
     tokenBegin = 0;
  }

  private final void FillBuff() throws java.io.IOException
  {
     if (maxNextCharInd == available)
     {
        if (available == bufsize)
        {
           if (tokenBegin > 2048)
           {
              bufpos = maxNextCharInd = 0;
              available = tokenBegin;
           }
           else if (tokenBegin < 0)
              bufpos = maxNextCharInd = 0;
           else
              ExpandBuff(false);
        }
        else if (available > tokenBegin)
           available = bufsize;
        else if ((tokenBegin - available) < 2048)
           ExpandBuff(true);
        else
           available = tokenBegin;
     }

     int i;
     if ((i = inputStream.read(buffer, maxNextCharInd,
                                    available - maxNextCharInd)) == -1)
     {
        --bufpos;
        backup(0);
        if (tokenBegin == -1)
           tokenBegin = bufpos;
        throw new java.io.IOException();
     }
     else
        maxNextCharInd += i;
  }

  public final char BeginToken() throws java.io.IOException
  {
     tokenBegin = -1;
     char c = readChar();
     tokenBegin = bufpos;

     return c;
  }

  private final void UpdateLineColumn(char c)
  {
     column++;

     if (prevCharIsLF)
     {
        prevCharIsLF = false;
        line += (column = 1);
     }
     else if (prevCharIsCR)
     {
        prevCharIsCR = false;
        if (c == '\n')
        {
           prevCharIsLF = true;
        }
        else
           line += (column = 1);
     }

     switch (c)
     {
        case '\r' :
           prevCharIsCR = true;
           break;
        case '\n' :
           prevCharIsLF = true;
           break;
        case '\t' :
           column += (9 - (column & 07));
           break;
        default :
           break;
     }

     bufline[bufpos] = line;
     bufcolumn[bufpos] = column;
  }

  private int inBuf = 0;
  public final char readChar() throws java.io.IOException
  {
     if (inBuf > 0)
     {
        --inBuf;
        return (char)((char)0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]);
     }

     if (++bufpos >= maxNextCharInd)
        FillBuff();

     char c = (char)((char)0xff & buffer[bufpos]);

     UpdateLineColumn(c);
     return (c);
  }

  /**
   * @deprecated 
   * @see #getEndColumn
   */

  public final int getColumn() {
      return bufcolumn[bufpos];
  }

  /**
   * @deprecated 
   * @see #getEndLine
   */

  public final int getLine() {
      return bufline[bufpos];
  }

  public final int getEndColumn() {
      return bufcolumn[bufpos];
  }

  public final int getEndLine() {
      return bufline[bufpos];
  }

  public final int getBeginColumn() {
      return bufcolumn[tokenBegin];
  }

  public final int getBeginLine() {
      return bufline[tokenBegin];
  }

  public final void backup(int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
       bufpos += bufsize;
  }

  public ASCII_CharStream(java.io.InputStream dstream, int startline,
  int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = buffersize;
    buffer = new byte[buffersize];
    bufline = new int[buffersize];
    bufcolumn = new int[buffersize];
  }

  public ASCII_CharStream(java.io.InputStream dstream, int startline,
                                                           int startcolumn)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = 1024;
    buffer = new byte[1024];
    bufline = new int[1024];
    bufcolumn = new int[1024];
  }

  public final String GetImage()
  {
     if (bufpos >= tokenBegin)
        return new String(buffer, 0, tokenBegin, bufpos - tokenBegin + 1);
     else
        return new String(buffer, 0, tokenBegin, bufsize - tokenBegin) +
                              new String(buffer, 0, 0, bufpos + 1);
  }

  public final byte[] GetSuffix(int len)
  {
     byte[] ret = new byte[len];

     if ((bufpos + 1) >= len)
        System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
     else
     {
        System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                                                          len - bufpos - 1);
        System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
     }

     return ret;
  }

  public void Done()
  {
     buffer = null;
     bufline = null;
     bufcolumn = null;
  }

}
