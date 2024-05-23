# Status of restoration of the tools in this directory:

## Compiles and Runs:
```
/analdump - may need syntax updating - into $BUILD/bin/analdump
/braun - scripts braun and braunie to convert .unit into .e files - untested - should these get copied to $BUILD/bin?
/edoc - compiles with warnings - into $BUILD/steward/classes/ec/edoc
/jwitch - into $BUILD/steward/classes/ec/jwhich
```

## Does not compile and reason: 

```
/bdbi -  Bdbi.e:14: cannot access class ELaunchable; file ec/e/start/ELaunchable.class not found (implements ELaunchable)
/ecomp - Classpath problems? - error: error while loading class Object: ec.ecomp.LoadError: file java/lang/Object.class not found
/ej - gcc -g -o /home/randy/Electric-Communities/Microcosm/Root/Build/objs/ej/ejfileio.o -Wreturn-type -I. -I../../../Tools/yacchelper -g -c ejfileio.c
In file included from generic.h:11,
                 from ejfileio.c:12:
ejfileio.c:30:30: error: initializer element is not constant
   30 | static FILE *CurrentOutput = stdout;
      |
/j4e - same error as for /ej (above)
/upgrade - infinite loop make (recursive submake?)
```
