// GridLayout.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass implementing a LayoutManager that manages a grid of
  * Views.  A GridLayout with zero rows and zero columns formatted as a square
  * matrix large enough to hold all of the subviews. A zero in a single
  * dimension indicates that dimension can grow without bounds. The
  * GridLayout resizes all Views to have the same size.
  * Note: Because Views do not call <b>layoutView()</b> whenever subviews are
  * added or removed, an application using a GridLayout must explicitly call
  * the GridLayout View's <b>layoutView()</b> with a zero delta width and delta
  * height.  Calling <b>layoutView()</b> in this manner will cause the
  * LayoutManager to properly position and size the View's subviews.
  */
public class GridLayout extends Object implements LayoutManager, Codable {
    /** Number of rows in the Grid. */
    int rowCount;
    /** Number of columns in the Grid. */
    int columnCount;
    /** Horizontal gap between views in the Grid. */
    int horizGap;
    /** Vertical gap between views in the Grid. */
    int vertGap;
    /** How should the views be flowed into the Grid. */
    int flowDirection;

    /** flowDirection value causing subviews to flow across the grid first. */
    public static final int FLOW_ACROSS   = 0;
    /** flowDirection value causing subviews to flow down the grid first. */
    public static final int FLOW_DOWN     = 1;


    final static String ROWCOUNT_KEY = "rowCount",
                        COLUMNCOUNT_KEY = "columnCount",
                        HORIZGAP_KEY = "horizGap",
                        VERTGAP_KEY = "vertGap",
                        FLOWDIRECTION_KEY = "flowDirection";

    /** Constructs a GridLayout with default values rowCount = 0,
      * columnCount = 0, horizGap = 0, vertGap = 0, and flowDirection =
      * FLOW_ACROSS/
      */
    public GridLayout()     {
        this(0, 0, 0, 0, FLOW_ACROSS);
    }

    /** Constructs a GridLayout with default values and <b>numRows</b> rows and
      * <b>numCols</b> columns.
      */
    public GridLayout(int numRows, int numCols)     {
        this(numRows, numCols, 0, 0, FLOW_ACROSS);
    }

    /** Constructs a GridLayout with the specified values and <b>numRows</b>
      * rows and <b>numCols</b> columns.
      */
    public GridLayout(int numRows, int numCols, int horizGap, int vertGap,
                      int flow) {
        setRowCount(numRows);
        setColumnCount(numCols);
        this.horizGap = horizGap;
        this.vertGap  = vertGap;
        setFlowDirection(flow);
    }

    /** Sets the GridLayout to have <b>numRows</b> rows. */
    public void setRowCount(int numRows)        {
        rowCount = (numRows >= 0) ? numRows : 0;
    }

    /** Returns the number of rows in the GridLayout.
      * @see #setRowCount
      */
    public int rowCount() {
        return rowCount;
    }

    /** Sets the GridLayout to have <b>numCols</b> columns. */
    public void setColumnCount(int numCols)        {
        columnCount = (numCols >= 0) ? numCols : 0;
    }

    /** Returns the number of columns in the GridLayout.
      * @see #setColumnCount
      */
    public int columnCount() {
        return columnCount;
    }

    /** Sets the horizontal gap. */
    public void setHorizGap(int gap) {
        horizGap = gap;
    }

    /** Returns the horizontal gap.
      * @see #setHorizGap
      */
    public int horizGap() {
        return horizGap;
    }

    /** Sets the vertical gap. */
    public void setVertGap(int gap) {
        vertGap = gap;
    }

    /** Returns the vertical gap.
      * @see #setVertGap
      */
    public int vertGap() {
        return vertGap;
    }

    /** Sets the flow direction. */
    public void setFlowDirection(int flow)      {
        if(flow == FLOW_ACROSS || flow == FLOW_DOWN)
            flowDirection = flow;
        else
            throw new InconsistencyException(this +
                                "Invalid Flow direction specified: " + flow);
    }

    /** Returns the flow direction.
      * @see #setFlowDirection
      */
    public int flowDirection() {
        return flowDirection;
    }

    /** LayoutManager interface method. GridLayout maintains no View-specific
      * properties, so this method does nothing.
      * @see LayoutManager
      */
    public void addSubview(View aView)      {
    }

    /** LayoutManager interface method. GridLayout maintains no View-specific
      * properties, so this method does nothing.
      * @see LayoutManager
      */
    public void removeSubview(View aView){
    }

