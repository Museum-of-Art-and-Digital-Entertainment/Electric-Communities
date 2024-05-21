// JDK11AirLock.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

class JDK11AirLock {
    static boolean lookedForPrintClass = false;
    static Class printClass = null;
    static boolean lookedForMenuShortcut = false;
    static boolean menuShortcutExists = false;

    static Clipboard clipboard() {
        Class cClass, jClass;

        try {
            cClass = Class.forName("java.awt.datatransfer.Clipboard");
            jClass = Class.forName("netscape.application.jdk11compatibility.JDKClipboard");
            return (Clipboard)jClass.newInstance();
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    static boolean setMenuShortcut(MenuItem item, char key) {
        Class cClass, jClass;
        MenuShortcut s;

        try {
            cClass = Class.forName("java.awt.MenuShortcut");
            jClass = Class.forName(
                  "netscape.application.jdk11compatibility.JDKMenuShortcut");
            s = (MenuShortcut)jClass.newInstance();
            if (s != null) {
                s.setMenuShortcut(item.foundationMenuItem, key);
                return true;
            }
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return false;
    }

    static boolean menuShortcutExists() {
        Class cClass, jClass;

        if (!lookedForMenuShortcut) {
            lookedForMenuShortcut = true;
            try {
                cClass = Class.forName("java.awt.MenuShortcut");
                jClass = Class.forName(
                   "netscape.application.jdk11compatibility.JDKMenuShortcut");
                menuShortcutExists = true;
            } catch (ClassNotFoundException e) {
            }
        }
        return menuShortcutExists;
    }

    static boolean isPrintGraphics(java.awt.Graphics g) {
        if (!lookedForPrintClass) {
            lookedForPrintClass = true;
            try {
                printClass = Class.forName("java.awt.PrintGraphics");
            } catch (ClassNotFoundException e) {
            }
        }
        if (printClass != null) {
            Class gClass;

            for (gClass = g.getClass();
                 gClass != null;
                 gClass = gClass.getSuperclass()) {
                Class interfaces[] = gClass.getInterfaces();
                int count = interfaces.length;

                while (count-- > 0) {
                    if (interfaces[count].equals(printClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
