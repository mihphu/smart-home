package com.compose.smarthome.presentation.lightscreen

import androidx.compose.ui.graphics.Color

data class LightScreenUiState(
    val isLightOn: Boolean = false,
    val lightColor: Color = Color.Transparent,
    val brightness: Float = 0f,
    val lightColors: List<Color> = listOf()
)