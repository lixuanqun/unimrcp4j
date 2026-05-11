# AGENTS.md

## Cursor Cloud specific instructions

### Repository overview

`unimrcp4j` is a Java wrapper/binding for [UniMRCP](https://www.unimrcp.org/) (Media Resource Control Protocol). As of the initial commit, the repository contains only a `.gitignore` and `LICENSE` (Apache 2.0) — no source code, build configuration, or dependencies exist yet.

### Development environment

- **JDK**: OpenJDK 21 (system-installed at `/usr/lib/jvm/java-21-openjdk-amd64`)
- **Build tool**: Apache Maven 3.8.7 (installed via `apt`)
- **No build files** (`pom.xml`, `build.gradle`) exist yet. When they are added, run `mvn install` or `mvn compile` to build.

### Running the project

Since no source code exists yet, there are no services, tests, or lint checks to run. When a `pom.xml` is added:

- **Build**: `mvn compile`
- **Test**: `mvn test`
- **Package**: `mvn package`

### Gotchas

- Maven's default `maven-archetype-quickstart` targets Java 7, which is unsupported by JDK 21. If using that archetype, set `maven.compiler.source` and `maven.compiler.target` to at least `8` (recommend `17` or `21`).
