plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.example.androidinterview"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.androidinterview"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_reports")
        metricsDestination = layout.buildDirectory.dir("compose_metrics")
    }
}

dependencies {
    // Modules. :data is only used by AppContainer, the composition root.
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":design-system"))

    // Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Core
    // Not imported directly. Older versions make Android Studio flag @Serializable classes as errors (KTIJ-31549).
    implementation(libs.androidx.annotation.experimental)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Lifecycle + ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.mockwebserver)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Biometric
    implementation(libs.androidx.biometric)
    // On API 26-27 the library's fingerprint dialog needs an AppCompat theme.
    implementation(libs.androidx.appcompat)

    // Testing
    testImplementation(testFixtures(project(":domain")))
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
tasks.register<JacocoReport>("jacocoUnitTestReport") {
    group = "verification"
    description = "Generates the unit test coverage report of :app, :data and :domain, limited to the classes we unit test."
    dependsOn(":app:testDebugUnitTest", ":data:testDebugUnitTest", ":domain:test")

    reports {
        html.required = true
        xml.required = true
    }

    // Only these classes count for coverage; everything else (composables, activities, ui state classes, models...) is left out
    val coveredClasses = listOf(
        "**/screens/**/*ViewModel.class",
        "**/screens/**/*ViewModel\$*.class",
        "**/screens/**/*UiStateKt*.class",
        "**/core/formatting/**",
        "**/data/repository/**",
        "**/data/service/**",
        "**/data/remote/ApiCallKt*.class",
        "**/data/remote/model/*Kt*.class",
        "**/data/remote/model/IsoInstantSerializer*.class",
        "**/domain/usecase/**",
        "**/domain/model/MoneyConverter*.class",
    )

    classDirectories.setFrom(
        files(
            fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")),
            fileTree(rootProject.layout.projectDirectory.dir("data/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")),
            fileTree(rootProject.layout.projectDirectory.dir("domain/build/classes/kotlin/main")),
        ).asFileTree.matching { include(coveredClasses) }
    )
    sourceDirectories.setFrom(files("src/main/java", "../data/src/main/java", "../domain/src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/debugUnitTest/*.exec") },
        fileTree(rootProject.layout.projectDirectory.dir("data/build")) { include("outputs/unit_test_code_coverage/debugUnitTest/*.exec") },
        fileTree(rootProject.layout.projectDirectory.dir("domain/build/jacoco")) { include("*.exec") },
    )
}
