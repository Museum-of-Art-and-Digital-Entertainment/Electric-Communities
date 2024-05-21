#!/bin/sh
#
# This compiles all .java files in the hierarchy of a given source
# directory.

if [ $# != 2 ] ; then
    echo "usage: $0 <source-directory> <class-directory>"
    exit 1
fi

find $1 -name '*.java' -print -exec javac -d $2 -deprecation {} \;
