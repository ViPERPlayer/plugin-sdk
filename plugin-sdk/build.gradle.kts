import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
    signing
}

// Coordinates for the published artifact. Consumers depend on
// "io.github.viperplayer:plugin-sdk:<version>"; the host app and the first-party plugins can
// substitute a local checkout instead — see the composite-build note in the README.
group = "io.github.viperplayer"
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0-SNAPSHOT")

android {
    namespace = "com.viperplayer.plugin"
    compileSdk = 37

    // Publish the release variant, with sources and javadoc jars alongside it (both are required
    // by Maven Central, and the sources jar is what makes the AIDL + KDoc readable to plugin
    // authors in their IDE).
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    // The frozen IPC transport lives in AIDL. Everything else is plain Kotlin.
    //
    // Note for consumers: the published AAR carries the AIDL-generated Stub/Proxy classes inside
    // classes.jar, which is what a plugin implements against — no first-party plugin declares any
    // .aidl of its own. The .aidl sources themselves are not packaged; read them in the repository
    // if you need the wire contract itself.
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Exposed to module authors: provider methods are suspend and authors commonly use
    // coroutines directly, so surface it (and the codec) on the SDK's API classpath.
    api(libs.kotlinx.coroutines.android)

    // Wire payload codec. NOT part of the binder ABI — it is carried inside Bundles as bytes,
    // so it can evolve independently of the frozen transport.
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    repositories {
        maven {
            name = "mavenCentral"
            // Central Portal's OSSRH-compatible staging endpoint, which is the one that accepts a
            // plain maven-publish deployment. Credentials come from
            // ORG_GRADLE_PROJECT_mavenCentralUsername / ...Password (set by the release workflow).
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = providers.gradleProperty("mavenCentralUsername").orNull
                password = providers.gradleProperty("mavenCentralPassword").orNull
            }
        }
    }

    publications {
        register<MavenPublication>("release") {
            afterEvaluate { from(components["release"]) }
            artifactId = "plugin-sdk"

            pom {
                name = "ViPER Player Plugin SDK"
                description = "Build music-source plugins for ViPER Player: the AIDL transport, " +
                        "the catalog models and the provider APIs a plugin implements."
                url = "https://github.com/ViPERPlayer/plugin-sdk"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "iscle"
                        name = "Iscle"
                        url = "https://github.com/iscle"
                    }
                }
                scm {
                    url = "https://github.com/ViPERPlayer/plugin-sdk"
                    connection = "scm:git:https://github.com/ViPERPlayer/plugin-sdk.git"
                    developerConnection = "scm:git:ssh://git@github.com/ViPERPlayer/plugin-sdk.git"
                }
            }
        }
    }
}

signing {
    // Maven Central requires signatures; local and CI-without-secrets builds must still work, so
    // only sign when a key is actually supplied (in-memory, so CI never writes a keyring to disk).
    val signingKey = providers.environmentVariable("SIGNING_KEY").orNull
    val signingPassword = providers.environmentVariable("SIGNING_PASSWORD").orNull
    isRequired = signingKey != null
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
