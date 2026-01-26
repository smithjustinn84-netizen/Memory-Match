package io.github.smithjustinn.di

/**
 * JVM-specific implementation of the dependency graph.
 * Standardizes initialization around Koin's initKoin.
 */
fun createJvmGraph(): AppGraph {
    initKoin(jvmUiModule)
    return KoinAppGraph()
}
