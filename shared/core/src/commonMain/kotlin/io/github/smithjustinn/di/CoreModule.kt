package io.github.smithjustinn.di

import io.github.smithjustinn.utils.CoroutineDispatchers
import io.github.smithjustinn.utils.LoggingContainer
import org.koin.dsl.module

val coreModule =
    module {
        single { LoggingContainer.getLogger() }
        single { CoroutineDispatchers() }
    }
