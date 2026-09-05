package com.compose.smarthome.presentation.lightscreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppSeekbar(
    modifier: Modifier = Modifier,
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    stepCount: Int = 10,
    trackColor: Color = Color.Black.copy(alpha = 0.15f),
    progressColor: Color = Color.Black,
    tickColor: Color = Color.White,
    cornerRadius: Dp = 10.dp,
    tickLength: Dp = 10.dp,
    tickWidth: Dp = 2.dp
) {
    val segments = (stepCount - 1).coerceAtLeast(1)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)

    val animatedProgress = remember { Animatable(progress.coerceIn(0f, 1f)) }
    val emittedProgress = remember { mutableFloatStateOf(progress.coerceIn(0f, 1f)) }
    val lastTickIndex = remember { mutableIntStateOf(-1) }

    LaunchedEffect(progress) {
        val target = progress.coerceIn(0f, 1f)
        if (target != emittedProgress.floatValue) {
            emittedProgress.floatValue = target
            animatedProgress.animateTo(target, tween(durationMillis = 200))
        }
    }

    val commit: (Float, Boolean) -> Unit = { rawFraction, animate ->
        val target = rawFraction.coerceIn(0f, 1f)

        val tickIndex = (target * segments).roundToInt()
        if (tickIndex != lastTickIndex.intValue) {
            lastTickIndex.intValue = tickIndex
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }

        if (target != emittedProgress.floatValue) {
            emittedProgress.floatValue = target
            currentOnProgressChanged(target)
        }

        scope.launch {
            if (animate) {
                animatedProgress.animateTo(target, tween(durationMillis = 180))
            } else {
                animatedProgress.snapTo(target)
            }
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
                setProgress { target ->
                    commit(target, true)
                    true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    commit(offset.x / size.width, true)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset -> commit(offset.x / size.width, false) },
                    onDragCancel = { lastTickIndex.intValue = -1 },
                    onDragEnd = { lastTickIndex.intValue = -1 }
                ) { change, _ ->
                    change.consume()
                    commit(change.position.x / size.width, false)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val tickLengthPx = tickLength.toPx()
        val tickTop = (height - tickLengthPx) / 2f
        val tickBottom = (height + tickLengthPx) / 2f

        drawRect(color = trackColor)
        drawRect(
            color = progressColor,
            size = Size(animatedProgress.value * width, height)
        )

        for (index in 1 until segments) {
            val x = width * index / segments
            drawLine(
                color = tickColor,
                start = Offset(x, tickTop),
                end = Offset(x, tickBottom),
                strokeWidth = tickWidth.toPx()
            )
        }
    }
}
