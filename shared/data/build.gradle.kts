plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    // Packs :shared:core composeResources (e.g. shop_items.json) into iOS test binaries
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.metro)
}

val buildAndroid = project.findProperty("target") == "android" || project.findProperty("target") == null
val buildJvm = project.findProperty("target") == "jvm" || project.findProperty("target") == null
val buildIos = project.findProperty("target") == "ios" || project.findProperty("target") == null

kotlin {

    if (buildAndroid) {
        android {
            namespace = "io.github.smithjustinn.data"
            compileSdk =
                libs.versions.android.compileSdk
                    .get()
                    .toInt()
            minSdk =
                libs.versions.android.minSdk
                    .get()
                    .toInt()
            androidResources.enable = false // Optimized for data module
        }
    }

    if (buildJvm) {
        jvm()
    }

    if (buildIos) {
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:core"))
            implementation(libs.bundles.ktor.common)
            api(libs.bundles.room)
            implementation(libs.kermit)
            implementation(libs.compose.runtime)
            implementation(libs.compose.resources)
        }

        if (buildAndroid) {
            androidMain.dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        if (buildJvm) {
            jvmMain.dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        if (buildIos) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        if (buildAndroid) add("kspAndroid", this)
        if (buildJvm) add("kspJvm", this)
        if (buildIos) {
            add("kspIosArm64", this)
            add("kspIosSimulatorArm64", this)
        }
    }
}

compose.resources {
    // This module has no resources of its own; it only reads :shared:core's.
    generateResClass = never
}
