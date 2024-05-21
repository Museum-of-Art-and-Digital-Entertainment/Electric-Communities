#!/bin/sh
#
# This will copy over to the build files in win32/bin and win32/lib
# that aren't already in the build, but only if it hasn't been done
# before--it checks for $BUILD/objs/.win32copy-done and will touch
# it when it's done.

if [ -f $BUILD/objs/.win32copy-done ] ; then
    echo "(win32 bin and libs already copied)"
else
    echo "(copying win32 bin and libs from pre-built version)"
    mkdir -p $BUILD/bin
    cd $EDIR/win32/bin
    for x in *; do
        if [ ! -f $BUILD/bin/$x ] ; then
            cp $x $BUILD/bin
        fi
    end
    mkdir -p $BUILD/lib
    cd $EDIR/win32/lib
    for x in *; do
        if [ ! -f $BUILD/lib/$x ] ; then
            cp $x $BUILD/lib
        fi
    end
    mkdir -p $BUILD/objs
    touch $BUILD/objs/.win32copy-done
fi
