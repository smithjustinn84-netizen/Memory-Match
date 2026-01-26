package io.github.smithjustinn.di

/**
 * iOS-specific implementation of the dependency graph.
 * Standardizes initialization around Koin's initKoin.
 */
fun createIosGraph(): AppGraph {
    initKoin(iosUiModule)
    return KoinAppGraph()
}
