package com.compose.smarthome.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.compose.smarthome.common.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val LIGHT_CONFIG_DATASTORE_NAME = "light_config"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LIGHT_CONFIG_DATASTORE_NAME
)

class LightConfigImpl(private val context: Context) : LightConfig {
    override val isLightOnFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LightConfigKeys.KEY_IS_LIGHT_ON] ?: Constants.DEFAULT_IS_LIGHT_ON
        }
        .distinctUntilChanged()

    override val lightColorFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LightConfigKeys.KEY_LIGHT_COLOR] ?: Constants.DEFAULT_LIGHT_COLOR
        }
        .distinctUntilChanged()

    override val brightnessFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LightConfigKeys.KEY_BRIGHTNESS] ?: Constants.DEFAULT_BRIGHTNESS
        }
        .distinctUntilChanged()

    override suspend fun setAll(isLightOn: Boolean, colorArgb: Int, brightness: Float) {
        context.dataStore.edit { preferences ->
            preferences[LightConfigKeys.KEY_IS_LIGHT_ON] = isLightOn
            preferences[LightConfigKeys.KEY_LIGHT_COLOR] = colorArgb
            preferences[LightConfigKeys.KEY_BRIGHTNESS] = brightness
        }
    }
}
