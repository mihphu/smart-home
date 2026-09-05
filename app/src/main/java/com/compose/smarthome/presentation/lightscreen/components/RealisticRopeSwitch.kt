package com.compose.smarthome.presentation.lightscreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * RealisticRopeSwitch
 *
 * A modern, physical rope pull-switch for smart home light control.
 *
 * @param isOn Current light switch state.
 * @param onCheckedChange Callback invoked when the switch is pulled and toggled.
 * @param modifier Modifier applied to the outer container.
 * @param ropeColor Base color of the cord.
 * @param handleColor Base color of the circular bead handle.
 * @param ringColor Color of the stadium capsule outline ring.
 * @param ropeLength Resting length of the cord from ceiling to handle.
 * @param ropeWidth Thickness of the cord line.
 * @param ringWidth Width of the stadium capsule outline ring.
 * @param ringHeight Height of the stadium capsule outline ring.
 * @param pullThreshold Minimum pull distance in Dp required to toggle state.
 * @param maxPullDistance Maximum stretching distance in Dp.
 * @param enabled Whether interaction is enabled.
 */
@Composable
fun RealisticRopeSwitch(
    modifier: Modifier = Modifier,
    isOn: Boolean,
    ropeColor: Color = Color.Black,
    handleColor: Color = Color.Black,
    ringColor: Color = Color.Gray,
    ropeLength: Dp = 210.dp,
    ropeWidth: Dp = 4.dp,
    ringWidth: Dp = 48.dp,
    ringHeight: Dp = 64.dp,
    pullThreshold: Dp = 50.dp,
    maxPullDistance: Dp = 100.dp,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val currentIsOn by rememberUpdatedState(isOn)
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)

    val ropeLengthPx = with(density) { ropeLength.toPx() }
    val ropeWidthPx = with(density) { ropeWidth.toPx() }
    val pullThresholdPx = with(density) { pullThreshold.toPx() }
    val maxPullDistancePx = with(density) { maxPullDistance.toPx() }
    val pillWidthPx = with(density) { ringWidth.toPx() }
    val pillHeightPx = with(density) { ringHeight.toPx() }
    val pillRadiusPx = pillWidthPx / 2f
    val ringStrokeWidthPx = with(density) { 2.5.dp.toPx() }
    val beadRadiusPx = with(density) { 11.5.dp.toPx() }
    val topFadePx = with(density) { 40.dp.toPx() }
    val maxSwayPx = with(density) { 35.dp.toPx() }

    val pullOffsetY = remember { Animatable(0f) }
    val swayOffsetX = remember { Animatable(0f) }
    val recoilWave = remember { Animatable(0f) }

    var hasPassedThreshold by remember { mutableStateOf(false) }

    val containerWidth = ringWidth + 36.dp
    val containerHeight = ropeLength + maxPullDistance + 70.dp

    Box(
        modifier = modifier
            .width(containerWidth)
            .height(containerHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier
                .width(containerWidth)
                .height(containerHeight)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = {
                            hasPassedThreshold = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val currentY = pullOffsetY.value
                                val resistance =
                                    1f / (1f + (currentY / (ropeLengthPx * 0.65f)).coerceAtLeast(0f))
                                val newY = (currentY + dragAmount.y * resistance).coerceIn(
                                    -5f,
                                    maxPullDistancePx
                                )
                                pullOffsetY.snapTo(newY)

                                val currentX = swayOffsetX.value
                                val newX = (currentX + dragAmount.x * 0.35f).coerceIn(
                                    -maxSwayPx,
                                    maxSwayPx
                                )
                                swayOffsetX.snapTo(newX)

                                if (newY >= pullThresholdPx && !hasPassedThreshold) {
                                    hasPassedThreshold = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else if (newY < pullThresholdPx && hasPassedThreshold) {
                                    hasPassedThreshold = false
                                }
                            }
                        },
                        onDragEnd = {
                            val triggered = hasPassedThreshold
                            if (triggered) {
                                currentOnCheckedChange(!currentIsOn)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            hasPassedThreshold = false

                            scope.launch {
                                val pullMagnitude =
                                    (pullOffsetY.value / pullThresholdPx).coerceIn(0.3f, 2f)
                                val initialSnapVelocity = -1200f * pullMagnitude

                                pullOffsetY.animateTo(
                                    targetValue = 0f,
                                    initialVelocity = initialSnapVelocity,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }

                            if (triggered) {
                                scope.launch {
                                    recoilWave.snapTo(8f)
                                    recoilWave.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.32f,
                                            stiffness = 380f
                                        )
                                    )
                                }
                            }

                            scope.launch {
                                swayOffsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            hasPassedThreshold = false
                            scope.launch {
                                pullOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            scope.launch {
                                swayOffsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    )
                }
        ) {
            val anchorX = size.width / 2f
            val anchorY = 0f

            val currentHandleX = anchorX + swayOffsetX.value
            val currentHandleY = ropeLengthPx + pullOffsetY.value

            val cordAttachY = currentHandleY - beadRadiusPx * 0.75f
            val cordPath = Path().apply {
                moveTo(anchorX, anchorY)
                if (abs(swayOffsetX.value) > 0.5f || abs(recoilWave.value) > 0.1f) {
                    val controlX = (anchorX + currentHandleX) / 2f + recoilWave.value
                    val controlY = cordAttachY * 0.52f
                    quadraticTo(controlX, controlY, currentHandleX, cordAttachY)
                } else {
                    lineTo(currentHandleX, cordAttachY)
                }
            }

            val fadeRatio =
                (topFadePx / currentHandleY.coerceAtLeast(topFadePx)).coerceIn(0.06f, 0.45f)
            val ropeBrush = Brush.verticalGradient(
                0.0f to ropeColor.copy(alpha = 0.0f),
                fadeRatio * 0.5f to ropeColor.copy(alpha = 0.55f),
                fadeRatio to ropeColor,
                1.0f to ropeColor
            )

            drawPath(
                path = cordPath,
                brush = ropeBrush,
                style = Stroke(
                    width = ropeWidthPx,
                    cap = StrokeCap.Round
                )
            )

            val highlightBrush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                fadeRatio to Color.White.copy(alpha = 0.22f),
                1.0f to Color.White.copy(alpha = 0.15f)
            )
            drawPath(
                path = cordPath,
                brush = highlightBrush,
                style = Stroke(
                    width = (ropeWidthPx * 0.32f).coerceAtLeast(1f),
                    cap = StrokeCap.Round
                )
            )

            val pillTop = currentHandleY - pillHeightPx * 0.46f
            val pillLeft = currentHandleX - pillWidthPx / 2f

            drawRoundRect(
                color = ringColor,
                topLeft = Offset(pillLeft, pillTop),
                size = Size(pillWidthPx, pillHeightPx),
                cornerRadius = CornerRadius(pillRadiusPx, pillRadiusPx),
                style = Stroke(width = ringStrokeWidthPx)
            )

            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                center = Offset(currentHandleX, currentHandleY + with(density) { 2.5.dp.toPx() }),
                radius = beadRadiusPx
            )

            if (isOn) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FFB74D),
                            Color(0x22FF9800),
                            Color.Transparent
                        ),
                        center = Offset(currentHandleX, currentHandleY),
                        radius = beadRadiusPx * 2.2f
                    ),
                    center = Offset(currentHandleX, currentHandleY),
                    radius = beadRadiusPx * 2.2f
                )
            }

            val beadBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.26f).compositeOver(handleColor),
                    handleColor,
                    Color.Black.copy(alpha = 0.40f).compositeOver(handleColor)
                ),
                center = Offset(
                    currentHandleX - beadRadiusPx * 0.32f,
                    currentHandleY - beadRadiusPx * 0.32f
                ),
                radius = beadRadiusPx * 1.18f
            )

            drawCircle(
                brush = beadBrush,
                center = Offset(currentHandleX, currentHandleY),
                radius = beadRadiusPx
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.36f),
                center = Offset(
                    currentHandleX - beadRadiusPx * 0.34f,
                    currentHandleY - beadRadiusPx * 0.34f
                ),
                radius = beadRadiusPx * 0.22f
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F6F0)
@Composable
fun RealisticRopeSwitchPreviewLight() {
    var state by remember { mutableStateOf(true) }
    RealisticRopeSwitch(
        isOn = state,
        onCheckedChange = { state = it }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E22)
@Composable
fun RealisticRopeSwitchPreviewDark() {
    var state by remember { mutableStateOf(false) }
    RealisticRopeSwitch(
        isOn = state,
        onCheckedChange = { state = it }
    )
}