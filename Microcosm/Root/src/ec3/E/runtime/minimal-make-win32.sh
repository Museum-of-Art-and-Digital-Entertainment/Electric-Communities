#!/bin/sh
#
# Minimal E runtime make for win32. This just sets up some defaults
# and calls into the generic minimal-make. Run with the environment
# variable BUILD set to the destination for the build (classes in
# $BUILD/classes and libraries in $BUILD/lib, etc.). It defaults to
# the current directory.

JAVAHOME=$BUILD_DRIVE'\EC\TP\java-1.1.3'
ARCH="win32"
CC="cl /G5 /GD /O2 -MDd -LDd -Zi -DLTTLE_ENDIAN -DWIN32 -Zp8 -Zi -c"
COUT="-Fo"
COUTSUF=".obj"
LD="link -debug -debug:full -dll -nodefaultlib:libc.lib -nodefaultlib:msvcrt.lib $JAVAHOME/lib/javai.lib"
LDOUT="-out:"
LDOUTNAME="run.dll"

export JAVAHOME
export ARCH
export CC
export COUT
export COUTSUF
export LD
export LDOUT
export LDOUTNAME

minimal-make.sh
