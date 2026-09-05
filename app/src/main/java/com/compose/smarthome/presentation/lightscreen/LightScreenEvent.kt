package com.compose.smarthome.presentation.lightscreen

import androidx.compose.ui.graphics.Color


sealed class LightScreenEvent {
    data class ToggleLightOn(val isOn: Boolean) : LightScreenEvent()
    data class ChangeLightColor(val color: Color) : LightScreenEvent()
    data class ChangeBrightness(val brightness: Float) : LightScreenEvent()
}