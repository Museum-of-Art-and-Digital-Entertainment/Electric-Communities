#!/bin/sh
#
# This is the program to turn the -printe2jtree output of compiling
# EObject.e into actual compilable java.

sed -e 's/new java.lang.Object\[\] //g' -e 's/String OUTPUT = "\(.*\)";/\1/g' $1 > $2
