package com.compose.smarthome.domain.repository

import androidx.compose.ui.graphics.Color
import com.compose.smarthome.domain.model.Config
import kotlinx.coroutines.flow.Flow

interface LightRepository {
    val isLightOn: Flow<Boolean>
    val lightColor: Flow<Int>
    val brightness: Flow<Float>

    suspend fun getLightColors() : List<Color>
    suspend fun applyLightConfig(config: Config)
}
