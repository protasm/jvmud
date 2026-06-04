# JVMud Runtime

This directory is reserved for JVMud runtime and server code: object hosting,
world services, persistence, scheduling, networking, and integration with
compiled LPC classes.

The compiler source currently includes a compiler-adjacent runtime helper package
at `compiler/src/main/java/io/github/protasm/jvmud/compiler/runtime/`, plus
host-facing execution classes under `io.github.protasm.jvmud.compiler.exec`.
Those are part of the compiler module and should not be confused with this
future top-level runtime module.

No runtime build or source layout has been established here yet.
