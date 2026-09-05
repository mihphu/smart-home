package com.compose.smarthome.common.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

fun Color.toHex(): String =
    String.format(Locale.US, "#%06X", 0xFFFFFF and toArgb())