# hidemymods

Privacy-respecting mod-list cloaker for Forge / NeoForge clients.

## What it is

A Minecraft mod that prevents your client from telling the server which
other mods are installed. The mod-list is still built locally so every
mod in your `mods/` directory loads and functions normally; only the
copy of the list that goes onto the wire during the network handshake
is suppressed.

The intended use case is connecting modded Forge / NeoForge clients to
servers that compare the reported mod-list against an internal allow-
list and disconnect on extras, while you want to run additional
client-only mods (performance, accessibility, cosmetic) the server has
no legitimate interest in.

## What it is not

- Not a cheat or anti-cheat
- Not a surveillance tool
- Not a way to bypass server-side gameplay rules
- Not affiliated with any specific server or launcher

The mod does not collect, transmit, or persist any data. Its entire
effect is to omit information the client would otherwise volunteer
during the FML / NeoForge network handshake.

## Supported versions

| Minecraft | Loader   | Build tool                | Gradle  | Build JVM | Status         |
|-----------|----------|---------------------------|---------|-----------|----------------|
| 1.7.10    | Forge    | RetroFuturaGradle 1.4.4   | 8.14.4  | Java 21+  | scaffold only  |
| 1.12.2    | Forge    | ForgeGradle 2.3-SNAPSHOT  | 4.10.3  | Java 8    | initial impl   |
| 1.21.1    | NeoForge | ModDevGradle 2.0          | 8.14.4  | Java 21+  | scaffold only  |

Each loader subproject is its own standalone Gradle build with a
pinned wrapper. They are not unified under a root Gradle build because
no Gradle version supports all three loader toolchains simultaneously.

Shared constants live in `_common-src/` as plain Java sources and are
included into the forge-1.7.10 and forge-1.12.2 subprojects via
`sourceSets.main.java.srcDirs`. There is no artifact coordination
between subprojects.

## Installation

Drop the appropriate jar into your `mods/` directory. The file must
match the Minecraft version of your installation. Once a release is
cut, jars will be on the Releases page.

## Building

Each subproject builds independently. Build JVM must match the
subproject's required Java version (see table above).

### forge-1.12.2

Requires Java 8 as build JVM and Gradle 4.10.3 (pinned by wrapper).

```
cd forge-1.12.2
JAVA_HOME=/path/to/java-8 ./gradlew build
```

Built jar lands in `forge-1.12.2/build/libs/`.

If the wrapper is missing (fresh clone with no `gradlew` script),
bootstrap it with a system Gradle 4.10.3 installation:

```
cd forge-1.12.2
JAVA_HOME=/path/to/java-8 gradle wrapper --gradle-version 4.10.3
```

### forge-1.7.10

Requires Java 21+ as build JVM and Gradle 8.14.4 (pinned by wrapper).

```
cd forge-1.7.10
./gradlew build
```

### neoforge-1.21.1

Requires Java 21+ as build JVM and Gradle 8.14.4 (pinned by wrapper).

```
cd neoforge-1.21.1
./gradlew build
```

## Design

See `docs/design.md` for the hook strategy and the reasoning behind
the initial drop-everything implementation.

## License

Apache License 2.0. See `LICENSE`.
