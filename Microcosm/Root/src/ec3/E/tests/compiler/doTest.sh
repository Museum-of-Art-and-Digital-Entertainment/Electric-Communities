#!/bin/sh
#
# Run the given test (first arg), by creating a temporary directory,
# copying all the files in the given directory (again, first arg) to
# it, running the "doit" command and then comparing the output to the
# "expected" file. If output is identical, exit with 0; if not, spit
# out the diffs and exit with 1. In any event, remove the temp
# dir. Note, doit scripts can assume that "." is in the CLASSPATH.
# Also note that the file .last-output is left in the current
# directory if the test fails; this facilitates correcting missing or
# outdated "expected" files.

if [ $# != 1 ] ; then
    echo "usage: $0 <test-directory>"
    exit 1
fi

if [ ! -d $1 ] ; then
    echo "$0: error: testing directory $1 not found"
    exit 1
fi

if [ ! -f $1/doit ] ; then
    echo "$0: error: no 'doit' script found"
    exit 1
fi

dirname=$BUILD/testing-$$
mkdir -p $dirname
cp -r $1/* $dirname
echo " "
echo "Performing test $1 in directory $dirname..."

if [ -f $1/notes ] ; then
    echo "########## PLEASE NOTE ##########"
    cat $1/notes
    echo "########### NOTE ENDS ###########"
fi

(
    cd $dirname
    if [ "x"$WINDIR = "x" ] ; then
        CLASSPATH=.:$CLASSPATH
    else
        CLASSPATH=".;$CLASSPATH"
    fi
    export CLASSPATH
    sh doit > ACTUAL 2>&1
    if [ ! -f expected ] ; then
        echo "No 'expected' output file was found." > DIFFS
        echo "Presumably, you want to make it; see the 'last-test-output'" >> DIFFS
	echo "file in your build directory." >> DIFFS
    else
        diff -c2 expected ACTUAL > DIFFS
    fi
)

if [ -s $dirname/DIFFS ] ; then
    echo "Test $1 differs from expected output:"
    echo "-----------------------------------------------------------------"
    cat $dirname/DIFFS
    echo "-----------------------------------------------------------------"
    rm -rf $BUILD/last-test-output
    cp $dirname/ACTUAL $BUILD/last-test-output
    exitStatus=1
else
    echo "Test $1 succeeded."
    exitStatus=0
fi

rm -rf $dirname
exit $exitStatus
