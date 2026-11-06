plugins {
    id("java-library")
    id("java-test-fixtures")
    id("jacoco")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
tasks.test {
    useJUnitPlatform()
}
dependencies {
    // api because Flow is part of the public repository interfaces.
    api(libs.kotlinx.coroutines.core)

    // MainDispatcherExtension lives in the test fixtures, so :data and :app reuse it. api so they get what it needs too.
    testFixturesApi(libs.junit.jupiter.api)
    testFixturesApi(libs.kotlinx.coroutines.test)

    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
