package com.apptimemachine.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * Simple key-value settings, distinct from the historical Room tables.
 * [isMonitoringActive] backs the Dashboard's "Monitoring Status" card and
 * is the single source of truth for whether background workers should be
 * running at all — flipped true only once Permission Setup completes.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val USAGE_ACCESS_GRANTED = booleanPreferencesKey("usage_access_granted")
        val MONITORING_ACTIVE = booleanPreferencesKey("monitoring_active")
        val MONITORING_STARTED_AT = longPreferencesKey("monitoring_started_at_device")
    }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { it[Keys.DARK_MODE] }
    val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }
    val isUsageAccessGranted: Flow<Boolean> = context.dataStore.data.map { it[Keys.USAGE_ACCESS_GRANTED] ?: false }
    val isMonitoringActive: Flow<Boolean> = context.dataStore.data.map { it[Keys.MONITORING_ACTIVE] ?: false }
    val deviceMonitoringStartedAt: Flow<Long?> = context.dataStore.data.map { it[Keys.MONITORING_STARTED_AT] }

    suspend fun setDarkMode(enabled: Boolean?) = context.dataStore.edit {
        if (enabled == null) it.remove(Keys.DARK_MODE) else it[Keys.DARK_MODE] = enabled
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) = context.dataStore.edit {
        it[Keys.DYNAMIC_COLOR] = enabled
    }

    suspend fun setOnboardingComplete(complete: Boolean) = context.dataStore.edit {
        it[Keys.ONBOARDING_COMPLETE] = complete
    }

    suspend fun setUsageAccessGranted(granted: Boolean) = context.dataStore.edit {
        it[Keys.USAGE_ACCESS_GRANTED] = granted
    }

    /** Call once, when Permission Setup completes and the first scan is scheduled. */
    suspend fun startMonitoring(timestamp: Long) = context.dataStore.edit {
        it[Keys.MONITORING_ACTIVE] = true
        if (it[Keys.MONITORING_STARTED_AT] == null) {
            it[Keys.MONITORING_STARTED_AT] = timestamp
        }
    }
}
