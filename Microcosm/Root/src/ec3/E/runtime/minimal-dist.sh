#!/bin/sh
#
# Build a minimal source distribution for the E runtime. Run in the
# directory that this script resides in (E/runtime).

if [ x$1 = x ]; then
	echo "$0: must specify a target directory name"
	exit 1
fi

dest=$1
echo "Building dist in $dest"

rm -rf $dest
mkdir -p $dest/ec/e/lang
mkdir -p $dest/ec/e/run
mkdir -p $dest/ec/util
mkdir -p $dest/ec/vcache

cp e/lang/*.e $dest/ec/e/lang
cp e/lang/*.java $dest/ec/e/lang
cp e/run/*.c $dest/ec/e/run
cp e/run/*.e $dest/ec/e/run
cp e/run/*.java $dest/ec/e/run
cp e/run/*.sh $dest/ec/e/run
cp e/run/*.txt $dest/ec/e/run
cp vcache/*.java $dest/ec/vcache
for i in EMainThread.java EThreadGroup.java Humanity.java NestedError.java NestedThrowable.java ReadOnlyHashtable.java NestedThrowableVector.java ExceptionNoticer.java; do
    cp util/$i $dest/ec/util
done

cp minimal-* $dest/ec

cd $dest
tar cf ec.tar ec
gzip ec.tar
