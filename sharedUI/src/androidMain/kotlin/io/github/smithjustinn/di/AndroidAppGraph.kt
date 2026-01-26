package io.github.smithjustinn.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Android-specific implementation of the dependency graph.
 * Refactored to use Koin for initialization and dependency management.
 */
fun createAndroidGraph(application: Application): AppGraph {
    initKoin(androidUiModule) {
        androidLogger()
        androidContext(application)
    }
    return KoinAppGraph()
}
