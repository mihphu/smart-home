package com.compose.smarthome.presentation.lightscreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.smarthome.common.Constants
import com.compose.smarthome.domain.model.Config
import com.compose.smarthome.domain.repository.LightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LightScreenViewModel(
    private val repo: LightRepository,
    private val externalScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(LightScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            val isLightOn = repo.isLightOn.first()
            val lightColorArgb = repo.lightColor.first()
            val brightness = repo.brightness.first()
            val lightColors = repo.getLightColors()

            _uiState.update {
                it.copy(
                    isLightOn = isLightOn,
                    lightColor = Color(lightColorArgb),
                    brightness = brightness,
                    lightColors = lightColors
                )
            }
        }
    }

    fun onEvent(event: LightScreenEvent) {
        when (event) {
            is LightScreenEvent.ToggleLightOn -> {
                _uiState.update { it.copy(isLightOn = event.isOn) }
            }

            is LightScreenEvent.ChangeLightColor -> {
                _uiState.update { it.copy(lightColor = event.color) }
            }

            is LightScreenEvent.ChangeBrightness -> {
                _uiState.update { it.copy(brightness = event.brightness) }
            }
        }

        scheduleSave()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = externalScope.launch {
            delay(Constants.SAVE_DEBOUNCE_MS.milliseconds)
            val state = _uiState.value
            repo.applyLightConfig(
                Config(
                    isOn = state.isLightOn,
                    colorArgb = state.lightColor.toArgb(),
                    brightness = state.brightness
                )
            )
        }
    }
}
