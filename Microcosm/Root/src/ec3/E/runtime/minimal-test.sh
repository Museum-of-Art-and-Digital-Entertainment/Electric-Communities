#!/bin/sh
#
# Simple and minimal test for the minimal E. Note that the EDIR environment
# variable should be set to a location containing a classes.zip file for
# ecomp.

pathsep=${PATH_SEPARATOR:-:}
build=${BUILD_DRIVE:-}${BUILD:-.}
javahome=${JAVAHOME:-/home/eng/java-1.1.3/solaris/java-1.1.3}
edir=${EDIR:-/home/eng/e-none}
eclasses=$edir/classes.zip

classes=$build/classes
mkdir -p $classes
CLASSPATH=$JAVAHOME/lib/classes.zip$pathsep$classes$pathsep$eclasses
export CLASSPATH
ecomp -d $classes minimal-test.e
java minimal.Test
rm -rf /tmp/classes
