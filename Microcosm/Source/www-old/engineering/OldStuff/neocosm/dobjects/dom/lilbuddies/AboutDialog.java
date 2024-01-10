package dom.lilbuddies;

/*
    A basic extension of the java.awt.Dialog class
 */

import java.awt.*;

public class AboutDialog extends Dialog {

    public AboutDialog(Frame parent, boolean modal)
    {
        super(parent, modal);

        // This code is automatically generated by Visual Cafe when you add
        // components to the visual environment. It instantiates and initializes
        // the components. To modify the code, only use code syntax that matches
        // what Visual Cafe can generate, or Visual Cafe may be unable to back
        // parse your Java file into its visual environment.

        //{{INIT_CONTROLS
        setLayout(null);
        setSize(249,150);
        label1 = new java.awt.Label("A Basic Java  Application");
        label1.setBounds(40,35,166,21);
        add(label1);
        okButton = new java.awt.Button("OK");
        okButton.setBounds(95,85,66,27);
        add(okButton);
        setTitle("About");
        setResizable(false);
        //}}

        //{{REGISTER_LISTENERS
        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        SymAction lSymAction = new SymAction();
        okButton.addActionListener(lSymAction);
        //}}

    }

    public AboutDialog(Frame parent, String title, boolean modal)
    {
        this(parent, modal);
        setTitle(title);
    }

    public void addNotify()
    {
        // Record the size of the window prior to calling parents addNotify.
                Dimension d = getSize();

        super.addNotify();

        // Only do this once.
        if (fComponentsAdjusted)
            return;

        // Adjust components according to the insets
        setSize(insets().left + insets().right + d.width, insets().top + insets().bottom + d.height);
        Component components[] = getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Point p = components[i].getLocation();
            p.translate(insets().left, insets().top);
            components[i].setLocation(p);
        }

        // Used for addNotify check.
        fComponentsAdjusted = true;
    }

    public void setVisible(boolean b)
    {
        if (b)
        {
            Rectangle bounds = getParent().bounds();
            Rectangle abounds = bounds();

            move(bounds.x + (bounds.width - abounds.width)/ 2,
                 bounds.y + (bounds.height - abounds.height)/2);
        }

        super.setVisible(b);
    }

    //{{DECLARE_CONTROLS
    java.awt.Label label1;
    java.awt.Button okButton;
    //}}

        // Used for addNotify check.
    boolean fComponentsAdjusted = false;

    class SymWindow extends java.awt.event.WindowAdapter
    {
        public void windowClosing(java.awt.event.WindowEvent event)
        {
            Object object = event.getSource();
            if (object == AboutDialog.this)
                AboutDialog_WindowClosing(event);
        }
    }

    void AboutDialog_WindowClosing(java.awt.event.WindowEvent event)
    {
                dispose();
    }

    class SymAction implements java.awt.event.ActionListener
    {
        public void actionPerformed(java.awt.event.ActionEvent event)
        {
            Object object = event.getSource();
            if (object == okButton)
                okButton_Clicked(event);
        }
    }

    void okButton_Clicked(java.awt.event.ActionEvent event)
    {
        //{{CONNECTION
        // Clicked from okButton Hide the Dialog
                dispose();
        //}}
    }
}
