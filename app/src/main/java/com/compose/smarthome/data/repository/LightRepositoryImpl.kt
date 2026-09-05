package com.compose.smarthome.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.compose.smarthome.common.Constants
import com.compose.smarthome.data.local.datastore.LightConfig
import com.compose.smarthome.domain.model.Config
import com.compose.smarthome.domain.repository.LightRepository
import com.compose.smarthome.ui.theme.ColorBrown
import com.compose.smarthome.ui.theme.ColorCyan
import com.compose.smarthome.ui.theme.ColorGreen
import com.compose.smarthome.ui.theme.ColorOrange
import com.compose.smarthome.ui.theme.ColorPurple
import com.compose.smarthome.ui.theme.ColorRed
import kotlinx.coroutines.flow.Flow

class LightRepositoryImpl(
    private val lightConfig: LightConfig
) : LightRepository {
    override val isLightOn: Flow<Boolean>
        get() = lightConfig.isLightOnFlow
    override val lightColor: Flow<Int>
        get() = lightConfig.lightColorFlow
    override val brightness: Flow<Float>
        get() = lightConfig.brightnessFlow

    private var colors: List<Color> = listOf()

    override suspend fun getLightColors(): List<Color> {
        return colors.ifEmpty {
            listOf(
                ColorPurple,
                ColorOrange,
                ColorCyan,
                ColorGreen,
                ColorRed,
                ColorBrown
            )
        }
    }

    override suspend fun applyLightConfig(config: Config) {
        lightConfig.setAll(
            isLightOn = config.isOn,
            colorArgb = config.colorArgb,
            brightness = config.brightness
        )
    }
}
