import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootEnvSpec

plugins {
    kotlin("multiplatform") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.kotlinmania"
version = "0.1.0"

val androidSdkDir: String? =
    providers.environmentVariable("ANDROID_SDK_ROOT").orNull
        ?: providers.environmentVariable("ANDROID_HOME").orNull

if (androidSdkDir != null && file(androidSdkDir).exists()) {
    val localProperties = rootProject.file("local.properties")
    if (!localProperties.exists()) {
        val sdkDirPropertyValue = file(androidSdkDir).absolutePath.replace("\\", "/")
        localProperties.writeText("sdk.dir=$sdkDirPropertyValue")
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
    }

    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val xcf = XCFramework("Mime")

    macosArm64 {
        binaries.framework {
            baseName = "Mime"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "Mime"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Mime"
            xcf.add(this)
        }
    }
    // Executor-keyed split for the JS target family (workspace template
    // fix #3). Kotlin 2.3.21 does not expose separate `browser`/`nodejs`
    // KotlinJsIrCompilation objects on a single `js` target — both runtimes
    // share one `main` compilation. Splitting into named top-level targets
    // gives each runtime its own Main source set (`jsBrowserMain` /
    // `jsNodeMain`) so Node-only `actual`s never reach the webpack browser
    // bundle.
    js("jsBrowser") {
        browser()
    }
    js("jsNode") {
        nodejs()
    }

    // wasmJs stays unsplit: Kotlin 2.3.21 disallows multiple wasmJs targets
    // ("Declaring multiple Kotlin Targets of the same type is not supported"
    // — only `js` has a temporary backdoor; see
    // https://kotl.in/declaring-multiple-targets). Until KGP supports the
    // split, wasmJsMain stays a single source set across both runtimes.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    // The `jvm()` target exists so CodeQL's Java/Kotlin extractor has a JVM
    // compilation task (`compileKotlinJvm`) to wrap. It is not a shipped
    // artifact — see the `afterEvaluate` block below which removes the `jvm`
    // Maven publication. `jvmMain` shares code with `androidMain` through the
    // `jvmAndAndroidMain` intermediate source set declared further down.
    jvm()

    swiftExport {
        moduleName = "Mime"
        flattenPackage = "io.github.kotlinmania.mime"
    }

    android {
        namespace = "io.github.kotlinmania.mime"
        compileSdk = 34
        minSdk = 24
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
            }
        }

        val commonTest by getting { dependencies { implementation(kotlin("test")) } }

        // Executor-keyed JS source-set hierarchy (workspace template fix #3).
        // `jsBrowserMain` and `jsNodeMain` are the auto-created Main source
        // sets for the `js("jsBrowser")` and `js("jsNode")` targets above.
        //
        //                  commonMain
        //                  /        \
        //               jsMain     wasmJsMain         (target-family axis)
        //               /    \
        //      jsBrowserMain  jsNodeMain              (executor axis, JS only)
        //
        // `jsMain` is a runtime-agnostic intermediate so any future
        // src/jsMain/kotlin/ code keeps feeding both jsBrowser and jsNode.
        // `wasmJsMain` remains a single source set across both wasmJs
        // runtimes because KGP rejects multiple wasmJs targets. Any future
        // Node-only wasmJs `actual` must rely on the `typeof process`
        // runtime guard (workspace CLAUDE.md fix #4).
        //
        // `browserMain` / `nodeMain` per-executor intermediates are not
        // wired here: with wasmJs unsplit they would have only one member
        // each on the JS side, making them degenerate. Reintroduce them
        // once Kotlin Multiplatform lifts the wasmJs single-target rule.
        val jsMain by creating { dependsOn(commonMain) }
        named("jsBrowserMain") { dependsOn(jsMain) }
        named("jsNodeMain") { dependsOn(jsMain) }

        // Shared intermediate source set between `jvmMain` (CodeQL-only) and
        // `androidMain`. Android already inherits from `commonMain` via the
        // default hierarchy template, so any code that genuinely needs to be
        // shared with the JVM target goes here. Today this is empty — both
        // platforms see only `commonMain` — and the intermediate exists so
        // that any future JVM-shaped `actual` automatically appears on
        // Android too.
        val jvmAndAndroidMain by creating { dependsOn(commonMain) }
        named("androidMain") { dependsOn(jvmAndAndroidMain) }
        named("jvmMain") { dependsOn(jvmAndAndroidMain) }
    }
    jvmToolchain(21)
}

