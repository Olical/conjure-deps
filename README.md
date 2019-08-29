# Conjure deps

Runtime dependencies for [Conjure][]. You don't need to add this to your project, it's just split into a separate repository at the moment to keep the main one neater.

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
