package io.github.smithjustinn.services

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A provider for the current activity that can be updated from the activity lifecycle.
 */
class ActivityProvider {
    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity.asStateFlow()

    fun updateActivity(activity: Activity?) {
        _activity.value = activity
    }
}
