package com.compose.smarthome.presentation.lightscreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun LightBeam(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    lightOpacity: Float = 0.5f,
    lightColor: () -> Color = { Color.White },
    topConeWidth: Float = 210f,
    bottomConeWidth: Float = 600f,
    coneHeight: Float = 500f
) {

    Canvas(
        modifier = modifier
    ) {
        if (isVisible) {
            val color = lightColor()

            val intensity = lightOpacity * color.alpha
            val hot = lerp(color.copy(alpha = 1f), Color.White, 0.45f)

            val canvasWidth = size.width
            val centerX = canvasWidth / 2
            val topY = 0f
            val bottomY = coneHeight

            val hazeExpand = 0.35f
            val hazeTopHalf = topConeWidth / 2 * (1f + hazeExpand)
            val hazeBottomHalf = bottomConeWidth / 2 * (1f + hazeExpand)

            val hazePath = Path().apply {
                moveTo(centerX - hazeTopHalf, topY)
                cubicTo(
                    centerX - hazeTopHalf, bottomY * 0.3f,
                    centerX - hazeBottomHalf * 0.85f, bottomY * 0.55f,
                    centerX - hazeBottomHalf, bottomY
                )
                lineTo(centerX + hazeBottomHalf, bottomY)
                cubicTo(
                    centerX + hazeBottomHalf * 0.85f, bottomY * 0.55f,
                    centerX + hazeTopHalf, bottomY * 0.3f,
                    centerX + hazeTopHalf, topY
                )
                close()
            }

            drawPath(
                path = hazePath,
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to color.copy(alpha = intensity * 0.35f),
                        0.15f to color.copy(alpha = intensity * 0.22f),
                        0.5f to color.copy(alpha = intensity * 0.10f),
                        1.0f to Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )
            )

            val outerPath = Path().apply {
                val topHalf = topConeWidth / 2
                val bottomHalf = bottomConeWidth / 2

                moveTo(centerX - topHalf, topY)
                cubicTo(
                    centerX - topHalf, bottomY * 0.25f,
                    centerX - bottomHalf * 0.78f, bottomY * 0.5f,
                    centerX - bottomHalf, bottomY
                )
                lineTo(centerX + bottomHalf, bottomY)
                cubicTo(
                    centerX + bottomHalf * 0.78f, bottomY * 0.5f,
                    centerX + topHalf, bottomY * 0.25f,
                    centerX + topHalf, topY
                )
                close()
            }

            drawPath(
                path = outerPath,
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to color.copy(alpha = intensity * 0.85f),
                        0.08f to color.copy(alpha = intensity * 0.70f),
                        0.25f to color.copy(alpha = intensity * 0.50f),
                        0.55f to color.copy(alpha = intensity * 0.25f),
                        0.85f to color.copy(alpha = intensity * 0.08f),
                        1.0f to Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )
            )

            val coreNarrow = 0.55f
            val coreTopHalf = topConeWidth / 2 * coreNarrow
            val coreBottomHalf = bottomConeWidth / 2 * coreNarrow

            val corePath = Path().apply {
                moveTo(centerX - coreTopHalf, topY)
                cubicTo(
                    centerX - coreTopHalf, bottomY * 0.25f,
                    centerX - coreBottomHalf * 0.8f, bottomY * 0.5f,
                    centerX - coreBottomHalf, bottomY
                )
                lineTo(centerX + coreBottomHalf, bottomY)
                cubicTo(
                    centerX + coreBottomHalf * 0.8f, bottomY * 0.5f,
                    centerX + coreTopHalf, bottomY * 0.25f,
                    centerX + coreTopHalf, topY
                )
                close()
            }

            drawPath(
                path = corePath,
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to color.copy(alpha = intensity * 0.95f),
                        0.10f to color.copy(alpha = intensity * 0.75f),
                        0.35f to color.copy(alpha = intensity * 0.45f),
                        0.65f to color.copy(alpha = intensity * 0.18f),
                        1.0f to Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )
            )

            val hotspotTopHalf = topConeWidth / 2 * 0.22f
            val hotspotBottomHalf = bottomConeWidth / 2 * 0.22f

            val hotspotPath = Path().apply {
                moveTo(centerX - hotspotTopHalf, topY)
                cubicTo(
                    centerX - hotspotTopHalf, bottomY * 0.3f,
                    centerX - hotspotBottomHalf * 0.85f, bottomY * 0.55f,
                    centerX - hotspotBottomHalf, bottomY * 0.7f
                )
                lineTo(centerX + hotspotBottomHalf, bottomY * 0.7f)
                cubicTo(
                    centerX + hotspotBottomHalf * 0.85f, bottomY * 0.55f,
                    centerX + hotspotTopHalf, bottomY * 0.3f,
                    centerX + hotspotTopHalf, topY
                )
                close()
            }

            drawPath(
                path = hotspotPath,
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to hot.copy(alpha = intensity * 0.80f),
                        0.15f to hot.copy(alpha = intensity * 0.50f),
                        0.45f to color.copy(alpha = intensity * 0.22f),
                        1.0f to Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY * 0.7f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to hot.copy(alpha = intensity * 0.85f),
                        0.3f to color.copy(alpha = intensity * 0.50f),
                        0.7f to color.copy(alpha = intensity * 0.15f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(centerX, topY),
                    radius = topConeWidth * 0.6f
                ),
                center = Offset(centerX, topY),
                radius = topConeWidth * 0.6f
            )

            val streakAlpha = intensity * 0.15f
            val streakOffsets = listOf(-0.18f, -0.06f, 0.08f, 0.2f)
            for (offset in streakOffsets) {
                val streakX = centerX + bottomConeWidth / 2 * offset
                val topStreakX = centerX + topConeWidth / 2 * offset * 0.7f

                val streakPath = Path().apply {
                    moveTo(topStreakX - 1f, topY)
                    lineTo(topStreakX + 1f, topY)
                    lineTo(streakX + 1.5f, bottomY * 0.75f)
                    lineTo(streakX - 1.5f, bottomY * 0.75f)
                    close()
                }

                drawPath(
                    path = streakPath,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to hot.copy(alpha = streakAlpha),
                            0.3f to hot.copy(alpha = streakAlpha * 0.6f),
                            0.7f to hot.copy(alpha = streakAlpha * 0.2f),
                            1.0f to Color.Transparent
                        ),
                        startY = topY,
                        endY = bottomY * 0.75f
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun LightBeamPreviewDark() {
    LightBeam(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        isVisible = true,
        lightColor = { Color.White }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F6F0)
@Composable
fun LightBeamPreviewYellow() {
    LightBeam(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        isVisible = true,
        lightColor = { Color.Yellow },
        lightOpacity = 0.7f
    )
}