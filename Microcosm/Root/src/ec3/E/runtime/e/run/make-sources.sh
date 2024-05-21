#!/bin/sh
#
# This script makes the special source files using the ArgMaker tool.

mkdir -p "$BUILD"/tmp
mkdir -p "$BUILD"/gencode/erun
if [ "x"$WINDIR = "x" ] ; then
    pathsep=":"
else
    pathsep=";"
fi
CLASSPATH="$JAVADIR""$pathsep""$BUILD_DRIVE$BUILD"/tmp
export CLASSPATH
set -xe
javac -d "$BUILD_DRIVE$BUILD/tmp" ArgMaker.java
java ec.e.run.ArgMaker EnqueueMaker.txt > $BUILD/gencode/erun/RtEnqueue.java
java ec.e.run.ArgMaker QObjMaker.txt > $BUILD/gencode/erun/RtQObj.java
rm -rf "$BUILD"/tmp
