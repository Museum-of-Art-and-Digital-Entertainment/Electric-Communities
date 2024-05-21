#!/bin/sh
#
# Simple and minimal test for the minimal E--win32 version.

JAVAHOME=$BUILD_DRIVE'\EC\ThirdParty\java-1.1.3'
PATH_SEPARATOR=";"

export JAVAHOME
export PATH_SEPARATOR

minimal-test.sh
