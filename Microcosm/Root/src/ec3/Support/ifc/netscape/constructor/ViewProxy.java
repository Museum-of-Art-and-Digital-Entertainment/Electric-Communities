// ViewProxy.java
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.constructor;

import netscape.application.*;
import netscape.util.*;

/** A ViewProxy is an object that in stored in a Constructor document
  * that represents a View object that is unavailable at build time. The
  * ViewProxy object can have
  * commands added to it's description while in Constructor. This allows you
  * to define arbitrary commands for the object and connect these commands
  * to specific events in the application. Additionally, like all Constructor
  * objects, they can be named. The purpose of these ViewProxies is to
  * represent some actual View that responds to these commands, during the
  * build process, and then replace them with new objects during unarchiving.
  * When a .plan file is loaded, the ViewProxy will attempt to create a view
  * named <b>viewClassName</b> and set its attributes equal to its own.
  * <BR><BR>
  * You normally will not need to create a ViewProxy outside of Constructor.
  * @see Plan
  *
  * @note 1.0b2 Added setAttributesToReplacingView for easier subclassing
  */
public class ViewProxy extends View implements Target, Codable {

    String  viewClassName;
    String  commands[] = { };
    String  shortName;

    private View    _replacingView;

    /** Key used to store view class name in the archive. Used by the encode/decode methods.*/
    public final static String CLASS_NAME_KEY = "viewClassName";
    /** Key used to store commands in the archive. Used by the encode/decode methods.*/
    public final static String COMMANDS_KEY = "commands";
    /** Default class name.*/
    public final static String VIEWPROXY_CLASS_NAME = "netscape.constructor.ViewProxy";

    /** @private */
    public ViewProxy() {
        this(0, 0, 0, 0);
    }

    public ViewProxy(int x, int y, int width, int height) {
        super(x, y, width, height);
        setViewClassName(VIEWPROXY_CLASS_NAME);
    }

    /** Sets the class name of the View that this object represents. */
    public void setViewClassName(String viewName)   {
        int i;

        viewClassName = viewName;
        if(viewClassName == null || "".equals(viewClassName))
            viewClassName = VIEWPROXY_CLASS_NAME;

        i = viewClassName.lastIndexOf('.');
        if(i != -1)
            shortName = viewClassName.substring(i+1);
        else
            shortName = viewClassName;
    }

    /** Returns the class name of the View that this object represents. */
    public String viewClassName()   {
        return viewClassName;
    }

    /** Sets the commands that this ViewProxy is supposed to be able to perform. */
    public void setCommands(String[] values)    {
        if(values != null)
            commands = values;
        else
            commands = new String[0];
    }

    /** Returns the commands that this ViewProxy is supposed to be able to perform. */
    public String[] commands()  {
        return commands;
    }

    /** @private */
    public void drawView(Graphics g) {
        g.setColor(Color.gray);
        g.fillRect(localBounds());

        g.setColor(Color.white);
        g.setFont(Font.defaultFont());
        g.drawStringInRect(shortName, localBounds(), Graphics.CENTERED);

        LineBorder.blackLine().drawInRect(g, localBounds());
    }

    /** @private */
    public boolean isTransparent() {
        return false;
    }

    /** @private */
    public void performCommand(String command, Object object) { }

    /** @private */
    public void describeClassInfo(ClassInfo info)   {
        super.describeClassInfo(info);

        info.addClass(VIEWPROXY_CLASS_NAME, 1);
        info.addField(CLASS_NAME_KEY, STRING_TYPE);
        info.addField(COMMANDS_KEY, STRING_ARRAY_TYPE);
    }

    /** @private */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeString(CLASS_NAME_KEY, viewClassName);
        encoder.encodeStringArray(COMMANDS_KEY, commands, 0, commands.length);
    }

    /** @private */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        setViewClassName(decoder.decodeString(CLASS_NAME_KEY));
        setCommands(decoder.decodeStringArray(COMMANDS_KEY));

        /// If we are in Constructor, we want to be ourselves.
        if((Application.application() instanceof Constructor))  {
            if(((Constructor)Application.application()).inConstructionMode()
                || VIEWPROXY_CLASS_NAME.equals(viewClassName))
                return;
        }

        /// Otherwise, we try to be someone we're not.
        try {
            Class objectClass = Class.forName(viewClassName);
            if (objectClass != null) {
                Object object = objectClass.newInstance();
                if (!(object instanceof View))  {
                    System.err.println("CustomView: " + this
                            + " decode error: " + viewClassName
                            + " is not a View subclass.");
                    return;
                }
                _replacingView = (View)object;
                decoder.replaceObject(_replacingView);
            }
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e) {
        }
    }
    /** @private */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
        if(_replacingView != null)
            setAttributesToReplacingView((View)_replacingView);
        _replacingView = null;
    }

    /** This method is called during the finishDecoding() method to
      * properly get the values out of the ViewProxy and set them on
      * the <b>realView</b> object that has replaced it in the Plan.
      * The current implementation sets the bounds, resize instructions,
      * buffered setting, and moves the subviews to <b>realView</b>.
      */
    public void setAttributesToReplacingView(View realView) {
        Vector views = subviews();

        realView.setBounds(bounds);
        realView.setHorizResizeInstruction(horizResizeInstruction());
        realView.setVertResizeInstruction(vertResizeInstruction());
        realView.setBuffered(isBuffered());

        /// Add any subviews into the replaced view
        if(views != null)   {
            int i, count = views.count();
            for(i = 0; i < count; i++)  {
              realView.addSubview((View)views.elementAt(i));
            }
        }
    }
}

