package io.github.smithjustinn.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter

object LoggingContainer {
    fun getLogger(): Logger = Logger(loggerConfigInit(platformLogWriter(NoTagFormatter)))
}
