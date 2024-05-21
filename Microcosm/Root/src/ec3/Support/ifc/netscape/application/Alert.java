// Alert.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass containing a collection of static methods that create an
  * "Alert": a modal Window presenting the user with
  * a simple question with up to three possible answers. When run, an
  * InternalWindow or ExternalWindow appears, centered, that will not
  * disappear until the user clicks one of the three answer Buttons.
  * @note 1.0 ...image() calls returned cached bitmap instead of new one each
  *           time.
  */

public class Alert {
    /** Value returned when the user chooses the default option. */
    public final static int DEFAULT_OPTION = 1;

    /** Value returned when the user chooses the 2nd option. */
    public final static int SECOND_OPTION = 2;

    /** Value returned when the user chooses the 3rd option. */
    public final static int THIRD_OPTION = 3;


    private final int MIN_WIDTH          =  200;
    private final int WIDTH_MARGIN       =  7;
    private final int TOP_MARGIN         =  8;
    private final int BOTTOM_MARGIN      =  8;
    private final int TEXT_TOP_MARGIN    =  0;
    private final int TEXT_BOTTOM_MARGIN =  3;
    private final int BUTTON_HORIZ_MARGIN = 6;
    private final int TITLE_LEFT_MARGIN   = 3;
    private final float BUTTON_CENTER_RATIO = ((float)2/(float)3);

    static  final String DEFAULT_ACTION="performDefaultAction";
    static  final String SECOND_ACTION="performSecondAction";
    static  final String THIRD_ACTION="performThirdAction";
    private final int    MIN_BUTTON_WIDTH=50;

    private Window window;
    private AlertContentView  contentView;
    private TextField titleTextField;
    private TextView  messageTextView;
    private Button defaultButton,secondButton,thirdButton;
    private int result;
    private Button bitmapContainer;



    /** Return an image for a notification */
    public static Image notificationImage() {
        return Bitmap.bitmapNamed("netscape/application/alertNotification.gif");
    }

    /** Return an image for a question */
    public static Image questionImage() {
        return Bitmap.bitmapNamed("netscape/application/alertQuestion.gif");
    }

    /** Return an image for a warning */
    public static Image warningImage() {
        return Bitmap.bitmapNamed("netscape/application/alertWarning.gif");
    }

    /** Runs an Alert using an InternalWindow. <b>title</b> is the
      * Alert's title and <b>message</b> is the message that
      * should be displayed.  <b>defaultOption</b>, <b>secondOption</b> and
      * <b>thirdOption</b> are the Alert's Button titles. If
      * <b>secondOption</b> or <b>thirdOption</b> are <b>null</b>, their
      * Buttons will not appear.  Returns the Button the user clicked.
      * This method does not return until the user clicks a Button.
      */
    public static int runAlertInternally(String title,
                        String message, String defaultOption,
                        String secondOption, String thirdOption) {
        return runAlertInternally(null,title,message,defaultOption,
                           secondOption,thirdOption);
    }

    /** Runs an Alert using an ExternalWindow. <b>title</b> is the
      * Alert's title and <b>message</b> is the message that
      * should be displayed.  <b>defaultOption</b>, <b>secondOption</b> and
      * <b>thirdOption</b> are the Alert's Button titles. If
      * <b>secondOption</b> or <b>thirdOption</b> are <b>null</b>, their
      * Buttons will not appear.  Returns the Button the user clicked.
      * This method does not return until the user clicks a Button.
      */
    public static int runAlertExternally(String title,
                        String message, String defaultOption,
                        String secondOption, String thirdOption) {
        return runAlertExternally(null,title,message,defaultOption,
                           secondOption,thirdOption);
    }

