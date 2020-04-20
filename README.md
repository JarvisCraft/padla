# PADLA

[![License](https://img.shields.io/github/license/JarvisCraft/padla)](/LICENSE)
[![Build Status](https://travis-ci.org/JarvisCraft/padla.svg?branch=development)](https://travis-ci.org/JarvisCraft/padla)
[![CodeFactor](https://www.codefactor.io/repository/github/jarviscraft/padla/badge)](https://www.codefactor.io/repository/github/jarviscraft/padla)

###### Plain And Direct Language Additions for Java

## What is it?

PADLA is a collection of useful general-purpose utilities for Java aimed at fulfilling common needs.
It uses functional approach intensively and attempts to follow OOP-approach as right as possible.
In addition to common tools, it does also provide more specific ones aimed at maximal productivity.

## Dependencies

As its dependencies PADLA uses:
- Compiletime:
  - [Lombok](https://github.com/rzwitserloot/lombok) for generating boilerplate stuff
  - [Jetbrains Annotations](https://github.com/JetBrains/java-annotations) for documenting code logic
- Runtime:
  - [Guava](https://github.com/google/guava) for some general-purpose needs
  - [GSON](https://github.com/google/gson) for manipulating JSON
- Testing:
  - [Junit5](https://github.com/junit-team/junit5/) with related sub-tools for testing
  - [Hamcrest](https://github.com/hamcrest/JavaHamcrest) for more creating more readable tests
  - [Mockito](https://github.com/mockito/mockito) for mocking in tests
- Additional (these are not inherited by default and are required only if using specific classes):
  - [ASM](https://gitlab.ow2.org/asm/asm) for runtime class generation (if using classes annotated with `BytecodeLibrary(ASM)`)
  - [Javassist](https://github.com/jboss-javassist/javassist) for runtime class generation (if using classes annotated with `BytecodeLibrary(JAVASSIST)`)
