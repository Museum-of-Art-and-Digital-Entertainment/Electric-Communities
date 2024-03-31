This directory holds the C sources and examples for the "Pluribus" compiler, which transforms `.plu` files into "units" for further processing by the internal toolchain.

## Build Instructions

Just type `make` from the `Pluribus/Compiler` directory. If you modify the Pluribus grammar file `pluribus.y`, you will also need `bison` installed to rebuild the compiler tokens.

## TODO

* Documentation relating E, Pluribus, and Unum.
* Description of the shape of the tools themselves; where C ends and Java begins, etc.
* Test cases and sample code
* Modernize the original code so that it builds without complaint on default modern compiler settings.
