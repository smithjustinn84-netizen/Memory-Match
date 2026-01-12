package io.github.smithjustinn.androidApp

import android.app.Application
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.createAndroidGraph

class MemoryMatchApp : Application() {
    val appGraph: AppGraph by lazy {
        createAndroidGraph(this)
    }
}
