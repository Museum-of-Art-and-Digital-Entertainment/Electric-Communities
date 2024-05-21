// PackLayout.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass implementing a LayoutManager similar to the TK Packer.
  * See the PackConstraints class for more information about the available
  * settings can be used.<p>
  * <i>Note: Because Views do not call <b>layoutView()</b> whenever subviews are
  * added or removed, an application using a PackLayout must explicitly call
  * the PackLayout View's <b>layoutView()</b> with a zero delta width and delta
  * height.  Calling <b>layoutView()</b> in this manner will cause the
  * LayoutManager to properly position and size the View's subviews.</i>
  *
  * @see PackConstraints
  * @note 1.0 added defaultConstraints
  * @note 1.0 better algorithm for determining nested view preferred size
  * @note 1.0 defaultConstraints are now archived
  */
public class PackLayout extends Object implements LayoutManager, Codable {
    Hashtable   viewConstraints;
    Vector      viewVector;
    PackConstraints defaultConstraints;

    final static String VIEWCONSTRAINTS_KEY = "viewConstraints",
                        VIEWVECTOR_KEY = "viewVector",
                        DEFAULT_CONSTRAINTS_KEY = "defaultConstraints";


    /** The PackConstraints to be used as default values, when a view
      * it added to the PackLayout without specific defaults. If you
      * are going to set this, you should do so before adding subviews.
      * The management of the mapping between constraints and views is lazy,
      * and may not happen until it is really needed.
      *
      */
    public PackConstraints defaultConstraints() {
        if (defaultConstraints == null) {
            defaultConstraints = new PackConstraints();
        }

        return defaultConstraints;
    }

    /** Sets the default constraints. These values are used when a
      * view is added to the PackLayout without specific defaults.
      * You should set this before you add any subviews.
      *
      */
    public void setDefaultConstraints(PackConstraints constraints)  {
        if(!constraints.equals(defaultConstraints()))   {
            int i, count;
            count = viewVector.count();
            for(i = 0; i < count; i++)  {
                /// This will automatically add the view to the hashtable with
                ///  the current default constraints.
                constraintsFor((View)viewVector.elementAt(i));
            }
        }
        defaultConstraints = constraints;
    }

    /** Constructs a PackLayout. */
    public PackLayout()     {
        super();
        viewConstraints = new Hashtable();
        viewVector = new Vector();
    }

    /** Returns the PackConstraints object associated with <b>aView</b>.
      */
    public PackConstraints constraintsFor(View aView)   {
        if(viewConstraints.get(aView) == null)  {
            setConstraints(aView, defaultConstraints());
        }
        return (PackConstraints)viewConstraints.get(aView);
    }

    /** Adds <b>aView</b> to the PackLayout with default PackConstraints.
      * @see LayoutManager
      * @see PackConstraints
      */
    public void addSubview(View aView)      {
        viewVector.addElementIfAbsent(aView);
    }

    /** Associates the PackConstraints <b>constraints</b> with <b>aView</b>.
      * You usually call this method to associate non-default constraints with
      * a particular View.<p>
      * <i><b>Note:</b> This method adds a clone of <b>constraints</b> to
      * its internal constraint container. You can therefore reconfigure and
      * pass in the PackConstraints same instance on each call without no side
      * effects.</i>
      */
    public void setConstraints(View aView, PackConstraints constraints) {
        viewVector.addElementIfAbsent(aView);
        viewConstraints.put(aView, constraints.clone());
    }

    /** Removes <b>aView</b> from the PackLayout.
      * @see LayoutManager
      */
    public void removeSubview(View aView)   {
        viewConstraints.remove(aView);
        viewVector.removeElement(aView);
    }

