Magic Incantations for Debugging E with Symantec Cafe

The E to Java translator is now sufficiently functional that it is possible to
begin using the visual debugger in Symantec Cafe to debug E code.

Currently there are a number of limitations and rough edges and the procedure
is a little complicated. Hopefully all of this will improve in the fullness of
time. In the meanwhile, this note describes what you can do, how you do it, and
what you can't do.

WHAT YOU NEED

1. The E to Java translator, ej -- It lives in ec3/E/tools/ej.

    LIMITATION #1: ej should be made a standard part of the build
    environment. I don't know how to do that and I wouldn't necessarily trust
    me to do it if I did. But those of you who take care of the build
    environment (and you know who you are) can. For now, one must to check out
    the code and compile it one's self.

2. The new inner-class compatible E runtime, in ec3/E/runtime/e/run -- if you
keep your world up to date you should just have this already (and if you never
use ej you will never notice that the new runtime stuff is there).

3. Symantec Cafe -- you get it from Fry's or Rob or someplace like that.

    LIMITATION #2: all of what follows only works with Symantec Cafe. In theory
    it should work with any Java IDE, but in practice that's not how it is. All
    the Java IDE's have serious deficiencies; for our purposes Cafe is the
    merely least deficient. Note also that everything I describe here about
    Cafe refers to version 1.8; if you have an older version you will need to
    upgrade.

    Once Metrowerks' has released their Java 1.1 implementation for Windows, I
    expect to shift over to CodeWarrior, since in all respects save support
    for inner classes it already sucks less than Cafe; compiler support for
    inner classes is the one critical thing it currently lacks. And once
    Metrowerks releases its compiler plugin API for Windows, we should be able
    to actually integrate ej right into Code Warrior, which will be a huge win.
    I've tried using their pre-release Java 1.1 on the Mac and it works great.
    *That* would be usable now, except that our stuff relies on native code
    which hasn't been ported to the Mac.

    We've looked into other IDEs a bit. Microsoft's J++ has a non-standard
    native code interface that would require an expensive conversion of all our
    native code. Their native code stuff is also ill behaved with respect to
    garbage collection. I'm told by MarcS that Borland's IDE is not a serious
    contender here; I'm not entirely sure what that means but I believe him.
    I have not yet looked seriously into Asymetrix; this is another thing on my
    todo list.

DEBUGGING E WITH CAFE

Step 1: Create your project

The first thing to do in Cafe is create a new project by picking "New..." off
the Project menu. You *can* use the IDE in the way it was intended, with a
separate project for each thing that you are working on, but currently using
Cafe with E is so indirect that I've found it better just to have a single
project for all my E stuff. I named mine "EDebug". So name your project, e.g.,
"EDebug", in the new project dialog window, and place it at the root of your
source tree. It's easier to accept all the defaults and then go back and tweak
a few things than it is to answer all of Cafe's questions, so go ahead and
click the check box that says "Use AppExpress to create new application" and
then click the "Finish" button. When the AppExpress dialog comes up, choose
"Console Application" and then click "Finish" again, and let Cafe do all the
detail work.

Step 2: Give the project a better "main"

AppExpress will have conveniently created a main class for you, and placed it
in a Java source file in your project root directory. The class will be named
after the project. The first thing you need to do is tweak the main. One of the
problems with Cafe is that it doesn't let you configure what class to
launch. It says it does but it lies. It wants to launch the project's main. But
for E code you really want to launch EBoot, which is not part of your
project. The hack to work around this is to substitute the following main
class:

    public class EDebug {
        public static void main(String args[]) {
            try {
                ec.e.start.EBoot.main(args);
            } catch (Exception e) {
                System.out.println("oops: " + e);
            }
            try {
                System.in.read();
            } catch (java.io.IOException e) {
                return;
            }
        }
    }

The call to system.in.read() is just so that you can look at the output window
before your application exits. If you don't care about this, leave it out. A
minimal, valid project main is:

    public class EDebug {
        public static void main(String args[]) throws Exception {
            ec.e.start.EBoot.main(args);
        }
    }

Step 3: Tweak various project settings

Choose the "Arguments" command from the "Project" menu. Fill in the arguments
dialog with whatever the arguments to EBoot would have been if you were
launching this thing from run.bat. Minimally this is just the fully qualified
class name of the E class that you want to launch. Click "OK".

Choose the "Settings" command from the "Project" menu...

Under the "Target" tab you should make sure the "Debug" radio button is
selected, that the Main Class is the same as your main class, that the Platform
is Java, and that the Target Type is Application. Incidently, you'd think that
you could set the Main Class here to ec.e.start.EBoot, but if you thought that
you'd be wrong. Most importantly, make sure the check box "Automatically Parse"
is unchecked (this is NOT the default and is the one thing on this tab you'll
probably actually have to change).