    /** Runs an alert using an InternalWindow. <b>image</b> is the Image
      * displayed near the title. <b>title</b> is the
      * Alert's title and <b>message</b> is the message that
      * should be displayed.  <b>defaultOption</b>, <b>secondOption</b> and
      * <b>thirdOption</b> are the Alert's Button titles. If
      * <b>secondOption</b> or <b>thirdOption</b> are <b>null</b>, their
      * Buttons will not appear.  Returns the Button the user clicked.
      * This method does not return until the user clicks a Button.
      */
    public static int runAlertInternally(Image image, String title,
                        String message, String defaultOption,
                        String secondOption, String thirdOption) {
        Alert alert = new Alert( title,
                                 message,
                                 defaultOption,
                                 secondOption,
                                 thirdOption);
        alert.setImage(image);
        return alert.run(false);
    }

    /** Runs an Alert using an ExternalWindow. <b>image</b> is the Image
      * displayed near the title. <b>title</b> is the
      * Alert's title and <b>message</b> is the message that
      * should be displayed.  <b>defaultOption</b>, <b>secondOption</b> and
      * <b>thirdOption</b> are the Alert's Button titles. If
      * <b>secondOption</b> or <b>thirdOption</b> are <b>null</b>, their
      * Buttons will not appear.  Returns the Button the user clicked.
      * This method does not return until the user clicks a Button.
      */
    public static int runAlertExternally(Image image, String title,
                        String message, String defaultOption,
                        String secondOption, String thirdOption) {
        Alert alert = new Alert( title,
                                 message,
                                 defaultOption,
                                 secondOption,
                                 thirdOption);
        alert.setImage(image);
        return alert.run(true);
    }


    /** Creates an alert. Negative option and alternateOption are optional and
      * can be null.
      */
    private Alert(String title, String message, String positiveOption,
                 String negativeOption,String alternateOption) {
        super();
        Size sz;
        Hashtable defaultAttributes;
        TextParagraphFormat format;

        contentView = new AlertContentView(this,0,0,100,100);

        bitmapContainer = new Button(0,0,100,100);
        bitmapContainer.setEnabled(false);
        bitmapContainer.setBordered(false);

        sz = bitmapContainer.minSize();
        bitmapContainer.sizeTo(sz.width,sz.height);
        contentView.addSubview(bitmapContainer);

        titleTextField = new TextField(0,0,100,0);
        titleTextField.setFont(new Font(Font.defaultFont().name(), Font.BOLD,
                               18));
        titleTextField.setJustification(Graphics.LEFT_JUSTIFIED);
        titleTextField.setStringValue(title);
        titleTextField.setBorder(null);
        titleTextField.setBackgroundColor(Color.lightGray);
        titleTextField.setEditable(false);
        contentView.addSubview(titleTextField);


        messageTextView  = new TextView(0,0,100,20);
        messageTextView.setEditable(false);
        messageTextView.setSelectable(false);
        messageTextView.setString("\n" + message + "\n");
        messageTextView.setBackgroundColor(Color.lightGray);
        defaultAttributes = messageTextView.defaultAttributes();
        format = (TextParagraphFormat) defaultAttributes.get(TextView.PARAGRAPH_FORMAT_KEY);
        defaultAttributes = (Hashtable) defaultAttributes.clone();
        format = (TextParagraphFormat) format.clone();
        format.setLeftMargin(0);
        format.setLeftIndent(0);
        format.setRightMargin(0);
        defaultAttributes.put(TextView.PARAGRAPH_FORMAT_KEY, format);
        messageTextView.setDefaultAttributes(defaultAttributes);
        contentView.addSubview(messageTextView);


        defaultButton = new Button(0,0,100,20);
        defaultButton.setTitle(positiveOption);
        defaultButton.setTarget(contentView);
        defaultButton.setCommand(DEFAULT_ACTION);
        contentView.addSubview(defaultButton);

        if( negativeOption != null ) {
            secondButton = new Button(0,0,100,20);
            secondButton.setTitle(negativeOption);
            secondButton.setTarget(contentView);
            secondButton.setCommand(SECOND_ACTION);
            contentView.addSubview(secondButton);
        }

        if( alternateOption != null ) {
            thirdButton = new Button(0,0,100,20);
            thirdButton.setTitle(alternateOption);
            thirdButton.setTarget(contentView);
            thirdButton.setCommand(THIRD_ACTION);
            contentView.addSubview(thirdButton);
        }

    }