    /** Positions and sizes its View's subviews according to the constraints
      * associated with each subview.
      * @see LayoutManager
      */
    public void layoutView(View aView, int deltaWidth, int deltaHeight) {
        View curView;
        int cntx,max;
        int cavityX = 0, cavityY = 0;
        int cavityWidth = aView.bounds.width;
        int cavityHeight = aView.bounds.height;
        int frameX, frameY, frameWidth, frameHeight;
        int width, height, x, y;
        int side, padx, pady, ipadx, ipady, anchor;
        boolean expand, fillx, filly;
        Vector sViews;
        PackConstraints constraints;
        Size preferredSize;

        sViews = aView.subviews();
        max=sViews.count();
        for(cntx=0 ; cntx < max ; cntx++)       {
            curView     = (View)sViews.elementAt(cntx);
            constraints = (PackConstraints)viewConstraints.get(curView);
            if(constraints == null)
                constraints = defaultConstraints();
            side        = constraints.side();
            padx        = constraints.padX()*2;
            pady        = constraints.padY()*2;
            ipadx       = constraints.internalPadX();
            ipady       = constraints.internalPadY();
            expand      = constraints.expand();
            fillx       = constraints.fillX();
            filly       = constraints.fillY();
            anchor      = constraints.anchor();

            preferredSize = preferredLayoutSize(curView);

            if ((side == PackConstraints.SIDE_TOP) || (side == PackConstraints.SIDE_BOTTOM)) {
                frameWidth = cavityWidth;
                frameHeight = preferredSize.height + pady + ipady;
                if (expand)
                    frameHeight += YExpansion(curView, cavityHeight);
                cavityHeight -= frameHeight;
                if (cavityHeight < 0) {
                    frameHeight += cavityHeight;
                    cavityHeight = 0;
                }
                frameX = cavityX;
                if (side == PackConstraints.SIDE_TOP) {
                    frameY = cavityY;
                    cavityY += frameHeight;
                } else {
                    frameY = cavityY + cavityHeight;
                }
            } else {
                frameHeight = cavityHeight;
                frameWidth = preferredSize.width + padx + ipadx;
                if (expand)
                    frameWidth += XExpansion(curView, cavityWidth);
                cavityWidth -= frameWidth;
                if (cavityWidth < 0) {
                    frameWidth += cavityWidth;
                    cavityWidth = 0;
                }
                frameY = cavityY;
                if (side == PackConstraints.SIDE_LEFT) {
                    frameX = cavityX;
                    cavityX += frameWidth;
                } else {
                    frameX = cavityX + cavityWidth;
                }
            }

            width = preferredSize.width + ipadx;
            if ((fillx) || (width > (frameWidth - padx)))
                width = frameWidth - padx;

            height = preferredSize.height + ipady;
            if ((filly) || (height > (frameHeight - pady)))
                height = frameHeight - pady;

            padx /= 2;
            pady /= 2;

            switch (anchor) {
            case PackConstraints.ANCHOR_NORTH:
                x = frameX + (frameWidth - width)/2;
                y = frameY + pady;
                break;
            case PackConstraints.ANCHOR_NORTHEAST:
                x = frameX + frameWidth - width - padx;
                y = frameY + pady;
                break;
            case PackConstraints.ANCHOR_EAST:
                x = frameX + frameWidth - width - padx;
                y = frameY + (frameHeight - height)/2;
                break;
            case PackConstraints.ANCHOR_SOUTHEAST:
                x = frameX + frameWidth - width - padx;
                y = frameY + frameHeight - height - pady;
                break;
            case PackConstraints.ANCHOR_SOUTH:
                x = frameX + (frameWidth - width)/2;
                y = frameY + frameHeight - height - pady;
                break;
            case PackConstraints.ANCHOR_SOUTHWEST:
                x = frameX + padx;
                y = frameY + frameHeight - height - pady;
                break;
            case PackConstraints.ANCHOR_WEST:
                x = frameX + padx;
                y = frameY + (frameHeight - height)/2;
                break;
            case PackConstraints.ANCHOR_NORTHWEST:
                x = frameX + padx;
                y = frameY + pady;
                break;
            case PackConstraints.ANCHOR_CENTER:
            default:
                x = frameX + (frameWidth - width)/2;
                y = frameY + (frameHeight - height)/2;
                break;
            }

            curView.setBounds(x, y, width, height);
        }
        return;
    }

    private int XExpansion(View current, int cavityWidth) {
        PackConstraints constraints;
        int numExpand, minExpand, curExpand, childWidth;
        int padx, ipadx, side;
        boolean expand;
        int x,max;
        View curView;

        minExpand = cavityWidth;
        numExpand = 0;

        max=viewVector.count();
        for(x=viewVector.indexOf(current) ; x < max ; x++)        {
            curView         = (View)viewVector.elementAt(x);
            constraints     = (PackConstraints)viewConstraints.get(curView);
            if(constraints == null)
                constraints = defaultConstraints();
            padx            = constraints.padX()*2;
            ipadx           = constraints.internalPadX();
            expand          = constraints.expand();
            side            = constraints.side();

            childWidth      = preferredLayoutSize(curView).width + padx + ipadx;

            if ((side == PackConstraints.SIDE_TOP) || (side == PackConstraints.SIDE_BOTTOM)) {
                curExpand = (cavityWidth - childWidth)/numExpand;
                if (curExpand < minExpand)
                    minExpand = curExpand;
            } else {
                cavityWidth -= childWidth;
                if (expand)
                    numExpand++;
            }
        }

        curExpand = cavityWidth/numExpand;
        if (curExpand < minExpand)
            minExpand = curExpand;

        if (minExpand < 0)
            return 0;
        else
            return minExpand;

    }

