# Conjure deps

Runtime dependencies for [Conjure][], this is only required for ClojureScript environments at the moment, Clojure environments will have the dependencies injected automatically.

If you don't include this in your project Conjure will still connect to ClojureScript environments but will only allow you to perform evaluations.

```clojure
;; deps.edn
olical/conjure-deps {:mvn/version "0.1.0"}

;; project.clj
[olical/conjure-deps "0.1.0"]
```

## Munged ahead of time

"Munging" is the term I use to describe rewriting dependency namespaces such that they won't conflict with any of your project dependencies via the wonderful [MrAnderson][].

All versions of outputs of this process are contained within this repository and the Clojars artifact, this allows the project to be entirely backwards compatible.

The source code of the projects I depend on should have their original licenses respected despite being contained within this repository under my license of choice. My license only applies to my code that helps fetch and munge the correct files.

## Unlicenced

Find the full [unlicense][] in the `UNLICENSE` file, but here's a snippet.

>This is free and unencumbered software released into the public domain.
>
>Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

[conjure]: https://github.com/Olical/conjure
[unlicense]: http://unlicense.org/
[mranderson]: https://github.com/benedekfazekas/mranderson
