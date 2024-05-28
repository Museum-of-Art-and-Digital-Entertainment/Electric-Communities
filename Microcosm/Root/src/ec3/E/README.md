# This is the root directory for compiling [Original-]E

It doesn't fully compile yet, and it requires the E compiler in order to compie the E compiler (ecomp) and runtime.

To bootstrap this process, we've recovered the e (and other) classfiles from the R167 build and have out them into this build tree. To use them, you'll need to set your $PATH and $CLASSPATH appropriately.

Here's an excerpt of my (Randy's) .bash_aliases related to making sure all the environment is set up to get the historical make system functional:

```export   TOP="/home/randy/Electric-Communities/Microcosm/Root/src/ec3"
export BUILD="/home/randy/Electric-Communities/Microcosm/Root/Build"
export  EDIR="$TOP/E"
export PLATFORM_OS="solaris"
export PATH=.:$BUILD/bin:$PATH
export CLASSPATH=.:/home/randy/Electric-Communities/Microcosm/Root/src/ec3/E/classes
alias   top='cd $TOP'
alias build='cd $BUILD'
alias     E='cd $EDIR'
```



## TODO:
Prune E/classes and E/solaris to just be E? Replace with the "correct" E for this archive (ec3?)

It is not clear that ec3 is the right archive, should we roll back to ec2 or ec2-giblets? Here's something that might be a clue from the currently failing e runtime compile failure:

```ecomp [...]
../../util/EMainThread.java:14: cannot access class ClassCache; class file has wrong version 55.0, should be 45.3
        Class mainClass = ClassCache.forName(mainClassName);
                          ^
RtDeflector.java:83: cannot access class ClassCache; class file has wrong version 55.0, should be 45.3
            theClass = ClassCache.forName(className + "_$_Deflector");
                       ^
RtSealer.java:103: cannot access class ClassCache; class file has wrong version 55.0, should be 45.3
                    result = ClassCache.forName(clName);
                             ^
RtSealer.java:320: cannot access class ClassCache; class file has wrong version 55.0, should be 45.3
            theClass = ClassCache.forName(className + "_$_Sealer");
```