    private int YExpansion(View current, int cavityHeight) {
        PackConstraints constraints;
        int numExpand, minExpand, curExpand, childHeight, pady, ipady;
        boolean expand;
        int side;
        int x,max;
        View curView;

        minExpand = cavityHeight;
        numExpand = 0;

        max=viewVector.count();
        for(x=viewVector.indexOf(current) ; x < max ; x++)        {
            curView         = (View)viewVector.elementAt(x);
            constraints     = (PackConstraints)viewConstraints.get(curView);
            if(constraints == null)
                constraints = defaultConstraints();
            pady            = constraints.padY()*2;
            ipady           = constraints.internalPadY();
            expand          = constraints.expand();
            side            = constraints.side();

            childHeight     = preferredLayoutSize(curView).height + pady + ipady;

            if ((side == PackConstraints.SIDE_LEFT) || (side == PackConstraints.SIDE_RIGHT)) {
                curExpand = (cavityHeight - childHeight)/numExpand;
                if (curExpand < minExpand)
                    minExpand = curExpand;
            } else {
                cavityHeight -= childHeight;
                if (expand)
                    numExpand++;
            }
        }

        curExpand = cavityHeight/numExpand;
        if (curExpand < minExpand)
            minExpand = curExpand;

        if (minExpand < 0)
            return 0;
        else
            return minExpand;

    }

    private Rect containedRect(View target) {
        int  x, max;
        Rect maxRect = new Rect(0,0,0,0);
        Vector sViews;
        Rect tmpBounds;

        sViews = target.subviews();
        if(sViews == null || sViews.count() < 1)    {
            return new Rect(target.bounds.x, target.bounds.y,
                        target.minSize().width, target.minSize().height);
        }

        target.layoutView(0,0);

        max = sViews.count();
        for(x=0 ; x < max ; x++)  {
            if(x == 0)
                maxRect = containedRect((View)sViews.elementAt(x));
            tmpBounds = containedRect((View)sViews.elementAt(x));
            maxRect.unionWith(tmpBounds);
        }

        return maxRect;
    }

    /** This is the primative method that determines what the right size is
      * for <b>target</b> current implmentation simply returns target.minSize().
      * If minSize() returns (0, 0), we will try to calculate a minimum size
      * based on the subviews of the <b>target</b>. The algorithm calculates a
      * bounding Rect small enough to contain all subviews.
      * All the layout code calls this to determine the size of child views.
      *
      */
    public Size preferredLayoutSize(View target) {
        Size minSize = target.minSize();
        if(minSize.width != 0 || minSize.height != 0)
            return minSize;

        Rect aRect = containedRect(target);
        return new Size(aRect.x+aRect.width, aRect.y+aRect.height);
    }

/// Codable Interface
    /** Describes the PackLayout class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info)   {
        info.addClass("netscape.application.PackLayout", 2);
        info.addField(VIEWCONSTRAINTS_KEY, OBJECT_TYPE);
        info.addField(VIEWVECTOR_KEY, OBJECT_TYPE);
        info.addField(DEFAULT_CONSTRAINTS_KEY, OBJECT_TYPE);

    }

    /** Encodes the PackLayout.
      * @see Codable#encode
      */
    public void decode(Decoder decoder) throws CodingException {
        viewConstraints = (Hashtable)decoder.decodeObject(VIEWCONSTRAINTS_KEY);
        viewVector = (Vector)decoder.decodeObject(VIEWVECTOR_KEY);
        if (decoder.versionForClassName("netscape.application.PackLayout") > 1) {
            defaultConstraints = (PackConstraints)decoder.decodeObject(DEFAULT_CONSTRAINTS_KEY);
        }
    }

    /** Decodes the PackLayout.
      * @see Codable#decode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(VIEWCONSTRAINTS_KEY, viewConstraints);
        encoder.encodeObject(VIEWVECTOR_KEY, viewVector);
        encoder.encodeObject(DEFAULT_CONSTRAINTS_KEY, defaultConstraints);
    }

    /** Finishes the PackLayout's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}
