Learning Haskell through Java
=============================

This project is an educational implementation of a Haskell-like runtime system written in Java.
It attempts to show case how the lazily graph reduction system works under the hood.

It turns out with the help of Java 8 lambdas and the extra type inference they get, combined
with static imports, it is possible to write fairly straightforward Haskell-like code in Java.

There seems to be no way to implement type classes, nor any kind of syntactic abstraction, not
to mention pattern matching and such, but other than that, for simple value level operations,
Java 8 seems pretty usable.

Getting Started
---------------

To run the examples,

    $ git clone git@github.com:eallik/learning-haskell-runtime-in-java.git
    $ cd learning-haskell-runtime-in-java
    $ javac *.java && java Examples

The RTS itself sits in `Thunk.java` — yes, there is not much more to it than `Thunk`.

Future Plans
------------

It would be nice to showcase how Haskell `IO` works by implementing a more realistic version of
`prelude.IO` with `>>=` and all, as well as a linter that parses the `.java` files in a project
and determines if anything non-pure is going on where it shouldn't.

For some more ideas, see [issue #1](https://github.com/eallik/learning-haskell-runtime-in-java/issues/1).
