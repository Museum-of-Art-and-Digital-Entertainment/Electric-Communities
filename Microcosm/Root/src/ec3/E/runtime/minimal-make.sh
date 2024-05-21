#!/bin/sh
#
# Minimal make for E runtime. Run with environment variable
# BUILD set to the destination for the build (classes in
# $BUILD/classes and libraries in $BUILD/lib, etc.).
# It defaults to the current directory. Also, run with
# JAVAHOME set to the base of the Java distribution. It
# defaults to "/home/eng/java-1.1.3/solaris/java-1.1.3"

build=${BUILD_DRIVE:-}${BUILD:-.}

javahome=${JAVAHOME:-/home/eng/java-1.1.3/solaris/java-1.1.3}

arch=${ARCH:-`arch`}
if [ $arch = sun4 ]; then
	arch=solaris
fi

cc1=${CC:-"gcc -g -c"}
cc="$cc1 -I$javahome/include -I$javahome/include/$arch -I$build/headers"

cout=${COUT:-"-o "}
coutsuf=${COUTSUF:-".o"}

ld=${LD:-"ld -G"}
ldout=${LDOUT:-"-o "}
ldoutname=${LDOUTNAME:-"librun.so"}

mkdir -p $build/classes
mkdir -p $build/lib
mkdir -p $build/headers
mkdir -p $build/stubs
mkdir -p $build/obj
mkdir -p $build/gencode/erun

FILES_1="
	vcache/BooleanCache.java
	vcache/ByteCache.java
	vcache/CharCache.java
	vcache/DoubleCache.java
	vcache/FloatCache.java
	vcache/IntCache.java
	vcache/LongCache.java
	vcache/ShortCache.java
	vcache/VCache.java
	e/run/RtFileDummies.java
	e/run/RtNetDummies.java
	e/run/RtRunDummies.java
	e/run/RtStartDummies.java
	e/run/TraceDummies.java
	e/run/TimerDummies.java
"

FILES_2="
	util/EMainThread.java
	util/EThreadGroup.java
	util/ExceptionNoticer.java
	util/Humanity.java
	util/NestedError.java
	util/NestedThrowable.java
	util/NestedThrowableVector.java
	util/ReadOnlyHashtable.java
	e/run/ClassDemangler.java
	e/run/EBoolean.e
	e/run/ECatchClosure.e
	e/run/EChannel.java
	e/run/EDelegator.java
	e/run/EDistributor.e
	e/run/EInterface.java
	e/run/ENullTether.java
	e/run/eParty.java
	e/run/EProxy.e
	e/run/EResult.e
	e/run/EStone.java
	e/run/EUniChannel.java
	e/run/EUniDistributor.e
	e/run/EWhenClosure.e
	e/run/Exportable.java
	e/run/InternalClosures.java
	e/run/OnceOnlyException.java
	e/run/RtAssignedTether.java
	e/run/RtCausality.java
	e/run/RtDeflector.java
	e/run/RtEnvelope.java
	e/run/RtErrorException.java
	e/run/RtExceptionEnv.java
	e/run/RtFinalizer.java
	e/run/RtInterfaces.java
	e/run/RtInvocation.java
	e/run/RtInvocationException.java
	e/run/RtQ.java
	e/run/RtRun.java
	e/run/RtRuntimeException.java
	e/run/RtSealer.java
	e/run/RtTether.java
	e/run/RtWeakCell.java
	e/run/RtWeakling.java
	e/run/UnknownSealer.java
	$build/gencode/erun/RtQObj.java
	$build/gencode/erun/RtEnqueue.java
"

FILES_3="
    e/run/EObject.e
"

FILES_4="
	e/lang/EDouble.e
	e/lang/EFloat.e
	e/lang/EInteger.e
	e/lang/ELong.e
	e/lang/EPrintStream.e
	e/lang/EString.e
	e/run/ENull.e
	e/run/ETrace.e
"

FILES_EXPORT=ec.e.run.RtWeakCell

FILES_C=e/run/RtWeakCell.c

set -xe
(cd e/run; make-sources.sh)
ecomp -d $build/classes -e2jdone $FILES_1
ecomp -d $build/classes -esystem $FILES_2
ecomp -d $build/classes -esystem $FILES_3
ecomp -d $build/classes -esystem $FILES_4
javah -classpath ${CLASSPATH}:$build/classes -d $build/stubs -stubs $FILES_EXPORT
javah -classpath ${CLASSPATH}:$build/classes -d $build/headers $FILES_EXPORT
for x in $FILES_C $build/stubs/*.c; do
	out=$build/obj/`basename $x .c`$coutsuf
	$cc $x $cout$out
done
$ld $ldout$build/lib/$ldoutname $build/obj/*$coutsuf
echo "Done"
