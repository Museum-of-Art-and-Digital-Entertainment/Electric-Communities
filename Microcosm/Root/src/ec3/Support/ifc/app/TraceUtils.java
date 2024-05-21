package ec.ifc.app;

import netscape.application.Button;
import netscape.application.ExternalWindow;
import netscape.application.ListItem;
import netscape.application.PopupItem;
import netscape.application.RootView;

/**
 * Utility routines to help construct useful trace messages.
 * At least for now, these routines are only used in this package.
 */
class TraceUtils {

    /** description for a button */
    static String traceDescription(Button button) {
        if (button == null) {
            return null;
        }
        
        String result;
        String buttonTypeString;
        switch (button.type()) {
            case Button.TOGGLE_TYPE:
                buttonTypeString = " checkbox";
                break;
            case Button.RADIO_TYPE:
                buttonTypeString = " radio button";
                break;
            case Button.CONTINUOUS_TYPE:
                buttonTypeString = " continuous button";
                break;
            default:
                buttonTypeString = " button";
                break;
        }
        String title = button.title();
        result = (emptyOrNull(title) ? "(unnamed)" : title) + buttonTypeString;
        if (!button.isEnabled()) {
            result = "disabled " + result;
        }
        
        RootView rootView = button.rootView();
        if (rootView != null) {
            String windowDescription = 
                traceDescription(rootView.externalWindow());
            if (windowDescription != null) {
                result = result + " in " + windowDescription;
            }
            
        }
        return result;
    }
    
    /** description for a list item */
    static String traceDescription(ListItem item) {
        if (item == null) {
            return null;
        }
        
        String result;
        String title = item.title();
        String itemTypeString = (item instanceof PopupItem) 
                                 ? " menu item" 
                                 : " list item";
        result = (emptyOrNull(title) ? "(untitled)" : title) + itemTypeString;
        
        String windowDescription =
            traceDescription(item.listView().rootView().externalWindow());      
        if (windowDescription != null) {
            result = result + " in " + windowDescription;
        }
        return result;
    }   
    
    /** description for a tab item */
    static String traceDescription(ECTabItem item) {
        if (item == null) {
            return null;
        }
        
        String result;      
        String title = item.title();
        result = (emptyOrNull(title) ? "(unnamed)" : title) + " tab";
        if (item.isSelected()) {
            result = "already-selected " + result;
        }   
        
        String windowDescription = 
            traceDescription(item.myTabView.rootView().externalWindow());
        if (windowDescription != null) {
            result = result + " in " + windowDescription;
        }
        return result;
    }
    
    /** description for a window */ 
    static String traceDescription(ExternalWindow window) {
        if (window == null) {
            return null;
        }
        String title = window.title();
        return (emptyOrNull(title) ? "(unnamed)" : title) + " window";
    }
    
    /** Returns true unless string has at least one character */    
    static boolean emptyOrNull(String string) {
        if (string == null) {
            return true;
        }
        return string.length() == 0;
    }   
        
}
