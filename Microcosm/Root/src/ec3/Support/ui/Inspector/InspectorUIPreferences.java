package ec.ui;

/**

 * Gather all user preferences about Inspector usage, especially UI
 * preferences like window size, into one object.

 */


public class InspectorUIPreferences {

    int buttonWidth = 260;      // Button width dictates most window's width
    int buttonHeight = 16;
    int buttonMinWidth = 100;
    int windowWidth = 350;
    int windowHeight = 250;
    int windowMaxHeight = 420;

    public void setInspectorUIPreferences(int buttonWidth,
                                          int buttonMinWidth,
                                          int windowWidth,
                                          int windowHeight
                                          ) {
        this.buttonWidth = buttonWidth;
        this.buttonMinWidth = buttonMinWidth;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public int defaultButtonWidth() { return buttonWidth; }
    public int defaultButtonHeight() { return buttonHeight; }
    public int defaultButtonMinWidth() { return buttonMinWidth; }
    public int defaultWindowHeight() { return windowHeight; }
    public int defaultWindowWidth() { return windowWidth; }
    public int defaultWindowMaxHeight() { return windowMaxHeight; }
}