    private void setImage(Image anImage) {
        Size sz;

        bitmapContainer.setImage(anImage);
        sz = bitmapContainer.minSize();
        bitmapContainer.sizeTo(sz.width,sz.height);
    }

    private void calculateLayout() {
        FontMetrics fm;
        int buttonRowWidth;
        int minWidth = MIN_WIDTH;
        boolean centerButtons = false;
        int centeredButtonSpacing = 0;
        int buttonOffset = 0;

        /* Calculate the required width */

        /* Title */
        fm = new FontMetrics(titleTextField.font());
        minWidth =  max(minWidth,
                        fm.stringWidth(titleTextField.stringValue()) +
                        (2 * WIDTH_MARGIN) +
                        TITLE_LEFT_MARGIN + bitmapContainer.width());
        titleTextField.sizeTo(100,fm.stringHeight());

        /* Message */
        Size reqSize  = requiredSizeToPreserveLinesInTextView(messageTextView);
        minWidth =  max(minWidth, reqSize.width + (2 * WIDTH_MARGIN));
        messageTextView.sizeTo(reqSize.width,reqSize.height);


        /* Buttons */
        Size buttonSize = new Size();
        int buttonCount = 1;
        fm = new FontMetrics(defaultButton.font());
        buttonSize.width  = max(fm.stringWidth(defaultButton.title()) + 10,
                                MIN_BUTTON_WIDTH);
        buttonSize.height = fm.stringHeight() + 4;

        if( secondButton != null ) {
            buttonSize.width = max(buttonSize.width, fm.stringWidth(secondButton.title()) + 10);
            buttonCount++;
        }

        if( thirdButton != null ) {
            buttonSize.width = max(buttonSize.width, fm.stringWidth(thirdButton.title()) + 10);
            buttonCount++;
        }


        buttonRowWidth = (2 * WIDTH_MARGIN) + (buttonCount * buttonSize.width) +
            (buttonCount-1) * BUTTON_HORIZ_MARGIN;
        if( buttonRowWidth < minWidth &&
            ((float)buttonRowWidth  / (float)minWidth) > BUTTON_CENTER_RATIO ) {

            buttonOffset = (minWidth - (buttonCount * buttonSize.width)) /
                             (1 + buttonCount);
            centeredButtonSpacing = (int)((double)buttonOffset * ((double)6/(double)7));
            buttonOffset = (minWidth - (buttonCount * buttonSize.width) -
                                      ((buttonCount-1) * centeredButtonSpacing)) / 2;
            centerButtons = true;
        } else {
            centerButtons = false;
            minWidth = max(minWidth,buttonRowWidth);
        }

        /* Let's make the layout */
        Rect rect = new Rect();

        /* Image  */
        rect.x = WIDTH_MARGIN;
        rect.y = TOP_MARGIN;
        if( bitmapContainer.image() != null ) {
            if( bitmapContainer.superview() == null )
                contentView.addSubview(bitmapContainer);
            bitmapContainer.moveTo(rect.x,rect.y);
        } else
            bitmapContainer.removeFromSuperview();

        /* Title */

        if( bitmapContainer.image() != null ) {
            rect.x += bitmapContainer.width() + TITLE_LEFT_MARGIN;
            rect.y = TOP_MARGIN + bitmapContainer.height() -
                titleTextField.height() + fm.descent() + 1;
        } else
            rect.y = TOP_MARGIN;
        rect.width  = minWidth - (2 * WIDTH_MARGIN) -
            TITLE_LEFT_MARGIN - bitmapContainer.width();
        rect.height = titleTextField.bounds.height;
        titleTextField.setBounds(rect);
        if((bitmapContainer.y() + bitmapContainer.height()) >
            rect.y + rect.height )
            rect.y = bitmapContainer.y() + bitmapContainer.height() + TEXT_TOP_MARGIN;
        else
            rect.y += (rect.height + TEXT_TOP_MARGIN);

        /* Message */
        rect.x = WIDTH_MARGIN;
        rect.width  = messageTextView.bounds.width;
        rect.height = messageTextView.bounds.height;
        messageTextView.setBounds(rect);
        rect.y += rect.height + TEXT_BOTTOM_MARGIN;


        /* Buttons */
        if( centerButtons ) {
            rect.x = buttonOffset;
            rect.width = buttonSize.width;
            rect.height = buttonSize.height;
            defaultButton.setBounds(rect);
            if( secondButton != null ) {
                rect.x += (rect.width + centeredButtonSpacing);
                secondButton.setBounds(rect);
            }

            if( thirdButton != null ) {
                rect.x += (rect.width + centeredButtonSpacing);
                thirdButton.setBounds(rect);
            }
        } else {
            rect.x = minWidth - (WIDTH_MARGIN + (buttonSize.width * buttonCount) +
                                 (BUTTON_HORIZ_MARGIN * (buttonCount-1)));
            rect.width = buttonSize.width;
            rect.height = buttonSize.height;
            defaultButton.setBounds(rect);
            rect.x += buttonSize.width + BUTTON_HORIZ_MARGIN;
            if( secondButton != null ) {
                secondButton.setBounds(rect);
                rect.x += buttonSize.width + BUTTON_HORIZ_MARGIN;
            }

            if( thirdButton != null ) {
                thirdButton.setBounds(rect);
            }
        }

        Size sz = new Size();
        sz.width = minWidth;
        sz.height = rect.y + buttonSize.height + BOTTOM_MARGIN;
        contentView.sizeTo(sz.width,sz.height);
    }


