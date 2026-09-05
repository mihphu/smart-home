package com.compose.smarthome.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface LightConfig {
    val isLightOnFlow: Flow<Boolean>
    val lightColorFlow: Flow<Int>
    val brightnessFlow: Flow<Float>

    suspend fun setAll(isLightOn: Boolean, colorArgb: Int, brightness: Float)
}
