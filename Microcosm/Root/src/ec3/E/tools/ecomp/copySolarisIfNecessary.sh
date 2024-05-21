#!/bin/sh
#
# This will copy over solaris bin and libs into the build, but only
# if it hasn't been done before--it checks for $BUILD/objs/.solariscopy-done
# and will touch it when it's done.

if [ -f $BUILD/objs/.solariscopy-done ] ; then
    echo "(solaris bin and libs already copied)"
elif [ ! -d $BUILD/solaris/bin ] ; then
    echo "(warning: cannot find solaris stuff to copy)"
else
    echo "(copying solaris bin and libs from pre-built version)"
    mkdir -p $BUILD/solaris/bin
    mkdir -p $BUILD/solaris/lib
    cp -r $EDIR/solaris/bin/* $BUILD/solaris/bin
    cp -r $EDIR/solaris/lib/* $BUILD/solaris/lib
    mkdir -p $BUILD/objs
    touch $BUILD/objs/.solariscopy-done
fi
