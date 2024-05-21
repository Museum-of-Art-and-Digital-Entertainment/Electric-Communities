#!/bin/sh
#
# This compiles the filter program and then uses it to filter all of
# the ecomp sources. The output ends up in the directory specified
# as the first argument.

if [ $# != 1 ] ; then
    echo "usage: $0 <output-directory>"
    exit 1
fi

mkdir -p $1
mkdir -p $BUILD/classes
javac -d $BUILD_DRIVE$BUILD/classes filter/FilterEext.java
java ec.tools.filtereext.FilterEext $1 *.eext lang/*.eext