The "Build" and "Interpreter" tabs are OK as is, but you need to fiddle with
some stuff under the "Directories" tab. The Class Path needs to be
substantially augmented. By default it includes the Cafe's class libraries; I'm
sure these are OK and in my projects I left them at the front of the Class
Path. However, you also need to add all the bazillion directories on the
standard CLASSPATH that we use. I cut'n'pasted mine from the 4DOS build
environment startup script. Note that Cafe will not let you put any directory
here which does not actually exist, so you may need to edit to get it to
work. Our default CLASSPATH includes some directories that not everybody always
has. The Source Search Path is where the debugger (note: not the compiler,
oddly enough) will look for source files. You need to include any directories
that will contain source you want to debug. This can be tedious, and is not
aided by the fact that the default you start with is whatever it was in the
last project you had open.

The final thing you need to do is make sure that Cafe's Java interpreter can
find all the native code DLLs and such. I'm still unclear on the right way to
do that; there's no way to tell Cafe directly. I ended up pasting the PATH
environment variable setting from my 4DOS build environment startup script into
my AUTOEXEC.BAT file. This is crude and no doubt will offend some Windows usage
aesthetic (assuming that's not an oxymoron), but it worked for me.

Step 4: Grind your E code with ej

One of the good things about Cafe is that it is happy to link and run with
classes that aren't in your project, so the only things you have to add to your
project are the things you actually want to debug. Thus the only eclasses you
have to grind through ej are the eclasses you actually want to debug. If your
eclass "EFoo" is in the file "EFoo.e", then the minimal command is:

    ej EFoo.e

It's that easy.

By default, ej will use the current working directory as the root of the
package hiearchy for output. If that's not what you want (and it probably
isn't), the command line option:

    -d dirname

will set the directory "dirname" to be the root instead (this is the same as
ecomp and pl).

By default, ej will use the current value of the CLASSPATH environment variable
as its classpath. The command line option:

    -c classpath

will add whatever directories are specified in "classpath" to the front of the
class path. Note that ej is like ecomp in that it reads .class files for
information about previously compiled classes, but knows about all the files it
is currently compiling so that mutual references among them are resolved. That
is, you can compile multiple files at once with ej:

    ej EFoo.e EBar.e EBaz.e

and cross references among these classes will work the way they are supposed
to.

For a given input E class EFoo, you get seven output Java classes: EFoo,
EFoo_$_Intf, EFoo_$_Impl, EFoo_$_Sealer, EFoo_$_Channel, EFoo_$_Proxy and
EFoo_$_Deflector. Yes, this is really icky. Dan is working on reducing this
number from seven to three or four, and someday ej will probably be able to
generate most of these as .class files directly, and in some even more advanced
era the Vat loader will be able to synthesize all of these as needed given a
single class file. But that's in the future. In any case, the only one of these
that you really care about is EFoo_$_Impl, which contains the actual code that
you might want to debug. By default, ej makes the source lines of the output
EFoo_$_Impl.java file line up one-to-one with the source lines of the input
EFoo.e source file. Actually, this is the whole point of ej. However, for
diagnostic purposes it is sometimes useful to see all the machine generated
glorpf in a more legible format, and for this there is the command line option:

    -l

which causes the output to be legible instead of to line up.

One of the limitations of Cafe is that it deeply believes in the whole stupid
Javasoft idea of one source file per class and a source tree which exactly
mirrors the package tree. So ej generates its output according to this
convention. Thus, if your class EFoo.e was declared in the package foo.bar.baz,
and you use an ej command line such as:

    ej -d mysources/synth EFoo.e

you will wind up with the output in seven files

    mysources/synth/foo/bar/baz/EFoo.java
    mysources/synth/foo/bar/baz/EFoo_$_Channel.java
    mysources/synth/foo/bar/baz/EFoo_$_Deflector.java
    mysources/synth/foo/bar/baz/EFoo_$_Impl.java
    mysources/synth/foo/bar/baz/EFoo_$_Intf.java
    mysources/synth/foo/bar/baz/EFoo_$_Proxy.java
    mysources/synth/foo/bar/baz/EFoo_$_Sealer.java

and so on. If there was more than one class in your input file (which ecomp
allows) you will get the corresponding expansion. I.e., if your single source
file declared three eclasses, you will have 21 output files generated. This
sucks, and is another reason why all the people at Javasoft are
damned. However, we must live with it for now. When Metrowerks brings
CodeWarrior with Java 1.1 to Windows this will be fixed, but that's in the
indefinite future.

WARNING: if you have Java classes in your input, they will go into their own
output files. Thus if you have multiple Java classes (as permitted by ecomp and
common practice here at EC), ej will nicely split up your source, even if it's
all Java code. However, ej doesn't care about what file name it is processing,
so if you feed it a file named Foo.java as input containing a class Foo, it
will happily write its output to Foo.java which is probably not what you
want. It's a good idea to only feed ej .e files. Enforcing this restriction is
another thing on my todo list.

Step 5: Offer up your .java files to Cafe

Take all these .java files that were generated by ej and put them in the
appropriate source directory for your Cafe project, assuming you didn't just
have ej generate them right into that directory to begin with (once ej runs on
Windows, that'll be the right thing to do).

In Cafe, add these sources to your project. The secret command is "Edit" on the
"Project" menu. Note that when you change your source, you need to regrind it
with ej and copy the new .java files into the project source directory, but you
don't need to add the files to the project again, unless you changed a class
name or something that would actually result in a different set of .java files.

Next (this is magic), in your project window, choose the command "Parse All"
from the "Parse" menu. You will notice all the "No"s in the Parsed column turn
to "Yes"es. Like adding, this is something that only has to be done once, when
the files are added. The purpose of this is basically to fool Cafe into
believing something which it needs to believe is true, even though it doesn't
actually need it to actually be true. Just trust me on this one, OK?

Tell Cafe to build your project. Assuming that you had something that compiled
correctly to begin with, this should just work. Other than some limitations
which I'll describe momentarily, if it doesn't work, there's a good chance you
have found a bug in ej and you should tell me about it.

    LIMITATION #3: Due to a flaw in the Java inner classes design, inner
    classes cannot refer to local variables of a containing method, even though
    those variables are in scope, unless those variables are declared final.
    Our code generation technique for ej uses inner classes to implement
    closures, which are what you get in ewhens, ecatches, and the like. For
    example:

    emethod doSomethingWithAnEInteger(EInteger foo) {
        int someNumber = complicatedFunction();
        ewhen foo (int fooVal) {
            if (fooVal > someNumber)
                someNumber = fargulate(someNumber);
                messWithSomeInts(fooVal, someNumber);
        }
    }

    This will generate an error from the Java compiler. The following
    equivalent code *will* work however:

    emethod doSomethingWithAnEInteger(EInteger foo) {
        final int someNumber[] = { complicatedFunction() };
        ewhen foo (int fooVal) {
            if (fooVal > someNumber[0])
                someNumber[0] = fargulate(someNumber[0]);
                messWithSomeInts(fooVal, someNumber[0]);
        }
    }

    Basically, you replace the variable with a final 1-element array. Some day,
    ej will do this transformation for you automagically. Until that day, if
    you run into this case you will have to do this transformation yourself, by
    hand. And maybe someday Javasoft will get a clue and change the language
    spec to define this problem away.

Step 6: Get ready to rumble

The final magic step is to copy your .e file on top of your _$_Impl.java file,
e.g.,

    cp EFoo.e EFoo_$_Impl.java

It is important that you do this *after* you have told the project to parse the
source file and *after* you have compiled, BUT make sure that you copy in such
a fashion that the overwritten .java file still has an earlier modification
time than the corresponding .class file. (My experience is that just dragging
the file icons around does what you want here. Normally I'd say that when you
copy a file onto another file the modification time on the file copied onto
should be the time that the copy happened, but it appears that under Windows it
ends up being the modification time of the file copied *from*. This is the
first case in my experience of a Windows design misfeature working in my
favor.)

Then go to it. Set break points. Look at variables. Debug. Try to figure out
the Cafe debugger. All that stuff.

    LIMITATION #4: Cafe has no manual. I'm not kidding.

    LIMITATION #5: Cafe won't let you set breakpoints inside inner classes.
    Maybe they will fix this in a future release. And Metrowerks does not have
    this problem. But for now...

    There is a workaround, if you are desparate enough. Let's say you have
    something like the following:

    eclass EFoo {
        emethod doSomething(EInteger foo) {
            ewhen foo (int fooVal) {
                horrible bug;
            }
        }
    }

    If you try to set a breakpoint on the horrible bug line, Cafe will give you
    a helpful message of the form: "No code at line 47, source file has not
    been parsed completely, or class is optimized."  Never mind that you and I
    know this is a bald-faced lie. The fix is to cut'n'paste your code to move
    the guts of the ewhen into a method of the containing class, then call this
    method in the ewhen.

    eclass EFoo {
        emethod doSomething(EInteger foo) {
            ewhen foo (int fooVal) {
                EFoo.this.fooHack(fooVal);
            }
        }
        void fooHack(int fooVal) {
            horrible bug;
        }
    }

    You will then be able to set breakpoints in the fooHack method. Note the
    peculiar syntax for calling an outer method from an inner class.

Step 7: Debug until your problem goes away

If you need to cycle changes, remember that after editing your .e source, you
need to regrind with ej, recopy the .java files, recompile, then recopy the .e
files onto the _$_Impl.java files.  Ideally we will figure out a way to
automate or at least semi-automate this cycle. But until we do you're on your
own to remember the steps.


That's it! This whole process should improve with time, as we improve the
tools, vendors improve their tools, and we figure out how to make more of the
steps automatic. It can only get better from here.


