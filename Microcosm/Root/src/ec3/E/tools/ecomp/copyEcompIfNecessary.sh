#!/bin/sh
#
# This copies $EDIR/classes and $EDIR/bin into $BUILD, but only if
# $BUILD/objs/.ecomp-done doesn't already exist. This prevents make
# from doing this copy over and over unnecessarily, and also prevents
# trashing a manually-made ecomp by a premade one.

if [ -f $BUILD/objs/.ecomp-done ] ; then
    echo "(ecomp already made or copied)"
else
    echo "(copying ecomp from pre-built version)"
    mkdir -p $BUILD/classes
    if [ -d $EDIR/classes ] ; then
        cp -r $EDIR/classes/* $BUILD/classes
    elif [ -f $EDIR/classes.zip ] ; then
        echo "(unzipping ecomp classes.zip)"
        unzip -qo $EDIR/classes.zip -d $BUILD/classes
    else
        echo "error: could not find E distribution to copy from"
        exit 1
    fi
    mkdir -p $BUILD/bin
    cp $EDIR/win32/bin/ecomp* $BUILD/bin
    mkdir -p $BUILD/objs
    touch $BUILD/objs/.ecomp-done
fi