    /** Run the alert. If useExternalWindow is true, an external window
      * will be used to display the alert. Otherwise, an internal window
      * will be used.
      */
    private int run(boolean useExternalWindow) {
        Size minSize;

        calculateLayout();

        if( useExternalWindow ) {
            window = new ExternalWindow( Window.TITLE_TYPE);
        } else {
            InternalAlertBorder border;

            window = new InternalWindow();
            border = new InternalAlertBorder((InternalWindow)window);
            ((InternalWindow)window).setBorder(border);
        }

        window.setResizable(false);
        minSize = window.windowSizeForContentSize( contentView.bounds.width,
                                            contentView.bounds.height);
        window.sizeTo(minSize.width,minSize.height);
        window.addSubview( contentView );
        window.center();
        window.showModally();
        return result;
    }

    private int max(int i,int j) {
        if( i > j )
            return i;
        else
            return j;
    }

    private Size requiredSizeToPreserveLinesInTextView(TextView aTextView) {
        FontMetrics fm = new FontMetrics(aTextView.font());
        FastStringBuffer sb = new FastStringBuffer();
        int i,c;
        int maxLineWidth = 0;
        char ch;
        int lineCount=1;
        for(i=0,c=aTextView.length() ; i < c ; i++ ) {
            if((ch= aTextView.characterAt(i)) == '\n' ) {
                maxLineWidth = max( maxLineWidth , fm.stringWidth(sb.toString()));
                sb.truncateToLength(0);
                lineCount++;
            } else
                sb.append(ch);
        }

        if( sb.length() != 0 )
            maxLineWidth = max(maxLineWidth, fm.stringWidth(sb.toString()));

        Size result = new Size();
        result.width = maxLineWidth;
        result.height = fm.stringHeight() * lineCount;
        return result;
    }

    void setResult(int aResult) {
        result = aResult;
    }

    void hide() {
        window.hide();
    }
}
