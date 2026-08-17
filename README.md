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

## Releasing

```bash
git tag v1.2.3 && git push origin v1.2.3
```

That is the whole release. The workflow derives the version from the tag (`v1.2.3` → `1.2.3`), signs
the artifacts, uploads them, promotes the staging repository into a Portal deployment and lets
Sonatype release it once validation passes. It reaches consumers within ~15–30 minutes.

**A Maven Central release is permanent** — a version can be deprecated but never unpublished or
replaced — so pushing a tag is the point of no return. To inspect the validation result before
anything goes public, change `publishing_type=automatic` to `user_managed` in the workflow and
release by hand from the Portal.

The build deliberately uses only Gradle's own `maven-publish` and `signing` rather than a
third-party publishing plugin. Sonatype ships no official Gradle plugin, so uploads go to their
OSSRH-compatibility endpoint — which parks artifacts in a legacy staging repository that must then
be promoted into a Portal deployment. That promotion is the `curl` step in the workflow; without it
the build reports success and nothing is ever published.

Four repository secrets are required:

| Secret | What it is |
|---|---|
| `SIGNING_KEY` | ASCII-armoured PGP **private** key (`gpg --armor --export-secret-keys <ID>`) |
| `SIGNING_PASSWORD` | that key's passphrase |
| `MAVEN_CENTRAL_USERNAME` | Central Portal **user token** name (not your login) |
| `MAVEN_CENTRAL_PASSWORD` | the matching token password |

The matching **public** key must be on a keyserver (`gpg --keyserver keyserver.ubuntu.com
--send-keys <ID>`) or Central cannot verify the signature and will reject the deployment.

Builds without these secrets skip signing and still compile, so forks and pull requests are
unaffected.
