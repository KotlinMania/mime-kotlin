# mime-kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fmime--kotlin-blue.svg)](https://github.com/KotlinMania/mime-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/mime-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/mime-kotlin)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/mime-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/mime-kotlin/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)

Kotlin Multiplatform line-by-line clean-room port of the Rust crate [`mime`](https://crates.io/crates/mime) — strongly typed MIME / Media Types.

This port targets behavioral parity with the upstream Rust crate while presenting an idiomatic Kotlin Multiplatform API. Every Kotlin file is a faithful translation of an upstream Rust file and carries a `// port-lint: source <path>` header so the AST-distance tool can track provenance.

## Supported targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

## Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:mime-kotlin:0.1.0")
}
```

## Build

```bash
./gradlew build
./gradlew test
```

## Porting guidelines

See [CLAUDE.md](CLAUDE.md) and [AGENTS.md](AGENTS.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

## Maintainer

**Sydney Renee** ([@sydneyrenee](https://github.com/sydneyrenee)) — *The Solace Project* — <sydney@solace.ofharmony.ai>

## Credits

This Kotlin port stands entirely on the shoulders of the upstream Rust [`mime`](https://github.com/hyperium/mime) crate. Sincere thanks to:

- **Sean McArthur** ([@seanmonstar](https://github.com/seanmonstar)) and the [hyperium](https://github.com/hyperium) contributors — original authors of the [`mime`](https://github.com/hyperium/mime) crate. The crate's design, parser, constants, and test suite are theirs; this repository merely translates that work into Kotlin Multiplatform.

If you find this port useful, please also consider starring the upstream project — it is the source of all the real engineering credit here.

## License

Licensed under the **Apache License, Version 2.0** — see [LICENSE](LICENSE).

The upstream Rust [`mime`](https://github.com/hyperium/mime) crate is dual-licensed `MIT OR Apache-2.0`; this Kotlin port chooses Apache-2.0. Original copyright belongs to Sean McArthur and the hyperium contributors. Kotlin port copyright © 2026 Sydney Renee and The Solace Project.
