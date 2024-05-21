#!/bin/sh
java -ms8m ec.ecomp.Main -resultfile $BUILD/ecomp_result $*
exit `cat $BUILD/ecomp_result`
#RESULT=`cat "c:\ECDev\ecomp_result"`
#if [ $RESULT = "0" ] ; then
#  exit 0
#else
#  exit 1
#fi
