# PADLA
<details>
<summary>Logo</summary>

![logo](./.branding/logo_512x512.png)
</details>

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-6-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![License](https://img.shields.io/github/license/JarvisCraft/padla)](/LICENSE)
[![Build Status](https://travis-ci.com/JarvisCraft/padla.svg?branch=development)](https://travis-ci.com/JarvisCraft/padla)
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
- Testing:
  - [Junit5](https://github.com/junit-team/junit5/) with related sub-tools for testing
  - [Hamcrest](https://github.com/hamcrest/JavaHamcrest) for more creating more readable tests
  - [Mockito](https://github.com/mockito/mockito) for mocking in tests
- Additional (these are not inherited by default and are required only if using specific classes):
  - [ASM](https://gitlab.ow2.org/asm/asm) for runtime class generation (if using classes annotated with `@UsesBytecodeModification(CommonBytecodeLibrary.ASM)`)
  - [Javassist](https://github.com/jboss-javassist/javassist) for runtime class generation (if using classes annotated with `@UsesBytecodeModification(CommonBytecodeLibrary.JAVASSIST)`)
- Optional (these are not required at all but allow some extra integrations and optimizations):
  - [Caffeine](https://github.com/ben-manes/caffeine) for caching of internal components

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://progrm-jarvis.ru/"><img src="https://avatars.githubusercontent.com/u/7693005?v=4?s=100" width="100px;" alt=""/><br /><sub><b>PROgrm_JARvis</b></sub></a><br /><a href="https://github.com/JarvisCraft/padla/commits?author=JarvisCraft" title="Code">💻</a> <a href="https://github.com/JarvisCraft/padla/commits?author=JarvisCraft" title="Documentation">📖</a> <a href="#ideas-JarvisCraft" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-JarvisCraft" title="Maintenance">🚧</a> <a href="#platform-JarvisCraft" title="Packaging/porting to new platform">📦</a> <a href="#projectManagement-JarvisCraft" title="Project Management">📆</a> <a href="#infra-JarvisCraft" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="https://github.com/JarvisCraft/padla/commits?author=JarvisCraft" title="Tests">⚠️</a> <a href="https://github.com/JarvisCraft/padla/pulls?q=is%3Apr+reviewed-by%3AJarvisCraft" title="Reviewed Pull Requests">👀</a></td>
    <td align="center"><a href="https://github.com/AbstractCoderX"><img src="https://avatars.githubusercontent.com/u/38766980?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Areg Yazychyan</b></sub></a><br /><a href="#ideas-AbstractCoderX" title="Ideas, Planning, & Feedback">🤔</a> <a href="https://github.com/JarvisCraft/padla/pulls?q=is%3Apr+reviewed-by%3AAbstractCoderX" title="Reviewed Pull Requests">👀</a> <a href="#business-AbstractCoderX" title="Business development">💼</a> <a href="https://github.com/JarvisCraft/padla/commits?author=AbstractCoderX" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/xxDark"><img src="https://avatars.githubusercontent.com/u/19853368?v=4?s=100" width="100px;" alt=""/><br /><sub><b>xxDark</b></sub></a><br /><a href="https://github.com/JarvisCraft/padla/commits?author=xxDark" title="Code">💻</a> <a href="#ideas-xxDark" title="Ideas, Planning, & Feedback">🤔</a> <a href="https://github.com/JarvisCraft/padla/issues?q=author%3AxxDark" title="Bug reports">🐛</a></td>
    <td align="center"><a href="http://vk.com/zabelovq"><img src="https://avatars.githubusercontent.com/u/45364639?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Daniil Sudomoin</b></sub></a><br /><a href="https://github.com/JarvisCraft/padla/commits?author=zabelovq" title="Code">💻</a> <a href="#ideas-zabelovq" title="Ideas, Planning, & Feedback">🤔</a></td>
    <td align="center"><a href="https://github.com/CertainLach"><img src="https://avatars.githubusercontent.com/u/6235312?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Yaroslav Bolyukin</b></sub></a><br /><a href="#ideas-CertainLach" title="Ideas, Planning, & Feedback">🤔</a> <a href="#security-CertainLach" title="Security">🛡️</a> <a href="https://github.com/JarvisCraft/padla/commits?author=CertainLach" title="Documentation">📖</a></td>
    <td align="center"><a href="https://github.com/Cheryl-des"><img src="https://avatars.githubusercontent.com/u/68248402?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Cheryl</b></sub></a><br /><a href="#infra-Cheryl-des" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="https://github.com/JarvisCraft/padla/pulls?q=is%3Apr+reviewed-by%3ACheryl-des" title="Reviewed Pull Requests">👀</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
