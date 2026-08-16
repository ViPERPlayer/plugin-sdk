# ViPER Player Plugin SDK

The contract between [ViPER Player](https://github.com/ViPERPlayer/viperplayer) and its plugins.
Plugins are separate APKs the host binds to over AIDL — a plugin can be a music source, an audio DSP
effect, a lyrics provider, a scrobble sink, a metadata enricher, or several at once.

See [`plugin-sdk/README.md`](plugin-sdk/README.md) for the design: the frozen wire ABI, the verb
dispatch, capability negotiation, and the rules that keep an old plugin working on a new host.

## Using it

```kotlin
dependencies {
    implementation("io.github.viperplayer:plugin-sdk:<version>")
}
```

The AAR carries the AIDL-generated `Stub`/`Proxy` classes your plugin implements against, so this is
all you need. The `.aidl` files themselves are not packaged — read them under
[`plugin-sdk/src/main/aidl`](plugin-sdk/src/main/aidl) if you want the wire contract itself.

Worked examples live in their own repositories:
[plugin-example](https://github.com/ViPERPlayer/plugin-example) (a music source) and
[dsp-example](https://github.com/ViPERPlayer/dsp-example) (an audio effect).

## Developing against a local checkout

To work on the SDK and a consumer at the same time, point the consumer at this repository and
Gradle will substitute the published coordinate for the local project:

```properties
# gradle.properties, or -Pviper.pluginSdk.dir=... on the command line
viper.pluginSdk.dir=../plugin-sdk
```

Consumers wire that up in `settings.gradle.kts`:

```kotlin
providers.gradleProperty("viper.pluginSdk.dir").orNull?.let { dir ->
    includeBuild(dir) {
        dependencySubstitution {
            substitute(module("io.github.viperplayer:plugin-sdk")).using(project(":plugin-sdk"))
        }
    }
}
```

Without the property the published artifact is used, which is the default everywhere.

## Building

```bash
./gradlew :plugin-sdk:assembleRelease          # build
./gradlew :plugin-sdk:publishToMavenLocal -PVERSION_NAME=1.0.0   # install locally
```

Releases are cut by pushing a `v*` tag; the workflow publishes the tagged version to Maven Central.
It needs `SIGNING_KEY` (an ASCII-armoured PGP secret key), `SIGNING_PASSWORD`, and the Central
Portal credentials as repository secrets. Builds without those secrets skip signing and still
compile, so forks and pull requests are unaffected.
