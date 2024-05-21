// Bitmap.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.awt.image.ImageObserver;


/** This class exists to assist with incremental bitmap loading, as well as
  * getting a Bitmap's size information without having to have all of the
  * Bitmap's data present.
  */


class BitmapObserver implements ImageObserver {
    Application         application;
    Bitmap              bitmap;
    int                 lastInfo;

    BitmapObserver(Application application, Bitmap bitmap) {
        super();

        this.application = application;
        this.bitmap = bitmap;
    }

    public synchronized boolean imageUpdate(java.awt.Image image,
                                            int infoflags, int x, int y,
                                            int width, int height) {
        Target          updateTarget;

        lastInfo = infoflags;

        if (image == null) {
            return true;
        }

        /* inform waiting bitmap that its width, height or data might
         * be available
         */
        if ((infoflags & ImageObserver.WIDTH) != 0 ||
            (infoflags & ImageObserver.HEIGHT) != 0 ||
            (infoflags & ImageObserver.PROPERTIES) != 0 ||
            (infoflags & ImageObserver.ALLBITS) != 0 ||
            (infoflags & ImageObserver.ERROR) != 0 ||
            (infoflags & ImageObserver.ABORT) != 0) {
            notifyAll();

            return true;
        }

        if (!bitmap.loadsIncrementally()) {
            return true;
        }

        if ((infoflags & ImageObserver.SOMEBITS) != 0) {
            bitmap.unionWithUpdateRect(x, y, width, height);
            updateTarget = bitmap.updateTarget();
            if (updateTarget != null) {
                application.performCommandLater(updateTarget,
                                                bitmap.updateCommand(),
                                                bitmap, true);
            }
        } else if ((infoflags & ImageObserver.FRAMEBITS) != 0) {
            updateTarget = bitmap.updateTarget();
            if (updateTarget != null) {
                application.performCommandLater(updateTarget,
                                                bitmap.updateCommand(),
                                                bitmap, true);
            }
        }

        return true;
    }

    synchronized boolean allBitsPresent() {
        return (lastInfo & ImageObserver.ALLBITS) != 0;
    }

    synchronized boolean imageHasProblem() {
        return ((lastInfo & ImageObserver.ERROR) != 0 ||
                (lastInfo & ImageObserver.ABORT) != 0);
    }
}
