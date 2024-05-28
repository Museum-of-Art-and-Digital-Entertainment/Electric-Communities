# Status of restoration of the tools in this directory:

## Compiles and Runs:
```
/analdump - may need syntax updating - into $BUILD/bin/analdump
/braun - scripts braun and braunie to convert .unit into .e files - untested - should these get copied to $BUILD/bin?
/edoc - compiles with warnings - into $BUILD/steward/classes/ec/edoc
/ej - compiles with warnings - into $BUILD/bin/ej - ej -h' for help
/j4e - compiles with warnings - into $BUILD/bin/j4e - j4e -h for help
/jwitch - into $BUILD/steward/classes/ec/jwhich
```

## Does not compile and reason: 
```
/bdbi -  Bdbi.e:14: cannot access class ELaunchable; file ec/e/start/ELaunchable.class not found (implements ELaunchable)
/ecomp - Classpath problems? - error: error while loading class Object: ec.ecomp.LoadError: file java/lang/Object.class not found
/upgrade - infinite loop make (recursive submake?)
```