    /** Resizes and positions all of the subviews to fit inside the specified
      * grid.
      * @see #gridSize
      * @see LayoutManager
      */
    public void layoutView(View aView, int deltaWidth, int deltaHeight)    {
        Vector theViews = aView.subviews();
        int cnt,max;
        int aveWidth, aveHeight;
        int x,y;
        View currView;
        int curRow, curCol;
        int nRows, nCols;
        Size realGridSize;

        max = theViews.count();

        if(max < 1)
            return;

        realGridSize = gridSize(aView);

        nCols = realGridSize.width;
        nRows = realGridSize.height;

        aveWidth  = ((aView.bounds.width  - (horizGap*nCols)-horizGap) / nCols);
        aveHeight = ((aView.bounds.height - (vertGap*nRows)-vertGap) / nRows);

        curRow = 0;
        curCol = 0;

        if(flowDirection == FLOW_DOWN) {
            /// FILL DOWN THE GRID
            for(cnt=0;cnt<max;cnt++)        {
                x = ((aveWidth*curCol) + (horizGap*(curCol+1)));
                y = ((aveHeight*curRow) + (vertGap*(curRow+1)));
                currView = (View)theViews.elementAt(cnt);
                currView.setBounds(x, y, aveWidth, aveHeight);
                curRow++;
                if(nRows > 0 && curRow >= nRows)        {
                    curCol++;
                    curRow = 0;
                }
            }
        } else {
            /// FILL ACROSS THE GRID
            for(cnt=0;cnt<max;cnt++)        {
                x = ((aveWidth*curCol) + (horizGap*(curCol+1)));
                y = ((aveHeight*curRow) + (vertGap*(curRow+1)));
                currView = (View)theViews.elementAt(cnt);
                currView.setBounds(x, y, aveWidth, aveHeight);
                curCol++;
                if(nCols > 0 && curCol >= nCols)        {
                    curRow++;
                    curCol = 0;
                }
            }
        }
    }

    /** Using the requested grid sizes and the number of subviews of
      * <b>aView</b>, determines the actual size of the grid. Grids of
      * size (0,0) are formatted as a square matrix large
      * enough to hold all of the subviews. A zero in a single dimension
      * indicates that dimension can grow without bounds. Grids with a non-zero
      * <b>rowCount()</b> or <b>columnCount</b> and have more Views than
      * (<b>rowCount()</b> * <b>columnCount</b>) will result in the extra Views
      * being clipped. This method returns the actual size necessary to hold
      * the current subviews of <b>parent</b>.  If <b>parent</b> contains no
      * subviews, returns a Size with 0 extent.
      */
    public Size gridSize(View aView) {
        int totalViews = aView.subviews().count();
        int estRows, estCols;

        if(totalViews < 1)
            return new Size();

        if(rowCount == 0)   {
            if(columnCount == 0)   {
                // Determine closest square matrix
                estCols = (int)Math.ceil(Math.sqrt((float)totalViews));
                estRows = estCols;
            } else {
                estCols = columnCount;
                estRows = (int)Math.ceil(((float)totalViews/estCols));
            }
        } else if(columnCount == 0)    {
            if(rowCount == 0)   {
                // Determine closest square matrix
                estCols = (int)Math.ceil(Math.sqrt((float)totalViews));
                estRows = estCols;
            } else {
                estRows = rowCount;
                estCols = (int)Math.ceil(((float)totalViews/estRows));
            }
        } else  {
            estRows = rowCount;
            estCols = columnCount;
        }
        return new Size(estCols, estRows);
    }


/// Codable Interface
    /** Describes the GridLayout class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info)   {
        info.addClass("netscape.application.GridLayout", 1);

        info.addField(ROWCOUNT_KEY, INT_TYPE);
        info.addField(COLUMNCOUNT_KEY, INT_TYPE);
        info.addField(HORIZGAP_KEY, INT_TYPE);
        info.addField(VERTGAP_KEY, INT_TYPE);
        info.addField(FLOWDIRECTION_KEY, INT_TYPE);
    }

    /** Encodes the GridLayout.
      * @see Codable#encode
      */
    public void decode(Decoder decoder) throws CodingException {
        rowCount = decoder.decodeInt(ROWCOUNT_KEY);
        columnCount = decoder.decodeInt(COLUMNCOUNT_KEY);
        horizGap = decoder.decodeInt(HORIZGAP_KEY);
        vertGap = decoder.decodeInt(VERTGAP_KEY);
        flowDirection = decoder.decodeInt(FLOWDIRECTION_KEY);
    }

    /** Decodes the GridLayout.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(ROWCOUNT_KEY, rowCount);
        encoder.encodeInt(COLUMNCOUNT_KEY, columnCount);
        encoder.encodeInt(HORIZGAP_KEY, horizGap);
        encoder.encodeInt(VERTGAP_KEY, vertGap);
        encoder.encodeInt(FLOWDIRECTION_KEY, flowDirection);
    }

    /** Finishes the GridLayout's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