// The `jvm()` target exists only so CodeQL's Java/Kotlin extractor has a
// `compileKotlinJvm` task to wrap. Disable every `publishJvm*` task so no
// `-jvm` artifact ships alongside the real KMP coordinates.
afterEvaluate {
    tasks.matching { it.name.startsWith("publishJvm") }.configureEach {
        enabled = false
    }
}

rootProject.extensions.configure<NodeJsEnvSpec>("kotlinNodeJsSpec") {
    version.set("22.22.2")
}

rootProject.extensions.configure<WasmNodeJsEnvSpec>("kotlinWasmNodeJsSpec") {
    version.set("22.22.2")
}

rootProject.extensions.configure<YarnRootEnvSpec>("kotlinYarnSpec") {
    version.set("1.22.22")
}

rootProject.extensions.configure<WasmYarnRootEnvSpec>("kotlinWasmYarnSpec") {
    version.set("1.22.22")
}

rootProject.extensions.configure<YarnRootExtension>("kotlinYarn") {
    resolution("diff", "8.0.3")
    resolution("**/diff", "8.0.3")
    resolution("serialize-javascript", "7.0.5")
    resolution("**/serialize-javascript", "7.0.5")
    resolution("webpack", "5.106.2")
    resolution("**/webpack", "5.106.2")
    resolution("follow-redirects", "1.16.0")
    resolution("**/follow-redirects", "1.16.0")
    resolution("lodash", "4.18.1")
    resolution("**/lodash", "4.18.1")
    resolution("ajv", "8.20.0")
    resolution("**/ajv", "8.20.0")
    resolution("brace-expansion", "5.0.5")
    resolution("**/brace-expansion", "5.0.5")
    resolution("flatted", "3.4.2")
    resolution("**/flatted", "3.4.2")
    resolution("minimatch", "10.2.5")
    resolution("**/minimatch", "10.2.5")
    resolution("picomatch", "4.0.4")
    resolution("**/picomatch", "4.0.4")
    resolution("qs", "6.15.1")
    resolution("**/qs", "6.15.1")
    resolution("socket.io-parser", "4.2.6")
    resolution("**/socket.io-parser", "4.2.6")
}


val patchedKarmaWebpackPackage = rootProject.layout.projectDirectory.dir("gradle/npm/karma-webpack").asFile.absolutePath.replace("\\", "/")

rootProject.extensions.configure<NodeJsRootExtension>("kotlinNodeJs") {
    versions.webpack.version = "5.106.2"
    versions.webpackCli.version = "7.0.2"
    versions.karma.version = "npm:karma-maintained@6.4.7"
    versions.karmaWebpack.version = "file:$patchedKarmaWebpackPackage"
    versions.mocha.version = "12.0.0-beta-10"
    versions.kotlinWebHelpers.version = "3.1.0"
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "mime-kotlin", version.toString())

    pom {
        name.set("mime-kotlin")
        description.set("Kotlin Multiplatform port of hyperium/mime - Strongly typed MIME / Media Types")
        inceptionYear.set("2026")
        url.set("https://github.com/KotlinMania/mime-kotlin")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
                organization.set("The Solace Project")
                organizationUrl.set("https://github.com/KotlinMania")
                roles.set(listOf("maintainer", "kotlin-port-author"))
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/mime-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/mime-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/mime-kotlin.git")
        }
    }
}

tasks.register("test") {
    group = "verification"
    description =
        "Runs the host-portable test suite (macOS + JS + WasmJS + Android unit). " +
        "Non-host native targets (mingwX64, linuxX64) only run on their own host."

    val defaultTestTasks = listOf(
        "macosArm64Test",
        "jsNodeTest",
        "wasmJsNodeTest",
        "compileAndroidMain",
        "assembleUnitTest",
    )

    dependsOn(defaultTestTasks.mapNotNull { taskName -> tasks.findByName(taskName) })
}
