package com.compose.smarthome.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object LightConfigKeys {
    val KEY_IS_LIGHT_ON = booleanPreferencesKey("is_light_on")
    val KEY_LIGHT_COLOR = intPreferencesKey("light_color")
    val KEY_BRIGHTNESS = floatPreferencesKey("brightness")
}
