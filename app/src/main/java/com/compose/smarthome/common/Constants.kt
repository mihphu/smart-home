package com.compose.smarthome.common

import androidx.compose.ui.graphics.toArgb
import com.compose.smarthome.ui.theme.ColorOrange

object Constants {
    const val DEFAULT_IS_LIGHT_ON = true
    const val DEFAULT_BRIGHTNESS = 0.5f
    val DEFAULT_LIGHT_COLOR = ColorOrange.toArgb()
    const val SAVE_DEBOUNCE_MS = 300L
}
