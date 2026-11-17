package io.github.smithjustinn.androidApp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.createAndroidGraph
import kotlinx.coroutines.launch

class MemoryMatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The Mobile Ads SDK does disk and network work while initialising; on the main thread it
        // delays the first frame and can trigger a startup ANR on slow devices.
        appGraph.applicationScope.launch(appGraph.coroutineDispatchers.io) {
            MobileAds.initialize(this@MemoryMatchApp)
        }
        appGraph.logger.i { "Logging initialized via Metro with Kermit" }
    }

    val appGraph: AppGraph by lazy {
        createAndroidGraph(this)
    }
}
